package strategybots.games.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

/**
 * Shader for rendering tiles.
 * 
 * @author Alec Dorrington
 */
public class TileShader extends Shader {
    
    /** Singleton tile shader instance. */
    public static final TileShader INSTANCE = new TileShader();
    
    /** Shader program files associated with the tile shader. */
    private static final String VERTEX_SHADER   = "/strategybots/games/graphics/tile_vertex.shdr",
                                FRAGMENT_SHADER = "/strategybots/games/graphics/tile_fragment.shdr";
    
    /** The tiles currently visible to the renderer. */
    private List<Tile> tiles = new LinkedList<>();
    
    /** Lock on the list of tiles. */
    private Semaphore lock = new Semaphore(1);
    
    /**
     * Create the tile shader instance with the appropriate shader files.
     */
    private TileShader() { super(VERTEX_SHADER, FRAGMENT_SHADER); }
    
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
        
        //Load tile mesh.
        loadMesh(Mesh.SQUARE);
        
        //Render each tile in set.
        for(Tile tile : tiles) {
            
            //Load the texture.
            loadTexture(tile.getTexture());
            //Render the tile.
            renderTile(tile);
        }
        unloadMesh();
        
        lock.release();
    }
    
    /**
     * Render a tile.
     * @param t the tile to render.
     */
    private void renderTile(Tile t) {
        
        //Load tile property uniforms to renderer.
        setUniform("position", new Vector2f(t.getX(), t.getY()));
        setUniform("size", new Vector2f(t.getWidth(), t.getHeight()));
        setUniform("angle", t.getAngle());
        setUniform("depth", t.getDepth());
        setUniform("colour", t.getColour().asVector());
        setUniform("hasTexture", t.getTexture().isPresent());
        
        //Render tile VAO.
        glDrawArrays(GL_TRIANGLES, 0, Mesh.SQUARE.getNumVertices());
    }
    
    /**
     * Load a mesh to OpenGL.
     * @param m the mesh to load.
     */
    private void loadMesh(Mesh m) {
        //Load VAO.
        glBindVertexArray(m.getVaoId());
        //Load each VBO.
        for(int i = 0; i < 2; i++) {
            glEnableVertexAttribArray(i);
        }
    }
    
    /**
     * Unload current mesh from OpenGL.
     */
    private void unloadMesh() {
        //Unload each VBO.
        for(int i = 0; i < 2; i++) {
            glDisableVertexAttribArray(i);
        }
        //Unload VAO.
        glBindVertexArray(0);
    }
    
    /**
     * Load a texture to OpenGL.
     * @param texture the optional texture to load.
     */
    private void loadTexture(Optional<Texture> texture) {
        
        //Enable texture rendering.
        glActiveTexture(GL_TEXTURE0);
        glEnable(GL_CULL_FACE);
        
        if(texture.isPresent()) {
            
            //Load texture.
            Texture t = texture.get();
            glBindTexture(GL_TEXTURE_2D, t.getTextureId());
            
            //Disable culling if texture has transparency.
            if(!t.isOpaque()) glDisable(GL_CULL_FACE);
        }
    }
    
    /**
     * Add a new tile to rendering.
     * @param tile the tile to add.
     */
    public void addTile(Tile tile) {
        
        acquireLock();
        if(!tiles.contains(tile)) {
            tiles.add(tile);
            tiles.sort((t1, t2) -> Float.compare(t1.getDepth(), t2.getDepth()));
        }
        lock.release();
    }
    
    /**
     * Remove a tile from rendering.
     * @param tile the tile to remove.
     */
    public void removeTile(Tile tile) {
        
        acquireLock();
        tiles.remove(tile);
        lock.release();
    }
    
    /**
     * Acquire lock for list of tiles.
     */
    private void acquireLock() {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}