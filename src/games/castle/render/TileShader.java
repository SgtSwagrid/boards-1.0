package games.castle.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

public class TileShader extends Shader {
	
	private static final String VERTEX_SHADER   = "src/games/castle/render/tile_vertex.shdr",
								FRAGMENT_SHADER = "src/games/castle/render/tile_fragment.shdr";
	
	public static final TileShader INSTANCE = new TileShader();
	
	private TileShader() {
		super(VERTEX_SHADER, FRAGMENT_SHADER);
	}
	
	@Override
	protected void bindAttribs() {
		bindAttrib(0, "vertex");
		bindAttrib(1, "texmap");
	}

	@Override
	protected void init() {}
	
	@Override
	protected void loadFrame() {
		setUniform("screenSize", new Vector2f(Display.getWidth(), Display.getHeight()));
		loadMesh(Shapes.SQUARE);
	}
	
	@Override
	protected void unloadFrame() {
		unloadMesh();
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
	
	private void loadTexture(Texture t) {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D,t.getTextureId());
		
		if(t.isOpaque()) glEnable(GL_CULL_FACE);
		else glDisable(GL_CULL_FACE);
	}
	
	public void render(Tile t) {
		setUniform("position", t.getPosition());
		setUniform("size", t.getSize());
		setUniform("depth", t.getDepth());
		setUniform("colour", t.getColour().asVector());
		loadTexture(t.getTexture());
		glDrawArrays(GL_TRIANGLES, 0, Shapes.SQUARE.getNumVertices());
	}
}