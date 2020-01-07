package strategybots.games.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import strategybots.games.base.Game.Player;
import swagui.tiles.Tile;

public abstract class State2<G extends Game<G>>
        implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 466853639779737105L;
    
    private G game;
    public G getGame() { return game; }
    
    private Player<G> currentPlayer;
    public Player<G> getCurrentPlayer() { return currentPlayer; }
    
    private int currentPlayerId;
    public int getCurrentPlayerId() { return currentPlayerId; }
    
    private State2<G> previousState;
    public State2<G> getPreviousState() { return previousState; }
    
    private Action<G> latestAction;
    public Action<G> getLatestAction() { return latestAction; }
    
    private boolean valid = true;
    public boolean isValid() { return valid; }
    
    private Piece<G>[][] board;
    private Map<Player<G>, List<Piece<G>>> pieces = new HashMap<>();
    
    public Optional<Piece<G>> getPiece(int x, int y) {
        return Optional.ofNullable(board[x][y]);
    }
    
    public List<Piece<G>> getPieces(Player<G> owner) {
        return Collections.unmodifiableList(pieces.get(owner));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State2<G> clone() {
        
        State2<G> state = null;
        try {
            state = (State2<G>) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        //state.pieces = copyBoard(pieces);
        return state;
    }
    
    public static abstract class Piece<G extends Game<G>>
            implements Serializable, Cloneable {
        
        private static final long serialVersionUID = 6302456919166619709L;
        
        private Player<G> owner;
        public Player<G> getOwner() { return owner; }
        
        private int x, y;
        public int getX() { return x; }
        public int getY() { return y; }
        
        private Tile tile;
        public Tile getTile() { return tile; }
        
        public Piece(Player<G> owner, int x, int y, Tile tile) {
            this.owner = owner;
            this.x = x; this.y = y;
            this.tile = tile;
        }
        
        protected abstract boolean validatePlace
                (State<G> state, int x, int y);
        
        protected abstract boolean validateMove
                (State<G> state, int xTo, int yTo);
    }
    
    public static abstract class Action<G extends Game<G>>
            implements Serializable, Cloneable {
        
        private static final long serialVersionUID = -4793558180828857910L;
        
        protected State2<G> getSuccessor(State2<G> state) {
            
            State2<G> newState = state.clone();
            newState.previousState = state;
            newState.latestAction = this;
            return newState;
        }
        
        protected abstract State<G> apply(State2<G> state);
        
        protected abstract boolean validate(State2<G> state);
    }
    
    public static abstract class Move<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = -7578053215151752299L;
        
        private Piece<G> piece;
        public Piece<G> getPiece() { return piece; }
        
        public int xTo, yTo;
        public int getXTo() { return xTo; }
        public int getYTo() { return yTo; }
        
        public Move(Piece<G> piece, int xTo, int yTo) {
            this.piece = piece;
            this.xTo = xTo; this.yTo = yTo;
        }
        
        @Override
        protected State<G> apply(State2<G> state) {
            
        }
        
        @Override
        protected boolean validate(State2<G> state) {
            
        }
    }
}