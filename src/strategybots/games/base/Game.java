package strategybots.games.base;

import strategybots.games.base.State.*;

/**
 * Base class for games played on a rectangular board.<br>
 * Manages IO and turn order progression.
 * 
 * @author Alec Dorrington
 *
 * @param <G> the game implementation itself.
 */
public abstract class Game<G extends Game<G>> {
    
    private volatile State<G> state;
    /** @return The current state of the game. */
    public State<G> getState() { return state; }
    
    private Player<G>[] players;
    /** @return the player with the given ID. */
    public Player<G> getPlayer(int id) { return players[id-1]; }
    /** @return the number of players participating in the game. */
    public int getNumPlayers() { return players.length; }
    
    private Board board;
    /** @return The board on which the game is played. */
    public Board getBoard() { return board; }
    
    private volatile boolean running = false;
    /** @return Whether the game is currently in progress. */
    public boolean isRunning() { return running; }
    
    /**
     * Start the game.
     * @param board the board on which the game is played.
     * @param state the initial state of the game.
     * @param players the players participating in the game.
     */
    @SuppressWarnings("unchecked")
    protected void start(Board board, State<G> state, Player<G>... players) {
        
        //Set the board.
        this.board = board;
        
        //Set and initialize the players.
        this.players = players;
        for(int i = 0; i < players.length; i++) {
            players[i].playerId = i+1;
            players[i].init(this, i+1);
        }
        
        //Set the state.
        this.state = state;
        
        //Run the game.
        running = true;
        new Thread(this::run).start();
        board.setState(state);
        board.show();
        running = false;
        
        //Finalize players.
        for(int i = 0; i < players.length; i++) {
            players[i].destroy(this, i+1);
        }
    }
    
    /**
     * Handle turn order progression, continuing to query
     * players for their actions until the game is complete.
     */
    private void run() {
        
        //While the game hasn't ended.
        while(running && !state.isTerminal()) {
            
            int turn = state.getTurnNum();
            
            //Query the current player for the next action.
            Player<G> player = state.getCurrentPlayer();
            Action<G> action = player.getAction(this, state, player.getPlayerId());
            State<G> newState = state.takeAction(action);
            
            //Update the state if it is valid and...
            if(newState.isValid() &&
                    //...Turn is not yet complete or...
                    (newState.getTurnNum() == turn ||
                    //...Turn is complete and next turn hasn't started.
                    (newState.getTurnNum() == turn+1 &&
                        newState.getNumActions() == 0))) {
                
                state = newState;
                board.setState(newState);
            }
        }
        //Print the winner to the console (temporary).
        if(state.getWinner().isPresent()) {
            System.out.println("Winner: " + state.getWinner().get()
                    + ", P" + state.getWinner().get().getPlayerId());
        }
        running = false;
    }
    
    /**
     * Base class for player implementations,
     * for use by bot implementations as well as player controllers.
     * 
     * @author Alec Dorrington
     *
     * @param <G> the game played by the player.
     */
    public static abstract class Player<G extends Game<G>> {
        
        private int playerId;
        /** @return the turn order ID of the player. */
        public int getPlayerId() { return playerId; }
        
        /**
         * Called once by the game upon initialization.<br>
         * Used to handle any player-specific initialization.
         * @param game being played by the player.
         * @param playerId the turn order ID of the player.
         */
        protected abstract void init(Game<G> game, int playerId);
        
        /**
         * Called by the game whenever the player is required to take an action.<br>
         * Will be continuously called until the player has ended their turn.<br>
         * This is where bot strategy should be implemented.
         * @param game being played by the player.
         * @param state of the game.
         * @param playerId the turn order ID of the player.
         * @return the action(s) the player wishes to take.
         */
        protected abstract Action<G> getAction(
            Game<G> game, State<G> state, int playerId);
        
        /**
         * Called once when the game ends.
         * @param game being played by the player.
         * @param playerId the turn order ID of the player.
         */
        protected abstract void destroy(Game<G> game, int playerId);
        
        @Override
        public String toString() { return "Player"; }
    }
    
    /**
     * Base class for user-controlled players.
     * 
     * @author Alec Dorrington
     *
     * @param <G> the game played by the controller.
     */
    public static abstract class Controller
            <G extends Game<G>> extends Player<G> {
        
        /** Most recent action trigged by user input. */
        private volatile Action<G> action;
        
        @Override
        protected void init(Game<G> game, int playerId) {
            
            game.getBoard().onClick((x, y) -> {
                State<G> state = game.getState();
                
                //Query the controller for an action when the board is clicked.
                if(state.getCurrentPlayerId() == playerId && game.running) {
                    while(action != null && game.running) {}
                    action = onClick(game, state, x, y, playerId);
                }
            });
        }
        
        @Override
        protected Action<G> getAction(Game<G> game,
                State<G> state, int playerId) {
            
            //Wait until user input triggers an action, and return it.
            action = null;
            while(action == null && game.running) {}
            return game.isRunning() ? action : new None<>();
        }
        
        @Override
        protected void destroy(Game<G> game, int playerId) {}
        
        /**
         * Called once whenever the board is clicked.
         * @param game being played by the controller.
         * @param state of the game.
         * @param x coordinate of grid square which was clicked.
         * @param y coordinate of grid square which was clicked.
         * @param playerId the turn order ID of the player.
         * @return the action(s) the player wishes to take, if any.
         */
        protected abstract Action<G> onClick(Game<G> game,
                State<G> state, int x, int y, int playerId);
        
        @Override
        public String toString() { return "Controller"; }
    }
}