package strategybots.games;

import java.util.Optional;

import strategybots.games.base.Board;
import strategybots.games.base.Game;
import strategybots.games.base.State;
import strategybots.games.base.State.*;
import swagui.graphics.Colour;
import swagui.graphics.Texture;
import swagui.tiles.Tile;

public final class Amazons extends Game<Amazons> {
    
    private final int WIDTH = 10, HEIGHT = 10;
    
    @SuppressWarnings("unchecked")
    public Amazons(Player<Amazons> player1, Player<Amazons> player2) {
        
        setBoard(new Board(WIDTH, HEIGHT));
        setPlayers(player1, player2);
        setState(new MutableState<>(new AmazonsState(this), true)
            .placePiece(new Amazon(player1, 0, 3))
            .placePiece(new Amazon(player1, 3, 0))
            .placePiece(new Amazon(player1, WIDTH-4, 0))
            .placePiece(new Amazon(player1, WIDTH-1, 3))
            .placePiece(new Amazon(player2, 0, HEIGHT-4))
            .placePiece(new Amazon(player2, 3, HEIGHT-1))
            .placePiece(new Amazon(player2, WIDTH-4, HEIGHT-1))
            .placePiece(new Amazon(player2, WIDTH-1, HEIGHT-4))
            .getState());
        start();
    }
    
    private static boolean isQueensMove(State<Amazons> state,
            int xFrom, int yFrom, int xTo, int yTo) {
        
        //Ensure arrow is shot in a straight line.
        if(Math.abs(xTo-xFrom) != Math.abs(yTo-yFrom) &&
            xTo != xFrom && yTo != yFrom) return false;
        
        //Get direction of travel.
        int xSign = (int) Math.signum(xTo-xFrom);
        int ySign = (int) Math.signum(yTo-yFrom);
        int xx = xFrom + xSign, yy = yFrom + ySign;
        
        //Ensure arrow has a clear path.
        while(xx != xTo+xSign || yy != yTo+ySign) {
            if(state.getPiece(xx, yy).isPresent()) return false;
            xx += xSign; yy += ySign;
        }
        return true;
    }
    
    public static final class AmazonsState extends State<Amazons> {

        private static final long serialVersionUID = 3249133840361374590L;
        
        public AmazonsState(Amazons game) {
            super(game);
        }
        
        @Override
        public boolean isTerminal() {
            return getWinner().isPresent();
        }
        
        @Override
        public Optional<Player<Amazons>> getWinner() {
            
            //For each of the current player's amazons.
            for(Piece<Amazons> piece : getPieces(getCurrentPlayer())) {
                if(!(piece instanceof Amazon)) continue;
                
                //Search for an adjacent free space.
                for(int xx = -1; xx <= 1; xx++) {
                    for(int yy = -1; yy <= 1; yy++) {
                        int x = piece.getX()+xx, y = piece.getY()+yy;
                        
                        //The game isn't over if an amazon can be moved.
                        if(getGame().getBoard().inBounds(x, y) &&
                                !getPiece(x, y).isPresent()) {
                            return Optional.empty();
                        }
                    }
                }
            }
            //The opponent wins if the player can't move.
            return Optional.of(getNextPlayer());
        }
        
        @Override
        public boolean canEndTurn() {
            return getNumActions() == 2;
        }
    }
    
    public static final class Amazon extends Piece<Amazons> {
        
        private static final long serialVersionUID = 6230962660418775376L;
        
        private static final Texture WHITE = new Texture("res/chess/white_queen.png");
        private static final Texture BLACK = new Texture("res/chess/black_queen.png");

        public Amazon(Player<Amazons> owner, int x, int y) {
            
            super(owner, x, y, new Tile()
                .setColour(Colour.WHITE)
                .setTexture(owner.getPlayerId()==1 ? WHITE : BLACK));
        }

        @Override
        public boolean validateMove(State<Amazons> state, int xTo, int yTo) {
            
            //Ensure amazon is moved before shooting an arrow.
            if(state.getNumActions() != 0) return false;
            
            //Ensure movement is queen's move.
            return isQueensMove(state, getX(), getY(), xTo, yTo);
        }
    }
    
    public static final class Arrow extends Piece<Amazons> {
        
        private static final long serialVersionUID = -5346286099209106927L;
        
        private static final Texture WHITE = new Texture("res/misc/white_dot.png");
        private static final Texture BLACK = new Texture("res/misc/black_dot.png");
        
        public Arrow(Player<Amazons> owner, int x, int y) {
            
            super(owner, x, y, new Tile()
                .setColour(Colour.WHITE)
                .setTexture(owner.getPlayerId()==1 ? WHITE : BLACK));
        }

        @Override
        public boolean validatePlace(State<Amazons> state) {
            
            //Ensure arrow is shot after moving an amazon.
            if(state.getNumActions() != 1) return false;
            
            //Ensure arrow trajectory is queen's move.
            Move<Amazons> move = (Move<Amazons>) state.getLatestAction();
            int xFrom = move.getXTo(), yFrom = move.getYTo();
            return isQueensMove(state,  xFrom, yFrom, getX(), getY());
        }
    }
    
    public static final class AmazonsController extends Controller<Amazons> {
        
        private static final Colour HIGHLIGHT = Colour.NAVAL;
        
        private Piece<Amazons> selected;

        @Override
        protected Action<Amazons> onClick(Game<Amazons> game,
                State<Amazons> state, int x, int y, int playerId) {
            
            Piece<Amazons> piece = state.getPiece(x, y).orElse(null);
            
            if(state.getNumActions() == 0) {
                
                if(piece != null && piece instanceof Amazon &&
                        piece.getOwner() == this) {
                    
                    selected = piece;
                    game.getBoard().highlight(x, y, HIGHLIGHT);
                    
                } else if(selected != null && piece == null) {
                    
                    Action<Amazons> action = new Move<>(selected, x, y);
                    
                    if(state.takeAction(action).isValid()) {
                        game.getBoard().highlight(x, y, HIGHLIGHT);
                        return new Move<>(selected, x, y);
                    }
                }
                
            } else if(state.getNumActions() == 1) {
                
                Action<Amazons> action = new Place<>(new Arrow(this, x, y))
                        .andThen(new EndTurn<>());
                
                if(state.takeAction(action).isValid()) {
                    selected = null;
                    game.getBoard().clearHighlights();
                    return action;
                }
            }
            return new None<>();
        }
    }
}