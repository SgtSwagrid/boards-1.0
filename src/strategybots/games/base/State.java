package strategybots.games.base;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
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
public abstract class State<G extends Game<G>> implements Serializable {

    private static final long serialVersionUID = 3297959735556718290L;
    
    private G game;

    private Piece<G>[][] pieces;
    
    private Player<G> currentPlayer;
    
    private State<G> previousState;
    private Action<G> latestAction;
    
    private boolean valid = true;
    
    protected State() {}
    
    protected State(State<G> previous, Action<G> action) {
        this.game = previous.game;
        this.pieces = copy(previous.pieces);
        this.currentPlayer = previous.currentPlayer;
        this.previousState = previous;
        this.latestAction = action;
    }
    
    public G getGame() { return game; }
    
    public Optional<Piece<G>> getPiece(int x, int y) {
        return Optional.ofNullable(pieces[x][y]);
    }
    
    public Player<G> getCurrentPlayer() { return currentPlayer; }
    
    public State<G> getPreviousState() { return previousState; }
    
    public Action<G> getLatestAction() { return latestAction; }
    
    public boolean isValid() { return valid; }
    
    public void forEachSquare(BiConsumer<Integer, Integer> action) {
        
        IntStream.range(0, game.getBoard().getWidth()).forEach(x ->
            IntStream.range(0, game.getBoard().getHeight()).forEach(y ->
                action.accept(x, y)));
    }
    
    public abstract State<G> takeAction(Action<G> action);
    
    public abstract boolean validateAction(Action<G> action);
    
    public abstract List<Action<G>> getActions();
    
    @Override
    public abstract State<G> clone();
    
    private <T> T[][] copy(T[][] array) {
        
        return Arrays.stream(array)
            .map(a -> a.clone())
            .toArray(s -> array.clone());
    }
    
    public static abstract class Piece<G extends Game<G>> {
        
        private Player<G> owner;
        
        private int x, y;
        
        private Tile tile;
        
        protected Piece(Player<G> owner, int x, int y, Tile tile) {
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
        
        public abstract boolean validatePlace(State<G> state, int x, int y);
        
        public abstract boolean validateMove(State<G> state, int x, int y);
    }
    
    public static abstract class Action<G extends Game<G>> {
        
        protected abstract boolean validate(State<G> state);
        
        protected abstract State<G> apply(State<G> state);
    }
    
    public static class Place<G extends Game<G>> extends Action<G> {
        
        private Piece<G> piece;
        
        private int x, y;
        
        public Place(Piece<G> piece, int x, int y) {
            this.piece = piece;
            this.x = x;
            this.y = y;
        }
        
        public Piece<G> getPiece() { return piece; }
        
        public int getX() { return x; }
        
        public int getY() { return y; }
        
        @Override
        protected boolean validate(State<G> state) {
            return piece.validatePlace(state, x, y);
        }
        
        @Override
        protected State<G> apply(State<G> state) {
            
            State<G> newState = state.clone();
            newState.previousState = state;
            newState.latestAction = this;
            newState.pieces[x][y] = piece;
            return newState;
        }
    }
    
    public static class Move<G extends Game<G>> extends Action<G> {
        
        private Piece<G> piece;
        
        private int x, y;
        
        public Move(Piece<G> piece, int x, int y) {
            this.piece = piece;
            this.x = x;
            this.y = y;
        }
        
        public Piece<G> getPiece() { return piece; }
        
        public int getX() { return x; }
        
        public int getY() { return y; }
        
        @Override
        protected boolean validate(State<G> state) {
            return piece.validateMove(state, x, y);
        }
        
        @Override
        protected State<G> apply(State<G> state) {
            
            State<G> newState = state.clone();
            newState.previousState = state;
            newState.latestAction = this;
            newState.pieces[piece.getX()][piece.getY()] = null;
            newState.pieces[x][y] = piece;
            return newState;
        }
    }
}