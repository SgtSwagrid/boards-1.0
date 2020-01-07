package strategybots.games.base;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import strategybots.games.base.Game.Player;
import swagui.layouts.Layout.Fill;
import swagui.tiles.Tile;

/**
 * 
 * @author Alec Dorrington
 * @param <G> the game with which this state is associated.
 */
public abstract class State<G extends Game<G>>
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 3297959735556718290L;
    
    private G game;

    private Piece<G>[][] pieces;
    
    private Player<G> currentPlayer;
    
    private State<G> previousState, turnStart;
    private Action<G> latestAction;
    
    private boolean valid = true;
    
    public State<G> takeAction(Action<G> action) {
        
        State<G> state = clone();
        state.valid = valid && action.validate(this);
        action.apply(state);
        state.previousState = this;
        state.latestAction = action;
        return state;
    }
    
    public G getGame() { return game; }
    
    public Optional<Piece<G>> getPiece(int x, int y) {
        return Optional.ofNullable(pieces[x][y]);
    }
    
    public Piece<G>[][] getBoardAsArray() { return copyBoard(pieces); }
    
    public Player<G> getCurrentPlayer() { return currentPlayer; }
    
    public State<G> getPreviousState() { return previousState; }
    
    public Action<G> getLatestAction() { return latestAction; }
    
    public State<G> getTurnStart() { return turnStart; }
    
    public State<G> getPreviousTurnStart() {
        return getTurnStart().getPreviousState().getTurnStart();
    }
    
    public List<Action<G>> getActionsSince(State<G> state) {
        
        if(state != this) {
            List<Action<G>> actions = getPreviousState().getActionsSince(state);
            actions.add(getLatestAction());
            return actions;
        } else return new LinkedList<>();
    }
    
    public List<Action<G>> getTurnActions() {
        return getActionsSince(getTurnStart());
    }
    
    public List<Action<G>> getPreviousTurnActions() {
        return getTurnStart().getActionsSince(getPreviousTurnStart());
    }
    
    public int getNumActions() { return getTurnActions().size(); }
    
    public boolean isValid() { return valid; }
    
    public void forEachSquare(BiConsumer<Integer, Integer> action) {
        
        IntStream.range(0, game.getBoard().getWidth()).forEach(x ->
            IntStream.range(0, game.getBoard().getHeight()).forEach(y ->
                action.accept(x, y)));
    }
    
    public abstract boolean validateAction(Action<G> action);
    
    public abstract List<Action<G>> getActions();
    
    protected void endTurn() {
        int currentPlayerId = currentPlayer.getPlayerId()%game.getNumPlayers()+1;
        currentPlayer = game.getPlayer(currentPlayerId);
    }
    
    @SuppressWarnings("unchecked")
    private Piece<G>[][] copyBoard(Piece<G>[][] board) {
        
        Piece<G>[][] newBoard = new Piece
                [getGame().getWidth()][getGame().getHeight()];
        
        for(int x = 0; x < getGame().getWidth(); x++) {
            for(int y = 0; y < getGame().getHeight(); y++) {
                
                newBoard[x][y] = board[x][y] != null ?
                        (Piece<G>)board[x][y].clone() : null;
            }
        }
        return newBoard;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<G> clone() {
        
        State<G> state = null;
        try {
            state = (State<G>) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        state.pieces = copyBoard(pieces);
        return state;
    }
    
    public static abstract class Piece<G extends Game<G>>
            implements Serializable, Cloneable {
        
        private static final long serialVersionUID = 8025641217206456968L;

        private Player<G> owner;
        
        private int x, y;
        
        private Tile tile;
        
        protected Piece(Player<G> owner,
                int x, int y, Tile tile) {
            
            this.owner = owner;
            this.x = x;
            this.y = y;
            this.tile = tile
                .setFill(Fill.FILL_PARENT);
        }
        
        public Player<G> getOwner() { return owner; }
        
        public int getX() { return x; }
        
        public int getY() { return y; }
        
        public Tile getTile() { return tile; }
        
        protected void move(State<G> state, int xTo, int yTo) {
            state.pieces[x][y] = null;
            state.pieces[xTo][yTo] = this;
            x = xTo; y = yTo;
        }
        
        protected void delete(State<G> state) {
            state.pieces[x][y] = null;
        }
        
        protected abstract boolean validatePlace(int x, int y);
        
        protected abstract boolean validateMove(int x, int y);
        
        @Override
        @SuppressWarnings("unchecked")
        protected Piece<G> clone() {
            
            Piece<G> piece = null;
            try {
                piece = (Piece<G>)super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return piece;
        }
    }
    
    public static abstract class Action
            <G extends Game<G>> implements Serializable {
        
        private static final long serialVersionUID = -2151905001120677722L;

        protected abstract boolean validate(State<G> state);
        
        protected abstract void apply(State<G> state);
        
        //protected final void movePiece()
        
        protected final void endTurn(State<G> state) { state.endTurn(); }
    }
    
    protected static class Place<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = 5586739554752729927L;
        
        @Override
        protected boolean validate(State<G> state) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        protected void apply(State<G> state) {
            // TODO Auto-generated method stub
            
        }
    }
    
    protected static class Move<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = -5142240456199165791L;
        
        private Piece<G> piece;
        
        private int xFrom, yFrom;
        
        private int xTo, yTo;
        
        protected Move(State<G> state, int xFrom, int yFrom, int xTo, int yTo) {
            
            this.piece = state.getPiece(xFrom, yFrom).orElseGet(null);
            this.xFrom = xFrom; this.yFrom = yFrom;
            this.xTo = xTo; this.yTo = yTo;
        }
        
        protected Move(Piece<G> piece, int xTo, int yTo) {
            
            this.piece = piece;
            xFrom = piece.getX(); yFrom = piece.getY();
            this.xTo = xTo; this.yTo = yTo;
        }
        
        public Piece<G> getPiece() { return piece; }
        
        public int getXFrom() { return xFrom; }
        
        public int getYFrom() { return yFrom; }
        
        public int getXTo() { return xTo; }
        
        public int getYTo() { return yTo; }
        
        @Override
        protected boolean validate(State<G> state) {
            
            int width = state.getGame().getWidth();
            int height = state.getGame().getHeight();
            
            //Ensure piece is owned by the current player.
            if(!(piece.getOwner() == state.getCurrentPlayer())) return false;
            
            //Ensure piece is moved somewhere else.
            if(xTo == xFrom && yTo == yFrom) return false;
            
            //Ensure source position is in bounds.
            if(xFrom < 0 || xFrom >= width || yFrom < 0
                    || yFrom >= height) return false;
            
            //Ensure destination position is in bounds.
            if(xTo < 0 || xTo >= width || yTo < 0
                    || yTo >= height) return false;
            
            //Ensure specific piece is able to make the move.
            if(!piece.validateMove(xTo, yTo)) return false;
            
            return true;
        }

        @Override
        protected void apply(State<G> state) {
            piece.move(state, xTo, yTo);
        }
    }
}