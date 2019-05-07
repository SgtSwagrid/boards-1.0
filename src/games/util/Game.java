package games.util;

import java.util.Optional;

import swagui.api.Colour;

/**
 * Abstract supertype for many abstract board games.<br>
 * Takes care of turn order progression.
 * 
 * @author Alec Dorrington
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Game {
    
    /** List of player colours. */
    private static final Colour[] COLOURS = new Colour[] {
             Colour.rgb(87, 95, 207),
             Colour.rgb(255, 94, 87),
             Colour.rgb(5, 196, 107),
             Colour.rgb(255, 211, 42)
    };
    
    /** List of colour names. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Blue", "Red", "Green", "Yellow"
    };
    
    /** Array of all players participating in this game. */
    private Player[] players;
    
    /** The player whose turn it currently is. */
    private Player currentPlayer;
    /** The ID of the player whose turn it currently is.<br>
     *  Starts at 1 for the first player and increases with turn order. */
    private volatile int currentPlayerId = 1;
    
    /** The winner of the game. Will be empty until game completion. */
    private Optional<Player> winner = Optional.empty();
    /** The ID of the winner. */
    private int winnerId = -1;
    
    /** Whether the game is currently in progress. */
    private boolean running = false;
    
    /** Indication of the completion of the current players' turn. */
    private boolean turnTaken;
    
    /**
     * Constructs a new game with the given players, given in turn order.<br>
     * Must subsequently call 'start()' to begin the game.
     * @param players the players (in turn order) participating in this game.
     */
    protected Game(Player... players) {
        
        //There can't be more players than there are colours.
        if(players.length > COLOURS.length)
            throw new IllegalArgumentException("Max players: " + COLOURS.length);
        
        this.players = players;
    }
    
    /**
     * @return the number of players participating in this game.
     */
    public int getNumPlayers() { return players.length; }
    
    /**
     * @return the ID of the player whose turn it currently is.
     */
    public int getCurrentPlayerId() { return currentPlayerId; }
    
    /**
     * @return the player who won, if such a player exists.
     */
    public Optional<Player> getWinner() { return winner; }
    
    /**
     * @return the ID of the player who won, or -1 if the game is in progess.
     */
    public int getWinnerId() { return winnerId; }
    
    /**
     * @return whether the game is still running.
     */
    public boolean isRunning() { return running; }
    
    /**
     * @param playerId the ID of the player to get (starting at 1).
     * @return the player of the given ID.
     */
    protected Player getPlayer(int playerId) { return players[playerId - 1]; }
    
    /**
     * @return the player whose turn it currently is.
     */
    protected Player getCurrentPlayer() { return currentPlayer; }
    
    /**
     * Ends the game after the current turn, setting the winner.
     * @param winnerId the ID of the winner of the game.
     */
    protected void endGame(int winnerId) {
        this.winnerId = winnerId;
        winner = Optional.of(players[winnerId - 1]);
        running = false;
    }
    
    /**
     * Get the colour associated with the given player ID.
     * @param playerId the ID of the player to get the colour of.
     * @return the colour of the player.
     */
    protected Colour getColour(int playerId) {
        return COLOURS[playerId - 1];
    }
    
    /**
     * Get the name of the colour associated with the given player ID.
     * @param playerId the ID of the player to get the colour of.
     * @return the human-readable name of the colour of the player.
     */
    protected String getColourName(int playerId) {
        return COLOUR_NAMES[playerId - 1];
    }
    
    protected boolean turnTaken() { return turnTaken; }
    
    protected void setTurnTaken() { turnTaken = true; }
    
    /**
     * Starts the game in a new thread.
     */
    protected void start() {
        
        //Run the game in a new thread.
        new Thread("Game") {
            @Override public void run() {
                
                //Initialise the game.
                init();
                
                //Initialise the players.
                for(int i = 0; i < players.length; i++) {
                    players[i].init(Game.this, i + 1);
                }
                
                //While the game is running (hasn't been finished or quit).
                running = true;
                while(isRunning()) {
                    
                    //Determine who the current player is.
                    currentPlayer = players[currentPlayerId - 1];
                    
                    //Have the current player take their turn.
                    turnTaken = false;
                    setupTurn();
                    currentPlayer.takeTurn(Game.this, currentPlayerId);
                    verifyTurn();
                    
                    //Increment the current player.
                    currentPlayerId = currentPlayerId % players.length + 1;
                }
                onFinish();
            }
        }.start();
    }
    
    /**
     * To be called by the game upon initialisation.<br>
     * Implementations of this may wish to create a board or otherwise prepare the game.
     */
    protected void init() {}
    
    /**
     * To be called by the game before each turn is taken.<br>
     */
    protected void setupTurn() {}
    
    /**
     * To be called by the game after each turn is taken.<br>
     * Implementations of this may wish to verify the turn taken.
     */
    protected void verifyTurn() {
        //Ensure the player completed their turn.
        if(!turnTaken())
            throw new IllegalMoveException("Player did not complete turn.");
    }
    
    /**
     * To be called by the game after the game has finished.<br>
     * Implementations of this may wish to display the winner to the user.
     */
    protected void onFinish() {}
    
    /**
     * Supertype for all player implementations.
     * @author Alec Dorrington
     */
    public interface Player<G extends Game> {
        
        /**
         * Called once before the game begins.
         * @param game the game being played.
         * @param playerId the ID of this player.
         */
        default void init(G game, int playerId) {}
        
        /**
         * Called once whenever the player is expected to take a turn.<br>
         * Implementations of this must complete a whole turn as per the rules of the specific game.
         * @param game the game being played.
         * @param playerId the ID of this player.
         */
        void takeTurn(G game, int playerId);
        
        /**
         * Called to determine the name of this player.<br>
         * Used only for display purposes.
         * @return the player name (defaults to "Bot").
         */
        default String getName() { return "Bot"; }
    }
    
    /**
     * Exception to be thrown when a player makes a move which isn't valid.
     * @author Alec Dorrington
     */
    public static class IllegalMoveException extends RuntimeException {
        
        private static final long serialVersionUID = 7081468376529411598L;
        
        public IllegalMoveException() {}
        
        public IllegalMoveException(String message) { super(message); }
    }
}