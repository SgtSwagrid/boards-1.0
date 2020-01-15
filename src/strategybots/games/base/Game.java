package strategybots.games.base;

import java.util.Optional;
import java.util.function.BiConsumer;

import strategybots.games.base.State.*;
import swagui.graphics.Colour;

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
    
    public void forEachSquare(BiConsumer<Integer, Integer> action) {
        
        for(int x = 0; x < getWidth(); x++) {
            for(int y = 0; y < getHeight(); y++) {
                action.accept(x, y);
            }
        }
    }
    
    public boolean inBounds(int x, int y) {
        
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }
    
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
        
        while(running) {
            
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
                
                System.out.println("hello");
                state = newState;
                board.setState(newState);
            }
        }
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
        
        private Game<G> game;
        private Piece<G> selected;
        private volatile Action<G> action;
        
        @Override
        protected void init(Game<G> game, int playerId) {
            
            this.game = game;
            
            game.getBoard().onClick((x, y) -> {
                State<G> state = game.getState();
                
                if(state.getCurrentPlayerId() == playerId) {
                    while(action != null) {}
                    action = onClick(state, x, y, playerId);
                }
            });
        }
        
        @Override
        protected Action<G> getAction(Game<G> game,
                State<G> state, int playerId) {
            
            action = null;
            while(action == null && game.isRunning()) {}
            return game.isRunning() ? action : new None<>();
        }
        
        @Override
        protected void destroy(Game<G> game, int playerId) {}
        
        protected abstract Action<G> onClick(State<G> state,
                int x, int y, int playerId);
        
        protected void selectPiece(Piece<G> piece, Colour colour) {
            unselectPiece();
            selected = piece;
            game.board.setColour(piece.getX(), piece.getY(), colour);
        }
        
        protected void unselectPiece() {
            selected = null;
            game.board.resetColours();
        }
        
        protected Optional<Piece<G>> getSelectedPiece() {
            return Optional.ofNullable(selected);
        }
        
        @Override
        public String toString() { return "Controller"; }
    }
}