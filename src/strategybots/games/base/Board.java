package strategybots.games.base;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import swagui.graphics.Colour;
import swagui.graphics.Gradient;
import swagui.input.Button;
import swagui.input.InputHandler;
import swagui.layouts.Frame;
import swagui.layouts.HorizontalList;
import swagui.layouts.Layout.Fill;
import swagui.layouts.VerticalList;
import swagui.tiles.Scene2D;
import swagui.window.Window;

/**
 * A grid-based game board.
 * @author Alec Dorrington
 */
public class Board {
    
    /** Scene in which the board tiles are placed. */
    private Scene2D scene = new Scene2D();
    /** Input handler for the board. */
    private InputHandler input = new InputHandler();
    /** Window in which the scene is shown. */
    private Window window = new Window(1280, 960, "Game", scene, input);
    
    /** Dimensions of the board (number of tiles). */
    private int width, height;
    
    /** Grid of tiles on the board. */
    private Frame[][] board;
    
    /** Function for determining board colours. */
    private BiFunction<Integer, Integer, Gradient> background = (x, y) ->
            (x+y)%2==0 ? Colour.SPICED_BUTTERNUT : Colour.MANDARIN_SORBET;
    
    /** Functions for determining row/column widths/heights. */
    private Function<Integer, Integer> cols = c -> 1, rows = r -> 1;
    
    /** Callback functions for when the board is left-clicked. */
    private List<BiConsumer<Integer, Integer>> callbacks = new LinkedList<>();
    
    /**
     * Create a new board.
     * @param width of the board (grid squares).
     * @param height of the board (grid squares).
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }
    
    /**
     * Set the background tile colours of the board.
     * @param colours function for determining tile colours.
     * @return this board.
     */
    public Board setBackground(BiFunction<Integer, Integer, Gradient> colours) {
        this.background = colours;
        clearHighlights();
        return this;
    }
    
    /**
     * Set the column widths of the board.
     * @param cols function for determining column widths.
     * @return this board.
     */
    public Board setCols(Function<Integer, Integer> cols) {
        this.cols = cols;
        return this;
    }
    
    /**
     * Set the row heights of the board.
     * @param rows function for determining row heights.
     * @return this board.
     */
    public Board setRows(Function<Integer, Integer> rows) {
        this.rows = rows;
        return this;
    }
    
    /**
     * Add listener for when a grid square is left-clicked.
     * Callback function is given grid coordinates of clicked square.
     * @param callback function to call upon click.
     * @return this board.
     */
    public Board onClick(BiConsumer<Integer, Integer> callback) {
        callbacks.add(callback);
        return this;
    }
    
    /**
     * @return the width of the board (number of tiles).
     */
    public int getWidth() { return width; }
    
    /**
     * @return the height of the board (number of tiles).
     */
    public int getHeight() { return height; }
    
    /**
     * Set the colour of a particular board tile.
     * @param x coordinate of tile.
     * @param y coordinate of tile.
     * @param colour of tile.
     * @return this board.
     */
    public Board highlight(int x, int y, Gradient colour) {
        
        clearHighlights();
        board[x][y].setColour(colour);
        return this;
    }
    
    /**
     * Reset the background tile colours of the board.
     * Used to undo any calls to setGradient().
     * @return this board.
     */
    public Board clearHighlights() {
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                board[x][y].setColour(background.apply(x, y));
            }
        }
        return this;
    }
    
    /**
     * Display a game state on the board.
     * @param state to show.
     * @return this board.
     */
    public Board setState(State<?> state) {
        
        //For each grid square on the board.
        forEachSquare((x, y) -> {
            
            //Remove all previous pieces.
            board[x][y].clearChildren();
            
            //If there is a new piece here, add it to the square.
            if(state.getPiece(x, y).isPresent()) {
                board[x][y].addTile(state.getPiece(x, y).get().getTile());
            }
        });
        scene.update();
        return this;
    }
    
    /**
     * Set the title of the window in which the board is shown.
     * @param title of the window for the board.
     * @return this board.
     */
    public Board setTitle(String title) {
        window.setTitle(title);
        return this;
    }
    
    public Board forEachSquare(BiConsumer<Integer, Integer> action) {
        
        for(int x = 0; x < getWidth(); x++) {
            for(int y = 0; y < getHeight(); y++) {
                action.accept(x, y);
            }
        }
        return this;
    }
    
    public boolean inBounds(int x, int y) {
        
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }
    
    /**
     * Show the board, blocking execution until exit.
     */
    public void show() {
        window.open();
    }
    
    /**
     * @return the window in which the board is shown.
     */
    public Window getWindow() { return window; }
    
    /**
     * Initialize the board.
     */
    private void init() {
        
        board = new Frame[width][height];
        
        //Get total relative dimensions to calculate aspect ratio.
        int w = IntStream.range(0, width).map(cols::apply).sum();
        int h = IntStream.range(0, height).map(rows::apply).sum();
        
        //Create vertical list of rows of grid squares.
        VerticalList grid = (VerticalList) new VerticalList()
            .setFill(Fill.FILL_PARENT_ASPECT)
            .setAspectRatio((float)w / (float)h);
        
        IntStream.range(0, height).forEach(y -> {
            
            //Create each horizontal row of grid squares.
            HorizontalList row = (HorizontalList) new HorizontalList()
                .setFill(Fill.FILL_PARENT)
                .setVWeight(rows.apply(y));
            
            IntStream.range(0, width).forEach(x -> {
                
                //Create each grid square in the row.
                Button square = (Button) new Button(input)
                    .onClick((bx, by) -> callbacks
                        .forEach(c -> c.accept(x, y)))
                    .setFill(Fill.FILL_PARENT)
                    .setHWeight(cols.apply(x))
                    .setColour(background.apply(x, y));
                
                //Add grid square to board and row.
                board[x][y] = square;
                row.addTile(square);
            });
            grid.addTile(row, 0);
        });
        scene.getBackground().addTile(grid);
        scene.update();
    }
}