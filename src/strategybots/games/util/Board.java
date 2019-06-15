package strategybots.games.util;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import strategybots.event.Event;
import strategybots.graphics.Button;
import strategybots.graphics.Colour;
import strategybots.graphics.Tile;
import strategybots.graphics.Window;
import strategybots.graphics.Window.WindowResizeEvent;

/**
 * Chessboard implementation for use in games which require
 * a fixed rectangular board with at most one piece per tile.<br>
 * <br>
 * Creates a window and provides automatic tile layout.
 * 
 * @author Alec Dorrignton
 */
public class Board {
    
    public enum Pattern { CHECKER, TABLE, GINGHAM, SOLID }
    
    /** Tile colours. */
    private Colour[] tileColours = new Colour[] {
            Colour.rgb(248, 239, 186), Colour.rgb(234, 181, 67)};
    
    /** Background colour. */
    private Colour background = Colour.rgb(25, 42, 86);
    
    /** The pattern with which the background is created. */
    private Pattern pattern = Pattern.CHECKER;
    
    /** Dimensions of the board in number of tiles. */
    private int width, height;
    
    /** The window in which this chessboard resides. */
    private Window window;
    
    /** The tile squares. */
    private BoardTile[][] tiles;
    
    /** Relative size ratio of each column/row. */
    private int[] colSize, rowSize;
    /** Cumulative size of columns/rows. */
    private int[] cumWidth, cumHeight;
    
    /**
     * Constructs a new chessboard with the given dimensions and title.<br>
     * Automatically opens a window for the chessboard in the process.
     * @param width the width of the chessboard in number of tiles.
     * @param height the height of the chessboard in number of tiles.
     * @param title the title of the window in which the chessboard resides.
     */
    public Board(int width, int height, String title) {
        
        //Set the dimensions of this chessboard.
        this.width = width;
        this.height = height;
        
        //Create an appropriately sized window.
        window = new Window(1000, 1000, title);
        window.setColour(background);
        
        //Tiles should automatically resize/reposition on window resize.
        Event.addHandler(WindowResizeEvent.class, e -> update());
        
        //Create the tiles (grid cells).
        createTiles();
        
        //Set the initial colours for the board.
        resetColours();
    }
    
    /**
     * Sets the relative width of the given column.
     * @param col the index of the column to be changed.
     * @param width the new relative width.
     */
    public void setColWidth(int col, int width) {
        
        //Determine difference between old and new widths.
        int delta = width - colSize[col];
        colSize[col] = width;
        //Update cumulative width for all subsequent columns.
        for(int x = col; x < this.width; x++) {
            cumWidth[x] += delta;
        }
        update();
    }
    
    /**
     * Sets the relative height of the given row.
     * @param row the index of the row to be changed.
     * @param height the new relative height.
     */
    public void setRowHeight(int row, int height) {
        
        //Determine different between old and new heights.
        int delta = height - rowSize[row];
        rowSize[row] = height;
        //Update cumulative height for all subsequent rows.
        for(int y = row; y < this.height; y++) {
            cumHeight[y] += delta;
        }
        update();
    }
    
    /**
     * Changes the background colours and pattern of the board.<br>
     * @param pattern the pattern with which the colours are arranged.
     * @param the colours to use.
     */
    public void setBackground(Pattern pattern, Colour... colours) {
        this.pattern = pattern;
        tileColours = colours.clone();
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
        
        //Ensure piece isn't present in another tile.
        for(int xx = 0; xx < width; xx++) {
            for(int yy = 0; yy < height; yy++) {
                if(tiles[xx][yy].piece.orElse(null) == tile) {
                    tiles[xx][yy].piece = Optional.empty();
                }
            }
        }
        tiles[x][y].setPiece(tile);
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
                
                switch(pattern) {
                    
                    //Checked pattern.
                    case CHECKER:
                        tiles[x][y].setColour((x + y) % 2 == 0
                                ? tileColours[0] : tileColours[1]);
                        break;
                    
                    //Table pattern.
                    case TABLE:
                        tiles[x][y].setColour(x % 2 == 1 && y % 2 == 1
                                ? tileColours[0] : tileColours[1]);
                        break;
                        
                    //Gingham pattern.
                    case GINGHAM:
                        if(x % 2 == 1 && y % 2 == 1) tiles[x][y].setColour(tileColours[0]);
                        else if(x % 2 == 1 ^ y % 2 == 1) tiles[x][y].setColour(tileColours[1]);
                        else tiles[x][y].setColour(tileColours[2]);
                        break;
                        
                    //Solid colour.
                    case SOLID:
                        tiles[x][y].setColour(tileColours[0]);
                        break;
                }
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
     * Creates the tiles (grid cells) for the board.
     */
    private void createTiles() {
        
        //Create arrays for buttons (tiles) and associated click listeners.
        tiles = new BoardTile[width][height];
        
        //Initialise column/row relative size arrays.
        colSize = new int[width];
        rowSize = new int[height];
        
        cumWidth = new int[width];
        cumHeight = new int[height];
        
        //Set all columns to be of equal width.
        for(int x = 0; x < width; x++) {
            colSize[x] = 1;
            cumWidth[x] = x+1;
        }
        
        //Set all rows to be of equal height.
        for(int y = 0; y < height; y++) {
            rowSize[y] = 1;
            cumHeight[y] = y+1;
        }
        
        //For each grid cell.
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                //Create each tile in the board.
                tiles[x][y] = new BoardTile(window, x, y);
            }
        }
    }
    
    /**
     * Updates the position and size of all the tiles.
     */
    private void update() {
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                tiles[x][y].update();
            }
        }
    }
    
    /**
     * Calculates the required board width to match the window size,
     * subject to maintaining proper aspect ratios.
     * @return the width of the board itself, in pixels.
     */
    private int getBoardWidth() {
        return Math.min(window.getWidth(), window.getHeight()
                * cumWidth[width-1]/cumHeight[height-1]);
    }
    
    /**
     * Calculates the required board height to match the window size,
     * subject to maintaining proper aspect ratios.
     * @return the height of the board itself, in pixels.
     */
    private int getBoardHeight() {
        return Math.min(window.getHeight(), window.getWidth()
                * cumHeight[height-1]/cumWidth[width-1]);
    }
    
    /**
     * Functional interface used for on click listeners.
     * @author Alec Dorrington
     */
    @FunctionalInterface
    public interface Action { void run(); }
    
    /**
     * Represents a single grid cell in a board.
     * @author Alec Dorrington
     */
    private class BoardTile extends Button {
        
        //The position of this tile.
        int x, y;
        
        //The piece currently on this tile.
        Optional<Tile> piece = Optional.empty();
        
        /** Click listeners for this tile. */
        Set<Action> listeners = new HashSet<>();
        
        /**
         * Constructs a new chessboard tile at the given position.
         * @param window the window in which the chessboard resides.
         * @param x the x position of the tile.
         * @param y the y position of the tile.
         */
        public BoardTile(Window window, int x, int y) {
            
            super(window);
            //Set the position of this tile.
            this.x = x;
            this.y = y;
            
            //Set the graphical position of the tile.
            update();
            setDepth(0.05F);
        }
        
        /**
         * Sets the piece currently on this tile.
         * Will remove any previous pieces.
         * Used to keep the piece updated graphically.
         * @param piece the piece to move onto this tile.
         */
        void setPiece(Tile piece) {
            this.piece = Optional.of(piece);
            moveTile(piece);
        }
        
        /**
         * Moves a tile so that it is centered on this board tile.
         * @param tile the tile to move.
         */
        void moveTile(Tile tile) {
            
            //Calculate the position (in relative coordinates) to which the tile should be moved.
            int xx = (x != 0 ? 2*cumWidth[x-1] : 0) + colSize[x];
            int yy = (y != 0 ? 2*cumHeight[y-1] : 0) + rowSize[y];
            
            //Convert the position to pixel space.
            xx = -getBoardWidth()/2 + xx * getBoardWidth()/cumWidth[width-1]/2;
            yy = -getBoardHeight()/2 + yy * getBoardHeight()/cumHeight[height-1]/2;
            
            //Move the tile to its new position.
            tile.setPosition(xx, yy);
            
            //Determine the appropriate size in pixels given relative column/row sizes.
            tile.setWidth(colSize[x] * getBoardWidth()/cumWidth[width - 1] + 1);
            tile.setHeight(rowSize[y] * getBoardHeight()/cumHeight[height - 1] + 1);
        }
        
        /**
         * Update this tile by repositioning/resizing it and any of its attached pieces.
         * To be called upon initialization or window resize.
         */
        void update() {
            moveTile(this);
            if(piece.isPresent()) moveTile(piece.get());
        }
        
        @Override
        protected void onLeftClick() {
            //When this tile is clicked, trigger all of its listeners.
            listeners.parallelStream().forEach(Action::run);
        }
        
        @Override
        public Colour getColour() {
            
            Colour colour = super.getColour();
            
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