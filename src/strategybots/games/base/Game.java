package strategybots.games.base;

import java.util.Optional;

import strategybots.games.base.State.Action;
import strategybots.games.base.State.Piece;

public abstract class Game<G extends Game<G>> {
    
    private State<G> state;
    
    private Player<G>[] players;
    
    private Board board;
    
    private boolean running = false;
    
    protected Game(State<G> state, Player<G>[] players, Board board) {
        this.state = state;
        this.players = players;
        this.board = board;
    }
    
    public int getWidth() { return board.getWidth(); }
    
    public int getHeight() { return board.getHeight(); }
    
    public Player<G> getPlayer(int id) { return players[id-1]; }
    
    public int getNumPlayers() { return players.length; }
    
    public State<G> getState() { return state; }
    
    public Board getBoard() { return board; }
    
    public boolean isRunning() { return running; }
    
    @SuppressWarnings("unchecked")
    public void start() {
        
        for(int i = 0; i < players.length; i++) {
            players[i].game = (G) this;
            players[i].playerId = i+1;
            players[i].init();
        }
        running = true;
        new Thread(this::run).start();
        board.show();
        running = false;
    }
    
    private void run() {
        
        while(running) {
            
            state = state.takeAction(state.getCurrentPlayer().getAction(state));
            if(!state.isValid()) {} //TODO
            board.setState(state);
            
        }
    }
    
    public static abstract class Player<G extends Game<G>> {
        
        private G game;
        
        private int playerId;
        
        private String name;
        
        public final G getGame() { return game; }
        
        public final int getPlayerId() { return playerId; }
        
        protected abstract void init();
        
        protected abstract Action<G> getAction(State<G> state);
        
        protected abstract void destroy();
        
        protected final void setName(String name) { this.name = name; }
        
        @Override
        public String toString() { return name; }
    }
    
    public static abstract class Controller
            <G extends Game<G>> extends Player<G> {
        
        private Piece<G> selected;
        
        @Override
        protected void init() {
            setName("Controller");
            getGame().getBoard().onClick((x, y) -> {
                if(getGame().getState().getCurrentPlayer() == this) {
                    
                }
            });
        }
        
        @Override
        protected Action<G> getAction(State<G> state) {
            
            return null;
        }
        
        protected abstract void onClick(State<G> state, int x, int y);
        
        protected Optional<Piece<G>> getSelectedPiece() {
            return Optional.ofNullable(selected);
        }
    }
}