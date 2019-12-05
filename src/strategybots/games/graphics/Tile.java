package strategybots.games.graphics;

import java.io.Serializable;
import java.util.Optional;

/**
 * A simple square primitive for UI rendering.
 * 
 * @author Alec Dorrrington
 */
public class Tile implements Serializable {
    
    private static final long serialVersionUID = 7954649994471522689L;
    
    /** The window in which the tile resides. */
    protected final Window window;
    /** The renderer with which the tile is rendered. */
    protected final TileShader shader;
    
    /** The position and size of the tile. */
    private int x, y, width, height, angle;
    
    /** The depth of the tile, used to order tiles. */
    private float depth = 0.1F;
    
    /** The (optional) texture applied to the tile. */
    private Texture texture;
    /** The colour of the tile, either solid or applied to the texture. */
    private Colour colour = Colour.BLACK;
    
    /**
     * Constructs a new immediately-visible tile (square) for rendering.
     * @param window the window in which the tile is to reside.
     */
    public Tile(Window window) {
        this.window = window;
        shader = (TileShader) window.getShader();
        shader.addTile(this);
    }
    
    /**
     * @return the current x-coordinate (pixels) of the tile centre.
     */
    public int getX() { return x; }
    
    /**
     * @return the current y-coordinate (pixels) of the tile centre.
     */
    public int getY() { return y; }
    
    /**
     * @param x the new x-coordinate (pixels) of the tile centre.
     * @return this tile.
     */
    public Tile setX(int x) {
        this.x = x;
        return this;
    }
    
    /**
     * @param y the new y-coordinate (pixels) of the tile centre.
     * @return this tile.
     */
    public Tile setY(int y) {
        this.y = y;
        return this;
    }
    
    /**
     * @param x the new x-coordinate (pixels) of the tile centre.
     * @param y the new y-coordinate (pixels) of the tile centre.
     * @return this tile.
     */
    public Tile setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * @return the current width (pixels) of the tile.
     */
    public int getWidth() { return width; }
    
    /**
     * @return the current height (pixels) of the tile.
     */
    public int getHeight() { return height; }
    
    /**
     * @param width the new width (pixels) of the tile.
     * @return this tile.
     */
    public Tile setWidth(int width) {
        this.width = width;
        return this;
    }
    
    /**
     * @param height the new height (pixels) of the tile.
     * @return this tile.
     */
    public Tile setHeight(int height) {
        this.height = height;
        return this;
    }
    
    /**
     * @param width the new width (pixels) of the tile.
     * @param height the new height (pixels) of the tile.
     * @return this tile.
     */
    public Tile setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }
    
    /**
     * @return the current angle (anti-clockwise degrees) of the tile.
     */
    public int getAngle() { return angle; }
    
    /**
     * @param angle the new angle (anti-clockwise degrees) of the tile.
     * @return this tile.
     */
    public Tile setAngle(int angle) {
        this.angle = angle;
        return this;
    }
    
    /**
     * @return the current depth (for ordering) of the tile.
     */
    public float getDepth() { return depth; }
    
    /**
     * @param depth the new depth (for ordering) of the tile.
     * @return this tile.
     */
    public Tile setDepth(float depth) {
        this.depth = depth;
        return this;
    }
    
    /**
     * @return the current texture of the tile (if there is one).
     */
    public Optional<Texture> getTexture() {
        return Optional.ofNullable(texture);
    }
    
    /**
     * @param texture the new texture of the tile.
     * @return this tile.
     */
    public Tile setTexture(Texture texture) {
        this.texture = texture;
        return this;
    }
    
    /**
     * @return the current colour of the tile.
     */
    public Colour getColour() { return colour; }
    
    /**
     * @param colour the new colour of the tile.
     * @return this tile.
     */
    public Tile setColour(Colour colour) {
        this.colour = colour;
        return this;
    }
    
    /**
     * @return the window in which the tile exists.
     */
    public Window getWindow() { return window; }
    
    /**
     * @param visible whether the tile is visible to the renderer.
     */
    public void setVisible(boolean visible) {
        if(visible) shader.addTile(this);
        else shader.removeTile(this);
    }
    
    /**
     * Remove tile from renderer, and remove events from tile.
     */
    public void destroy() {
        shader.removeTile(this);
    }
    
    @Override
    public String toString() {
        return getWidth() + " x " + getHeight() + " Tile @ ("
                + getX() + ", " + getY() + ")";
    }
}