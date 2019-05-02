package games;

import games.util.ChessBoard;
import games.util.Game;
import games.util.IllegalMoveException;
import games.util.Player;
import swagui.api.Colour;
import swagui.api.Texture;
import swagui.api.Tile;

/**
 * <b>Chomp implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Chomp">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Chomp>'.<br>
 * Human players can be made by instantiating 'ChompController'.
 * 
 * @author Alec Dorrington
 */
public class Chomp extends Game {
    
    /** Title of the window. */
    private static final String TITLE = "Chomp";
    
    /** Chessboard instance, manages the window, and tile layout. */
    private ChessBoard board;
    
    /** Stores whether the tile at each position has been chomped. */
    private boolean[][] chomped;
    /** The running total number of tiles which have been chomped. */
    private int numChomped = 0;
    
    /** The dimensions of the game board. */
    private int width, height;
    
    /** Whether the current turn has been completed yet. */
    private volatile boolean turnTaken = false;
    
    /**
     * Asynchronously runs a new Chomp instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param players the players who are to participate.
     */
    @SafeVarargs
    public Chomp(int width, int height, Player<Chomp>... players) {
        
        super(players);
        
        //Set the board dimensions.
        this.width = width;
        this.height = height;
        
        //Start the game.
        start();
    }
    
    /**
     * Will chomp (remove from play) the tile at the given coordinates,
     * along with all other tiles above and to the right of it.
     * @param x the x position to chomp from.
     * @param y the y position to chomp from.
     * @throws IllegalMoveException
     */
    public void chompTile(int x, int y) {
        
        verifyMove(x, y);
        
        //Perform a chomp on all tiles above and to the right of the given location.
        for(int xx = x; xx < width; xx++) {
            for(int yy = y; yy < height; yy++) {
                
                //For each tile which isn't already chomped.
                if(!chomped[xx][yy]) {
                    
                    //Set the tile to chomped and increment the chomp counter.
                    chomped[xx][yy] = true;
                    numChomped++;
                    
                    //Recolour the tile to indicate that it was chomped by the current player.
                    board.setColour(xx, yy, (xx + yy) % 2 == 0 ?
                            COLOURS[currentPlayerId - 1].lighten(0.1F) :
                            COLOURS[currentPlayerId - 1]);
                }
            }
        }
        checkLose();
        turnTaken = true;
    }
    
    /**
     * Returns whether the tile at the given grid position is chomped.
     * @param x the x position to check.
     * @param y the y position to check.
     * @return true if this tile is chomped, false otherwise.
     */
    public boolean isTileChomped(int x, int y) { return chomped[x][y]; }
    
    @Override
    protected void init() {
        
        //Create board and window.
        board = new ChessBoard(width, height, TITLE);
        chomped = new boolean[width][height];
        
        //Create a poison marker on the bottom-left tile.
        Tile poison = new Tile(board.getWindow());
        poison.setSize(ChessBoard.TILE_SIZE, ChessBoard.TILE_SIZE);
        poison.setTexture(Texture.getTexture("res/misc/poison.png"));
        poison.setColour(Colour.WHITE);
        board.setPosition(poison, 0, 0);
    }
    
    @Override
    protected void setupTurn() {
        
        turnTaken = false;
        
        //Set the title to indicate the players turn.
        board.getWindow().setTitle(TITLE + " - " + currentPlayer.getName()
                + "'s Turn (" + COLOUR_NAMES[currentPlayerId - 1] + ")");
    }
    
    @Override
    protected void verifyTurn() {
        
        //Ensure the player completed their turn.
        if(!turnTaken && board.getWindow().isOpen())
            throw new IllegalMoveException("Player did not complete turn.");
    }
    
    @Override
    protected boolean isRunning() {
        return winnerId == -1 && board.getWindow().isOpen();
    }
    
    @Override
    protected void onFinish() {
        
        //Display the loser of the game.
        //Note: The player marked as the winner is actually the loser in this implementation.
        if(winnerId != -1) {
            board.getWindow().setTitle(TITLE + " - " + players[winnerId - 1].getName()
                    + " (" + COLOUR_NAMES[winnerId - 1] + ") has lost!");
        }
    }
    
    /**
     * Verifies that a move is legitimate, throwing an exception if it isn't.
     * @param x the x coordinate of the chomp.
     * @param y the y coordinate of the chomp.
     * @throws IllegalMoveException
     */
    private void verifyMove(int x, int y) {
        
        //Ensure only one chomp is performed per turn.
        if(turnTaken)
            throw new IllegalMoveException("Can't chomp multiple pieces.");
        
        //Ensure chomp location is in bounds.
        if(x < 0 || x >= width || y < 0 || y >= width)
            throw new IllegalMoveException("Location out of bounds.");
        
        //Ensure chomp location isn't already chomped.
        if(chomped[x][y])
            throw new IllegalMoveException("Tile has already been chomped.");
    }
    
    /**
     * Checks if a player has lost (there are no more available tiles).<br>
     * Will set the loser and end the game if this is so.
     */
    private void checkLose() {
        
        //Note: The player marked as the winner is actually the loser in this implementation.
        
        //The player loses if they chomp the poison tile.
        if(chomped[0][0]) {
            winnerId = currentPlayerId;
            
        //The player loses if there are no available tiles to chomp.
        } else if(numChomped == width * height - 1) {
            winnerId = currentPlayerId % players.length + 1;
        }
    }
    
    /**
     * Implementation of Player<Chomp> for use in inserting a human-controlled player.<br>
     * Each ChompController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class ChompController implements Player<Chomp> {
        
        /** The display name of this player. */
        private String name = "Controller";
        
        /**
         * Constructs a new ChompController with the default name of "Controller".
         */
        public ChompController() {}
        
        /** 
         * Constructs a new ChompController with the given name.
         * @param name the display name of this controller.
         */
        public ChompController(String name) { this.name = name; }
        
        @Override
        public void init(Chomp game, int playerId) {
            
            //Add a click listener to each grid cell on the board.
            for(int i = 0; i < game.width; i++) {
                for(int j = 0; j < game.width; j++) {
                    
                    int x = i, y = j;
                    game.board.addListener(x, y, () -> {
                        
                        //Listeners should only be active on your turn.
                        if(playerId != game.currentPlayerId)
                            return;
                        
                        try {
                            //Attempt to chomp this tile.
                            game.chompTile(x, y);
                        //Invalid moves should be ignored.
                        } catch(IllegalMoveException e) {}
                    });
                }
            }
        }

        @Override
        public void takeTurn(Chomp game, int playerId) {
            //Wait until the turn is complete before returning control to the game.
            //Actual logic is handled asynchronously by the above button listeners.
            while(!game.turnTaken && game.board.getWindow().isOpen()) {}
        }
        
        @Override
        public String getName() { return name; }
    }
}