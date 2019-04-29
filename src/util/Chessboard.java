package util;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import swagui.api.Button;
import swagui.api.Colour;
import swagui.api.Tile;
import swagui.api.Window;

public class Chessboard {
    
    public static final Colour TILE_COLOUR1 = Colour.rgb(248, 239, 186);
    public static final Colour TILE_COLOUR2 = Colour.rgb(234, 181, 67);
    public static final Colour HIGHLIGHT_COLOUR = Colour.rgb(85, 230, 193);
    
    public static final int TILE_SIZE = 96;
    
    private int width, height;
    
    private Window window;
    private Button[][] tiles;
    private Action[][] listeners;
    
    public Chessboard(int width, int height, String title) {
        
        this.width = width;
        this.height = height;
        
        window = new Window(width * TILE_SIZE, height * TILE_SIZE, title);
        
        tiles = new Button[width][height];
        listeners = new Action[width][height];
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                createButton(x, y);
            }
        }
    }
    
    public void addListener(int x, int y, Action l) {
        listeners[x][y] = l;
    }
    
    public void movePiece(Tile piece, int x, int y) {
        piece.setPosition((x - width / 2) * TILE_SIZE + TILE_SIZE / 2,
                (y - height / 2) * TILE_SIZE + TILE_SIZE / 2);
    }
    
    public void highlightTile(int x, int y) {
        tiles[x][y].setColour(HIGHLIGHT_COLOUR);
    }
    
    public void unhighlightTile(int x, int y) {
        tiles[x][y].setColour((x + y) % 2 == 0 ? TILE_COLOUR1 : TILE_COLOUR2);
    }
    
    public void close() { window.close(); }
    
    public int getWidth() { return width; }
    
    public int getHeight() { return height; }
    
    public Window getWindow() { return window; }
    
    private void createButton(int x, int y) {
        
        tiles[x][y] = new Button(window) {
            
            @Override protected void onLeftClick() {
                if(listeners[x][y] != null)
                    listeners[x][y].run();
            }
            
           @Override public Colour getColour() {
               
               Colour colour = this.colour;
               
               if(checkBounds(Mouse.getX() - Display.getWidth() / 2,
                       Mouse.getY() - Display.getHeight() / 2)) {
                   
                   colour = colour.darken(0.05F);
                   
                   if(Mouse.isButtonDown(0)) {
                       colour = colour.darken(0.05F);
                   }
               }
               return colour;
           }
        };
        
        tiles[x][y].setPosition((x - width / 2) * TILE_SIZE + TILE_SIZE / 2,
                (y - height / 2) * TILE_SIZE + TILE_SIZE / 2);
        tiles[x][y].setSize(TILE_SIZE, TILE_SIZE);
        tiles[x][y].setColour((x + y) % 2 == 0 ? TILE_COLOUR1 : TILE_COLOUR2);
        tiles[x][y].setDepth(0.05F);
    }
    
    @FunctionalInterface
    public interface Action { void run(); }
}