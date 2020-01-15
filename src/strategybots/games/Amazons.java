package strategybots.games;

import strategybots.games.base.Board;
import strategybots.games.base.Game;
import strategybots.games.base.State;
import strategybots.games.base.State.*;
import swagui.graphics.Colour;
import swagui.graphics.Texture;
import swagui.tiles.Tile;

public final class Amazons extends Game<Amazons> {
    
    @SuppressWarnings("unchecked")
    public Amazons(Player<Amazons> player1, Player<Amazons> player2) {
        
        setBoard(new Board(10, 10));
        setPlayers(player1, player2);
        setState(new MutableState<>(new AmazonsState(this))
            .placePiece(new Amazon(player1, 0, 3))
            .placePiece(new Amazon(player1, 3, 0))
            .placePiece(new Amazon(player1, 6, 0))
            .placePiece(new Amazon(player1, 9, 3))
            .placePiece(new Amazon(player2, 0, 6))
            .placePiece(new Amazon(player2, 3, 9))
            .placePiece(new Amazon(player2, 6, 9))
            .placePiece(new Amazon(player2, 9, 6))
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
        protected boolean canEndTurn() {
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

        @Override
        protected Action<Amazons> onClick(State<Amazons> state,
                int x, int y, int playerId) {
            
            Piece<Amazons> piece = state.getPiece(x, y).orElse(null);
            Piece<Amazons> selected = getSelectedPiece().orElse(null);
            
            if(state.getNumActions() == 0) {
                
                if(piece != null && piece instanceof Amazon &&
                        piece.getOwner() == this) {
                    
                    selectPiece(piece, Colour.WHITE);
                    return new None<>();
                    
                } else if(selected != null && piece == null) {
                    
                    unselectPiece();
                    return new Move<>(selected, x, y);
                }
                
            } else if(state.getNumActions() == 1) {
                
                return new Place<>(new Arrow(this, x, y))
                    .andThen(new EndTurn<>());
            }
            return new None<>();
        }
    }
}