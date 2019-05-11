package games.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import swagui.api.Button;
import swagui.api.Colour;
import swagui.api.Tile;
import swagui.api.Window;

/**
 * Chessboard implementation for use in games which require
 * a fixed rectangular board with at most one piece per tile.<br>
 * <br>
 * Creates a window and provides automatic tile layout.
 * 
 * @author Alec Dorrignton
 */
public class ChessBoard {
    
    /** Light tile colour. */
    public static Colour TILE_COLOUR1 = Colour.rgb(248, 239, 186);
    /** Dark tile colour. */
    public static Colour TILE_COLOUR2 = Colour.rgb(234, 181, 67);
    
    /** Size of each tile in pixels. */
    public static int TILE_SIZE = 96;
    
    /** Dimensions of the board in number of tiles. */
    private int width, height;
    
    /** The window in which this chessboard resides. */
    private Window window;
    
    /** The tile squares. */
    private ChessboardTile[][] tiles;
    
    /**
     * Constructs a new chessboard with the given dimensions and title.<br>
     * Automatically opens a window for the chessboard in the process.
     * @param width the width of the chessboard in number of tiles.
     * @param height the height of the chessboard in number of tiles.
     * @param title the title of the window in which the chessboard resides.
     */
    public ChessBoard(int width, int height, String title) {
        
        //Set the dimensions of this chessboard.
        this.width = width;
        this.height = height;
        
        //Create an appropriately sized window.
        window = new Window(width * TILE_SIZE, height * TILE_SIZE, title);
        
        //Create arrays for buttons (tiles) and associated click listeners.
        tiles = new ChessboardTile[width][height];
        
        //For each grid cell.
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //Create each tile in the board.
                tiles[x][y] = new ChessboardTile(window, x, y);
            }
        }
        //Set the initial colours for the board.
        resetColours();
    }
    
    /**
     * Changes the background colours of the board.<br>
     * The 2 given colours are distributed in a chessboard pattern.
     * @param colour1 the first tile colour.
     * @param colour2 the second tile colour.
     */
    public void setBackground(Colour colour1, Colour colour2) {
        TILE_COLOUR1 = colour1;
        TILE_COLOUR2 = colour2;
        resetColours();
    }
    
    /**
     * Attach a new listener to given board position.<br>
     * The given action will be triggered once when the tile is clicked.<br>
     * Will not replace any existing listeners - a single tile can have multiple listeners.
     * @param x the x position for which to add the listener.
     * @param y the y position for which to add the listener.
     * @param l the action to be triggered on click.
     */
    public void addListener(int x, int y, Action l) {
        tiles[x][y].listeners.add(l);
    }
    
    /**
     * Attach a new listener to every board position.<br>
     * The given action will be triggered once when any tile is clicked,
     * with the position of the tile passed in as parameters.<br>
     * Will not replace any existing listeners - a single tile can have multiple listeners.
     * @param l the action to be triggered on click.
     */
    public void addListenerToAll(BiConsumer<Integer, Integer> l) {
        
        //For each tile on the board.
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                
                int x = i, y = j;
                //Add the listener to this tile.
                addListener(x, y, () -> l.accept(x, y));
            }
        }
    }
    
    /**
     * Move a piece so that it is centered on the tile at the given position.
     * @param tile the tile to move.
     * @param x the x position to move the tile to.
     * @param y the y position to move the tile to.
     */
    public void setPosition(Tile tile, int x, int y) {
        tiles[x][y].moveTile(tile);
    }
    
    /**
     * Set the colour of the tile at a particular position.<br>
     * Can be undone by using 'resetColours()'.
     * @param x the x position of the tile to change colour.
     * @param y the y position of the tile to change colour.
     * @param colour the new colour for the tile.
     */
    public void setColour(int x, int y, Colour colour) {
        tiles[x][y].setColour(colour);
    }
    
    /**
     * Reset the colour of all tiles on the board.
     */
    public void resetColours() {
        //For each tile.
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //Set to default colour (chessboard pattern).
                tiles[x][y].setColour((x + y) % 2 == 0 ? TILE_COLOUR1 : TILE_COLOUR2);
            }
        }
    }
    
    /**
     * Close the window and destroy the chessboard.
     */
    public void close() { window.close(); }
    
    /**
     * @return the width of the chessboard in number of tiles.
     */
    public int getWidth() { return width; }
    
    /**
     * @return the height of the chessboard in number of tiles.
     */
    public int getHeight() { return height; }
    
    /**
     * @return the window in which this chessboard resides.
     */
    public Window getWindow() { return window; }
    
    /**
     * Functional interface used for on click listeners.
     * @author Alec Dorrington
     */
    @FunctionalInterface
    public interface Action { void run(); }
    
    /**
     * Represents a single tile in a chessboard.
     * @author Alec Dorrington
     */
    private class ChessboardTile extends Button {
        
        //The position of this tile.
        int x, y;
        
        /** Click listeners for this tile. */
        Set<Action> listeners = new HashSet<>();
        
        /**
         * Constructs a new chessboard tile at the given position.
         * @param window the window in which the chessboard resides.
         * @param x the x position of the tile.
         * @param y the y position of the tile.
         */
        public ChessboardTile(Window window, int x, int y) {
            
            super(window);
            //Set the position of this tile.
            this.x = x;
            this.y = y;
            
            //Set the graphical position of the tile.
            moveTile(this);
            setSize(TILE_SIZE, TILE_SIZE);
            setDepth(0.05F);
        }
        
        /**
         * Moves the tile so that it is centered on this chess tile.
         * @param tile the tile to move.
         */
        void moveTile(Tile tile) {
            
            //Calculate the position (in pixels) to which the tile should be moved.
            int xx = (x - width / 2) * TILE_SIZE + (width + 1) % 2 * TILE_SIZE / 2;
            int yy = (y - height / 2) * TILE_SIZE + (height + 1) % 2 * TILE_SIZE / 2;
            
            //Move the tile to its new position.
            tile.setPosition(xx, yy);
        }
        
        @Override
        protected void onLeftClick() {
            //When this tile is clicked, trigger all of its listeners.
            listeners.forEach(Action::run);
        }
        
        @Override
        public Colour getColour() {
            
            Colour colour = this.colour;
            
            //If the cursor is over this button.
            if(checkBounds(Mouse.getX() - Display.getWidth() / 2,
                    Mouse.getY() - Display.getHeight() / 2)) {
               
               //Darken the tile slightly.
               colour = colour.darken(0.1F);
               
               //If the left click button is pressed.
               if(Mouse.isButtonDown(0)) {
                   //Darken the tile further.
                   colour = colour.darken(0.1F);
               }
           }
           return colour;
       }
    }
}