package games.castle.render;

import org.lwjgl.util.vector.Vector2f;

public class Tile {
	
	protected Vector2f position = new Vector2f(0.0F, 0.0F);
	protected Vector2f size = new Vector2f(100.0F, 100.0F);
	
	protected float depth = 0.1F;
	
	protected Texture texture = Texture.BLANK;
	protected Colour colour = Colour.WHITE;
	
	public Tile() {
		TileRenderer.INSTANCE.addTile(this);
	}
	
	public Vector2f getPosition() { return position; }
	
	public Vector2f getSize() { return size; }
	
	public float getDepth() { return depth; }
	
	public Texture getTexture() { return texture; }
	
	public Colour getColour() { return colour; }
}