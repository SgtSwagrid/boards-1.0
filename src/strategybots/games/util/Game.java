package strategybots.games.util;

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
    private volatile boolean running = false;
    
    /** Indication of the completion of the current players' turn. */
    private volatile boolean turnDone;
    
    /** The maximum amount of time allocated per turn, in milliseconds. */
    private volatile long timeLimit = -1;
    
    /**
     * Constructs a new game with the given players, given in turn order.<br>
     * Must subsequently call 'start()' to begin the game.
     * @param players the players (in turn order) participating in this game.
     */
    protected Game(Player... players) { this.players = players; }
    
    /**
     * @return the number of players participating in this game.
     */
    public int getNumPlayers() { return players.length; }
    
    /**
     * @return the ID of the player whose turn it currently is.
     */
    public int getCurrentPlayerId() { return  currentPlayerId; }
    
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
     * @return whether the current turn has yet been completed.
     */
    public boolean turnDone() { return turnDone; }
    
    /**
     * Set the amount of time allocated for each player per turn.<br>
     * Use a value of -1 to disable time limits (default).<br>
     * Should the time limit expire, the game will be forfeit.
     * @param timeLimit the allocated per-turn time, in milliseconds.
     */
    public void setTimeLimit(long timeLimit) { this.timeLimit = timeLimit; }
    
    /**
     * Declare the current turn as having been completed.
     */
    protected void endTurn() { turnDone = true; }
    
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
     * @return the array of players (no copy is made).
     */
    protected Player[] getPlayers() { return players; }
    
    /**
     * Skips the next players turn.
     */
    protected final void skipTurn() {
        currentPlayerId = currentPlayerId % players.length + 1;
        currentPlayer = players[currentPlayerId - 1];
    }
    
    /**
     * Ends the game after the current turn, setting the winner.
     * @param winnerId the ID of the winner of the game.
     */
    protected final void endGame(int winnerId) {
        endTurn();
        if(winnerId > 0 && winnerId <= players.length) {
            this.winnerId = winnerId;
            winner = Optional.of(players[winnerId - 1]);
        }
        running = false;
    }
    
    /**
     * Starts the game in a new thread.
     */
    protected void start() {
        
        //Run the game in a new thread.
        new Thread(() -> {
            
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
                turnDone = false;
                preTurn();
                
                //Record the time at which the turn began.
                long startTime = System.currentTimeMillis();
                
                //Prompt the player to take a turn, in a new thread.
                new Thread(() -> {
                    
                    //Wait for the turn to be completed.
                    while(!turnDone && isRunning()) {
                        //Forfeit the game if the time runs out.
                        if(timeLimit > 0 && System.currentTimeMillis()-startTime > timeLimit) {
                            endGame(getCurrentPlayerId()%2+1);
                        }
                    }
                }, "Timer").start();
                
                //Continue to call takeTurn() until the turn is complete.
                while(!turnDone && isRunning()) {
                    currentPlayer.takeTurn(Game.this, currentPlayerId);
                }
                
                //Wait for the turn to be completed.
                while(!turnDone && isRunning()) {
                    //Forfeit the game if the time runs out.
                    if(timeLimit > 0 && System.currentTimeMillis()-startTime > timeLimit) {
                        endGame(getCurrentPlayerId()%2+1);
                    }
                }
                
                postTurn();
                checkEnd();
                
                //Increment the current player.
                currentPlayerId = currentPlayerId % players.length + 1;
            }
            onFinish();
            for(int i = 0; i < players.length; i++) {
                players[i].gameEnd(Game.this, i+1, winnerId);
            }
        }, "Game").start();
    }
    
    /**
     * To be called by the game upon initialisation.<br>
     * Implementations of this may wish to create a board or otherwise prepare the game.
     */
    protected void init() {}
    
    /**
     * To be called by the game before each turn is taken.<br>
     */
    protected void preTurn() {}
    
    /**
     * To be called by the game after each turn is taken.<br>
     * Implementations of this may wish to verify the turn taken.
     */
    protected void postTurn() {}
    
    /**
     * To be called upon the completion of each turn to check if any player has won.
     */
    protected void checkEnd() {}
    
    /**
     * To be called by the game after the game has finished.<br>
     * Implementations of this may wish to display the winner to the user.
     */
    protected void onFinish() {}
    
    /**
     * Supertype for all player implementations.
     * @author Alec Dorrington
     * @param <G> the game which this player plays.
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
         * Called once at the completion of the game.
         * @param game the game being played.
         * @param playerId the ID of this player.
         * @param winnerId the ID of the winning player.
         */
        default void gameEnd(G game, int playerId, int winnerId) {}
        
        /**
         * Called to determine the name of this player.<br>
         * Used only for display purposes.
         * @return the player name (defaults to "Bot").
         */
        default String getName() { return "Bot"; }
    }
}