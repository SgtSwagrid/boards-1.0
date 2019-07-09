package strategybots.graphics;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Tile {
    
    protected final Window window;
    protected final TileRenderer renderer;
    
    private int x, y, width, height, angle;
    
    private float depth = 0.1F;
    
    private Optional<Texture> texture = Optional.empty();
    private Colour colour = Colour.BLACK;
    
    protected Optional<Tile> parent = Optional.empty();
    protected List<Tile> children = new LinkedList<>();
    
    private volatile boolean deleted = false;
    
    public Tile(Window window) {
        this.window = window;
        renderer = window.getRenderer();
        renderer.addTile(this);
        
    }
    
    public int getX() { return x; }
    
    public int getY() { return y; }
    
    public Tile setX(int x) {
        this.x = x;
        return this;
    }
    
    public Tile setY(int y) {
        this.y = y;
        return this;
    }
    
    public Tile setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public int getWidth() { return width; }
    
    public int getHeight() { return height; }
    
    public Tile setWidth(int width) {
        this.width = width;
        return this;
    }
    
    public Tile setHeight(int height) {
        this.height = height;
        return this;
    }
    
    public Tile setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public int getAngle() { return angle; }
    
    public Tile setAngle(int angle) {
        this.angle = angle;
        return this;
    }
    
    public float getDepth() { return depth; }
    
    public Tile setDepth(float depth) {
        this.depth = depth;
        return this;
    }
    
    public Optional<Texture> getTexture() { return texture; }
    
    public Tile setTexture(Texture texture) {
        if(!deleted) {
            renderer.removeTile(this);
            this.texture = Optional.ofNullable(texture);
            renderer.addTile(this);
        }
        return this;
    }
    
    public Colour getColour() { return colour; }
    
    public Tile setColour(Colour colour) {
        this.colour = colour;
        return this;
    }
    
    public Window getWindow() { return window; }
    
    public void setVisible(boolean visible) {
        if(visible && !deleted) renderer.addTile(this);
        else renderer.removeTile(this);
    }
    
    public void delete() {
        renderer.removeTile(this);
        deleted = true;
    }
    
    @Override
    public String toString() {
        return getWidth() + " x " + getHeight() + " Tile @ (" + getX() + ", " + getY() + ")";
    }
}