package games;

import games.util.ChessBoard;
import games.util.GridGame;
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
public class Chomp extends GridGame {
    
    /** Title of the window. */
    private static final String TITLE = "Chomp";
    
    /** Stores whether the tile at each position has been chomped. */
    private boolean[][] chomped;
    /** The running total number of tiles which have been chomped. */
    private int numChomped = 0;
    
    /**
     * Asynchronously runs a new Chomp instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param players the players who are to participate.
     */
    public Chomp(int width, int height, Player<Chomp> player1, Player<Chomp> player2) {
        super(width, height, TITLE, player1, player2);
    }
    
    /**
     * Will chomp (remove from play) the tile at the given coordinates,
     * along with all other tiles above and to the right of it.
     * @param x the x position to chomp from.
     * @param y the y position to chomp from.
     * @throws IllegalMoveException
     */
    public void chompTile(int x, int y) {
        
        validateMove(x, y);
        
        //Perform a chomp on all tiles above and to the right of the given location.
        for(int xx = x; xx < getWidth(); xx++) {
            for(int yy = y; yy < getHeight(); yy++) {
                
                //For each tile which isn't already chomped.
                if(!chomped[xx][yy]) {
                    
                    //Set the tile to chomped and increment the chomp counter.
                    chomped[xx][yy] = true;
                    numChomped++;
                    
                    //Recolour the tile to indicate that it was chomped by the current player.
                    getBoard().setColour(xx, yy, (xx + yy) % 2 == 0 ?
                            COLOURS[getCurrentPlayerId() - 1].lighten(0.1F) :
                            COLOURS[getCurrentPlayerId() - 1]);
                }
            }
        }
        setTurnTaken();
    }
    
    /**
     * Verifies that a move is legitimate, throwing an exception if it isn't.
     * @param x the x coordinate of the chomp.
     * @param y the y coordinate of the chomp.
     * @throws IllegalMoveException
     */
    @Override
    protected void validateMove(int x, int y) {
        
        validateMove(x, y);
        
        //Ensure chomp location isn't already chomped.
        if(chomped[x][y])
            throw new IllegalMoveException("Tile has already been chomped.");
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
        
        chomped = new boolean[getWidth()][getHeight()];
        
        //Create a poison marker on the bottom-left tile.
        Tile poison = new Tile(getWindow());
        poison.setSize(ChessBoard.TILE_SIZE, ChessBoard.TILE_SIZE);
        poison.setTexture(Texture.getTexture("res/misc/poison.png"));
        poison.setColour(Colour.WHITE);
        getBoard().setPosition(poison, 0, 0);
    }
    
    @Override
    protected void checkWin() {
        
        //The player loses if they chomp the poison tile.
        if(chomped[0][0]) {
            endGame(getCurrentPlayerId() % 2 + 1);
            
        //The player wins if there are no available tiles to chomp.
        } else if(numChomped == getWidth() * getHeight() - 1) {
            endGame(getCurrentPlayerId());
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
            game.getBoard().addListenerToAll((x, y) -> {
                
                //Listeners should only be active on your turn.
                if(playerId != game.getCurrentPlayerId() || !game.isRunning())
                    return;
                
                try {
                    //Attempt to chomp this tile.
                    game.chompTile(x, y);
                //Invalid moves should be ignored.
                } catch(IllegalMoveException e) {}
            });
        }

        @Override
        public void takeTurn(Chomp game, int playerId) {
            //Wait until the turn is complete before returning control to the game.
            //Actual logic is handled asynchronously by the above button listeners.
            while(!game.turnTaken() && game.getWindow().isOpen()) {}
        }
        
        @Override
        public String getName() { return name; }
    }
}