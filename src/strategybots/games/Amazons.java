package strategybots.games;

import java.util.List;

import strategybots.games.base.Board;
import strategybots.games.base.Game;
import strategybots.games.base.State;
import strategybots.games.base.State.Move;
import strategybots.games.base.State.Piece;
import swagui.graphics.Colour;
import swagui.graphics.Texture;
import swagui.tiles.Tile;

public class Amazons extends Game<Amazons> {
    
    @SafeVarargs
    public Amazons(Player<Amazons>... players) {
        this(10, 10, players);
    }
    
    @SafeVarargs
    public Amazons(int width, int height, Player<Amazons>... players) {
        super(new AmazonsState(), players, new Board(width, height));
    }
    
    public static class AmazonsState extends State<Amazons> {

        private static final long serialVersionUID = 3249133840361374590L;

        @Override
        public State<Amazons> takeAction(Action<Amazons> action) {
            // TODO Auto-generated method stub
            return null;
        }
        
        @Override
        public boolean validateAction(Action<Amazons> action) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public List<Action<Amazons>> getActions() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public State<Amazons> clone() {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    public static class AmazonsController extends Controller<Amazons> {

        @Override
        protected void init() {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected void destroy() {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected void onClick(State<Amazons> state, int x, int y) {
            // TODO Auto-generated method stub
            
        }
    }
    
    public static class Amazon extends Piece<Amazons> {
        
        private static final Texture WHITE = new Texture("res/chess/white_queen.png");
        private static final Texture BLACK = new Texture("res/chess/black_queen.png");

        public Amazon(Player<Amazons> owner, int x, int y) {
            
            super(owner, x, y, new Tile()
                .setColour(Colour.WHITE)
                .setTexture(owner.getPlayerId()==1 ? WHITE : BLACK));
        }

        @Override
        public boolean validatePlace(State<Amazons> state, int x, int y) {
            return false;
        }

        @Override
        public boolean validateMove(State<Amazons> state, int x, int y) {
            
            int width = state.getGame().getBoard().getWidth();
            int height = state.getGame().getBoard().getHeight();
            
            //Ensure piece is owned by the current player.
            if(!(getOwner() == state.getCurrentPlayer())) return false;
            
            //Ensure piece isn't moved to its current location.
            if(x == getX() && y == getY()) return false;
            
            //Ensure destination position is in bounds.
            if(x < 0 || x >= width || y < 0 || y >= height) return false;
            
            //Ensure amazon moves in a straight line.
            if(Math.abs(x-getX()) != Math.abs(y-getY()) &&
                x != getX() && y != getY()) return false;
            
            int xSign = (int) Math.signum(x - getX());
            int ySign = (int) Math.signum(y - getY());
            int xx = getX() + xSign, yy = getY() + ySign;
            
            //Ensure amazon has a clear path.
            while(xx != x+xSign || yy != y+ySign) {
                if(state.getPiece(xx, yy).isPresent()) return false;
                xx += xSign; yy += ySign;
            }
            return true;
        }
    }
    
    public static class Arrow extends Piece<Amazons> {
        
        private static final Texture WHITE = new Texture("res/misc/white_dot.png");
        private static final Texture BLACK = new Texture("res/misc/black_dot.png");
        
        public Arrow(Player<Amazons> owner, int x, int y) {
            
            super(owner, x, y, new Tile()
                .setColour(Colour.WHITE)
                .setTexture(owner.getPlayerId()==1 ? WHITE : BLACK));
        }

        @Override
        public boolean validatePlace(State<Amazons> state, int x, int y) {
            
            int width = state.getGame().getBoard().getWidth();
            int height = state.getGame().getBoard().getHeight();
            
            //Ensure piece is owned by the current player.
            if(!(getOwner() == state.getCurrentPlayer())) return false;
            
            //Ensure firing is preceded by the movement of an amazon.
            if(!(state.getLatestAction() instanceof Move)) return false;
            
            //Ensure destination position is in bounds.
            if(x < 0 || x >= width || y < 0 || y >= height) return false;
            
            //Ensure arrow is shot in a straight line.
            if(Math.abs(x-getX()) != Math.abs(y-getY()) &&
                x != getX() && y != getY()) return false;
            
            Move<Amazons> move = (Move<Amazons>) state.getLatestAction();
            int xFrom = move.getX(), yFrom = move.getY();
            
            int xSign = (int) Math.signum(x - xFrom);
            int ySign = (int) Math.signum(y - yFrom);
            int xx = getX() + xSign, yy = getY() + ySign;
            
            //Ensure arrow has a clear path.
            while(xx != x+xSign || yy != y+ySign) {
                if(state.getPiece(xx, yy).isPresent()) return false;
                xx += xSign; yy += ySign;
            }
            return true;
        }

        @Override
        public boolean validateMove(State<Amazons> state, int x, int y) {
            return false;
        }
    }
}