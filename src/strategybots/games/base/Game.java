package strategybots.games.base;

import strategybots.games.base.State.*;

public abstract class Game<G extends Game<G>> {
    
    private volatile State<G> state;
    
    private Player<G>[] players;
    
    private Board board;
    
    private volatile boolean running = false;
    
    public int getWidth() { return board.getWidth(); }
    
    public int getHeight() { return board.getHeight(); }
    
    public Player<G> getPlayer(int id) { return players[id-1]; }
    
    public int getNumPlayers() { return players.length; }
    
    public State<G> getState() { return state; }
    
    public Board getBoard() { return board; }
    
    public boolean isRunning() { return running; }
    
    protected void setBoard(Board board) {
        this.board = board;
    }
    
    @SuppressWarnings("unchecked")
    protected void setPlayers(Player<G>... players) {
        
        this.players = players;
        for(int i = 0; i < players.length; i++) {
            players[i].playerId = i+1;
            players[i].init(this, i+1);
        }
    }
    
    protected void setState(State<G> state) {
        this.state = state;
    }
    
    protected void start() {
        
        running = true;
        new Thread(this::run).start();
        board.setState(state);
        board.show();
        running = false;
    }
    
    private void run() {
        
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
        if(state.getWinner().isPresent()) {
            System.out.println("Winner: " + state.getWinner().get()
                    + ", P" + state.getWinner().get().getPlayerId());
        }
        running = false;
    }
    
    public static abstract class Player<G extends Game<G>> {
        
        private int playerId;
        public int getPlayerId() { return playerId; }
        
        protected abstract void init(Game<G> game, int playerId);
        
        protected abstract Action<G> getAction(
            Game<G> game, State<G> state, int playerId);
        
        protected abstract void destroy(Game<G> game, int playerId);
        
        @Override
        public String toString() { return "Player"; }
    }
    
    public static abstract class Controller
            <G extends Game<G>> extends Player<G> {
        
        private volatile Action<G> action;
        
        @Override
        protected void init(Game<G> game, int playerId) {
            
            game.getBoard().onClick((x, y) -> {
                State<G> state = game.getState();
                
                if(state.getCurrentPlayerId() == playerId && game.running) {
                    while(action != null && game.running) {}
                    action = onClick(game, state, x, y, playerId);
                }
            });
        }
        
        @Override
        protected Action<G> getAction(Game<G> game,
                State<G> state, int playerId) {
            
            action = null;
            while(action == null && game.running) {}
            return game.isRunning() ? action : new None<>();
        }
        
        @Override
        protected void destroy(Game<G> game, int playerId) {}
        
        protected abstract Action<G> onClick(Game<G> game,
                State<G> state, int x, int y, int playerId);
        
        @Override
        public String toString() { return "Controller"; }
    }
}