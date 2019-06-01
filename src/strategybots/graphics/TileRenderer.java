package strategybots.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

public class TileRenderer extends Renderer {
    
    private static final String VERTEX_SHADER   = "/strategybots/graphics/tile_vertex.shdr",
                                FRAGMENT_SHADER = "/strategybots/graphics/tile_fragment.shdr";
    
    private Map<Optional<Texture>, Set<Tile>> tiles = new HashMap<>();
    
    private Semaphore lock = new Semaphore(1);
    
    public TileRenderer() { super(VERTEX_SHADER, FRAGMENT_SHADER); }
    
    @Override
    protected void bindAttribs() {
        bindAttrib(0, "vertex");
        bindAttrib(1, "texmap");
    }
    
    @Override
    protected void init() {
        setUniform("screenSize", new Vector2f(
                Display.getWidth(), Display.getHeight()));
    }
    
    @Override
    protected void onWindowResize() {
        setUniform("screenSize", new Vector2f(
                Display.getWidth(), Display.getHeight()));
    }
    
    @Override
    protected void render() {
        
        acquireLock();
        
        loadMesh(Mesh.SQUARE);
        
        for(Optional<Texture> texture : tiles.keySet()) {
            
            loadTexture(texture);
            
            for(Tile tile : tiles.get(texture)) {
                
                renderTile(tile);
            }
        }
        unloadMesh();
        
        lock.release();
    }
    
    private void renderTile(Tile t) {
        
        setUniform("position", new Vector2f(t.getX(), t.getY()));
        setUniform("size", new Vector2f(t.getWidth(), t.getHeight()));
        setUniform("depth", t.getDepth());
        setUniform("colour", t.getColour().asVector());
        setUniform("hasTexture", t.getTexture().isPresent());
        
        glDrawArrays(GL_TRIANGLES, 0, Mesh.SQUARE.getNumVertices());
    }
    
    private void loadMesh(Mesh m) {
        glBindVertexArray(m.getVaoId());
        for(int i = 0; i < 2; i++) {
            glEnableVertexAttribArray(i);
        }
    }
    
    private void unloadMesh() {
        for(int i = 0; i < 2; i++) {
            glDisableVertexAttribArray(i);
        }
        glBindVertexArray(0);
    }
    
    private void loadTexture(Optional<Texture> texture) {
        
        glActiveTexture(GL_TEXTURE0);
        glEnable(GL_CULL_FACE);
        
        if(texture.isPresent()) {
            
            Texture t = texture.get();
            glBindTexture(GL_TEXTURE_2D, t.getTextureId());
            
            if(!t.isOpaque()) glDisable(GL_CULL_FACE);
            
        }
    }
    
    public void addTile(Tile tile) {
        
        acquireLock();
        
        if(!tiles.containsKey(tile.getTexture()))
            tiles.put(tile.getTexture(), new HashSet<>());
        
        tiles.get(tile.getTexture()).add(tile);
        
        lock.release();
    }
    
    public void removeTile(Tile tile) {
        
        acquireLock();
        
        if(tiles.containsKey(tile.getTexture()))
            tiles.get(tile.getTexture()).remove(tile);
        
        lock.release();
    }
    
    private void acquireLock() {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}