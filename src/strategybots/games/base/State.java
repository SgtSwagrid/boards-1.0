package strategybots.games.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;

import strategybots.games.base.Game.Player;
import swagui.layouts.Layout.Fill;
import swagui.tiles.Tile;

public abstract class State<G extends Game<G>>
        implements Serializable, Cloneable {
    
    private static final long serialVersionUID = 466853639779737105L;
    
    private G game;
    public G getGame() { return game; }
    
    private Player<G> currentPlayer;
    public Player<G> getCurrentPlayer() { return currentPlayer; }
    
    private State<G> previousState;
    public State<G> getPreviousState() { return previousState; }
    
    private State<G> turnStart;
    public State<G> getTurnStart() { return turnStart; }
    
    private Action<G> latestAction;
    public Action<G> getLatestAction() { return latestAction; }
    
    private boolean valid = true;
    public boolean isValid() { return valid; }
    
    private Piece<G>[][] board;
    private Map<Player<G>, List<Piece<G>>> pieces = new HashMap<>();
    private Map<Player<G>, Integer> scores = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    public State(G game) {
        
        this.game = game;
        board = new Piece[game.getWidth()][game.getHeight()];
        for(int i = 0; i < game.getNumPlayers(); i++) {
            pieces.put(game.getPlayer(i+1), new LinkedList<>());
            scores.put(game.getPlayer(i+1), 0);
        }
        currentPlayer = game.getPlayer(1);
        turnStart = this;
        latestAction = new None<>();
    }
    
    public Optional<Piece<G>> getPiece(int x, int y) {
        return !game.getBoard().inBounds(x, y) ? Optional.empty() :
            Optional.ofNullable(board[x][y]);
    }
    
    public List<Piece<G>> getPieces(Player<G> owner) {
        return Collections.unmodifiableList(pieces.get(owner));
    }
    
    public int getScore(Player<G> player) {
        return scores.get(player);
    }
    
    public List<Action<G>> getActionsSince(State<G> state) {
        
        if(state == this || previousState == null) {
            return new LinkedList<>();
        } else {
            List<Action<G>> actions = previousState.getActionsSince(state);
            actions.add(latestAction);
            return actions;
        }
    }
    
    public List<Action<G>> getActions() {
        return getActionsSince(turnStart);
    }
    
    public int getNumActions() {
        return getActions().size();
    }
    
    public State<G> getPreviousTurn() {
        return turnStart.previousState == null ? null :
            turnStart.previousState.turnStart;
    }
    
    public int getTurnNum() {
        return getPreviousTurn() == null ? 1 :
            getPreviousTurn().getTurnNum() + 1;
    }
    
    public boolean isTurnStarted() {
        return this != turnStart;
    }
    
    public int getCurrentPlayerId() {
        return currentPlayer.getPlayerId();
    }
    
    public Player<G> getNextPlayer() {
        return game.getPlayer(getNextPlayerId());
    }
    
    public int getNextPlayerId() {
        return currentPlayer.getPlayerId()%game.getNumPlayers() + 1;
    }
    
    public State<G> takeAction(Action<G> action) {
        
        if(action instanceof None) {
            return this;
            
        } else if(action instanceof MultiAction) {
            MultiAction<G> actions = (MultiAction<G>) action;
            return takeAction(actions.action1).takeAction(actions.action2);
            
        } else {
            
            MutableState<G> newState = new MutableState<>(this, false);
            newState.getState().previousState = this;
            newState.getState().latestAction = action;
            
            action.apply(newState);
            newState.getState().valid = valid && action.validate(this);
            return newState.getState();
        }
    }
    
    public abstract boolean isTerminal();
    
    public abstract Optional<Player<G>> getWinner();
    
    public abstract boolean canEndTurn();
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<G> clone() {
        try {
            
            //Copy the state.
            State<G> state = (State<G>) super.clone();
            
            //Copy the board.
            Piece<G>[][] board = new Piece[game.getWidth()][game.getHeight()];
            game.getBoard().forEachSquare((x, y) ->
                board[x][y] = state.board[x][y]);
            state.board = board;
            
            //Copy the pieces.
            state.pieces = state.pieces.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey,
                    p -> new LinkedList<>(p.getValue())));
            
            //Copy the scores.
            state.scores = new HashMap<>(state.scores);
            
            return state;
            
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static class MutableState<G extends Game<G>> {
        
        private State<G> state;
        public State<G> getState() { return state; }
        
        public MutableState(State<G> state, boolean initial) {
            
            this.state = state.clone();
            this.state.valid = initial;
            
            if(initial) {
                this.state.previousState = null;
                this.state.latestAction = null;
            }
        }
        
        public MutableState<G> endTurn() {
            
            int numPlayers = state.game.getNumPlayers();
            int playerId = state.getCurrentPlayerId() % numPlayers + 1;
            
            state.currentPlayer = state.game.getPlayer(playerId);
            
            state.turnStart = state;
            return this;
        }
        
        public MutableState<G> placePiece(Piece<G> piece) {
            
            //Remove piece originally in this position if present.
            Piece<G> oldPiece = state.board[piece.x][piece.y];
            if(oldPiece != null) {
                state.pieces.get(oldPiece.owner).remove(oldPiece);
            }
            //Add new piece to board.
            state.board[piece.x][piece.y] = piece;
            state.pieces.get(piece.owner).add(piece);
            
            return this;
        }
        
        public MutableState<G> removePiece(Piece<G> piece) {
            
            state.board[piece.x][piece.y] = null;
            state.pieces.get(piece.owner).remove(piece);
            return this;
        }
        
        public MutableState<G> movePiece(Piece<G> piece, int xTo, int yTo) {
            
            removePiece(piece);
            placePiece(piece.atPosition(xTo, yTo));
            return this;
        }
        
        public MutableState<G> setPlayer(Player<G> player) {
            
            state.currentPlayer = player;
            return this;
        }
        
        public MutableState<G> setScore(Player<G> player, int score) {
            
            state.scores.put(player,  score);
            return this;
        }
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
            this.tile = tile
                .setFill(Fill.FILL_PARENT);
        }
        
        public Piece<G> atPosition(int x, int y) {
            Piece<G> newPiece = clone();
            newPiece.x = x; newPiece.y = y;
            return newPiece;
        }
        
        protected boolean validatePlace(State<G> state) {
            return false;
        }
        
        protected boolean validateRemove(State<G> state) {
            return false;
        }
        
        protected boolean validateMove(State<G> state, int xTo, int yTo) {
            return false;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected Piece<G> clone() {
            
            Piece<G> piece = null;
            try {
                piece = (Piece<G>) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return piece;
        }
    }
    
    public static abstract class Action<G extends Game<G>>
            implements Serializable, Cloneable {
        
        private static final long serialVersionUID = -4793558180828857910L;
        
        protected abstract void apply(MutableState<G> state);
        
        protected abstract boolean validate(State<G> state);
        
        public Action<G> andThen(Action<G> action) {
            return new MultiAction<>(this, action);
        }
    }
    
    public static final class None<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = -2429198943864560223L;

        @Override
        protected void apply(MutableState<G> state) {}

        @Override
        protected boolean validate(State<G> state) { return false; }
    }
    
    private static final class MultiAction<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = -6329524076591365345L;
        
        private Action<G> action1, action2;
        
        private MultiAction(Action<G> action1, Action<G> action2) {
            this.action1 = action1; this.action2 = action2;
        }

        @Override
        protected void apply(MutableState<G> state) {}

        @Override
        protected boolean validate(State<G> state) { return false; }
    }
    
    public static class EndTurn<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = 2363771191741897828L;

        @Override
        protected void apply(MutableState<G> state) {
            state.endTurn();
        }

        @Override
        protected boolean validate(State<G> state) {
            return state.canEndTurn();
        }
    }
    
    public static class Place<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = -8475648365913067282L;
        
        private Piece<G> piece;
        public Piece<G> getPiece() { return piece; }
        
        public Place(Piece<G> piece) {
            this.piece = piece;
        }
        
        @Override
        protected void apply(MutableState<G> state) {
            state.placePiece(piece);
        }
        
        @Override
        protected boolean validate(State<G> state) {
            return state.game.getBoard().inBounds(piece.getX(), piece.getY())
                && (piece.owner == state.currentPlayer || piece.owner == null)
                && piece.validatePlace(state);
        }
    }
    
    public static class Remove<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = 2004483522227797246L;
        
        private Piece<G> piece;
        public Piece<G> getPiece() { return piece; }
        
        public Remove(Piece<G> piece) {
            this.piece = piece;
        }
        
        @Override
        protected void apply(MutableState<G> state) {
            state.removePiece(piece);
        }
        
        @Override
        protected boolean validate(State<G> state) {
            return piece.validateRemove(state);
        }
    }
    
    public static class Move<G extends Game<G>> extends Action<G> {
        
        private static final long serialVersionUID = -7578053215151752299L;
        
        private Piece<G> piece;
        public Piece<G> getPiece() { return piece; }
        
        private int xTo, yTo;
        public int getXTo() { return xTo; }
        public int getYTo() { return yTo; }
        
        public Move(Piece<G> piece, int xTo, int yTo) {
            this.piece = piece;
            this.xTo = xTo; this.yTo = yTo;
        }
        
        @Override
        protected void apply(MutableState<G> state) {
            state.movePiece(piece, xTo, yTo);
        }
        
        @Override
        protected boolean validate(State<G> state) {
            return state.game.getBoard().inBounds(xTo, yTo)
                && (piece.owner == state.currentPlayer || piece.owner == null)
                && !(piece.x == xTo && piece.y == yTo)
                && piece.validateMove(state, xTo, yTo);
        }
    }
}