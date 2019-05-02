package games.util;

import java.util.Optional;

/**
 * Abstract supertype for many abstract board games.<br>
 * Takes care of turn order progression.
 * 
 * @author Alec Dorrington
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Game {
    
    /** Array of all players participating in this game. */
    protected Player[] players;
    /** The winner of the game. Will be present at game completion. */
    protected Optional<Player> winner = Optional.empty();
    
    /** The player whose turn it currently is. */
    protected Player currentPlayer;
    /** The ID of the player whose turn it currently is.<br>
     *  Starts at 1 for the first player and increases with turn order. */
    protected volatile int currentPlayerId = 1;
    
    /**
     * Constructs a new game with the given players, given in turn order.<br>
     * Must subsequently call 'start()' to begin the game.
     * @param players the players (in turn order) participating in this game.
     */
    protected Game(Player[] players) { this.players = players; }
    
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
                while(isRunning()) {
                    
                    //Determine who the current player is.
                    currentPlayer = players[currentPlayerId - 1];
                    
                    //Have the current player take their turn.
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
    protected void verifyTurn() {}
    
    /**
     * To be called by the game before each turn to check if the game should be continued.
     * @return whether the game should continue.
     */
    protected boolean isRunning() { return !winner.isPresent(); }
    
    /**
     * To be called by the game after the game has finished.<br>
     * Implementations of this may wish to display the winner to the user.
     */
    protected void onFinish() {}
}