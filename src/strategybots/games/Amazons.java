package strategybots.games;

import java.util.List;

import strategybots.games.Amazons.AmazonsState.MoveAmazon;
import strategybots.games.base.Board;
import strategybots.games.base.Game;
import strategybots.games.base.State;
import strategybots.games.base.State.Action;
import strategybots.games.base.State.Piece;
import swagui.graphics.Colour;
import swagui.graphics.Texture;
import swagui.tiles.Tile;

public final class Amazons extends Game<Amazons> {
    
    @SafeVarargs
    public Amazons(Player<Amazons>... players) {
        this(10, 10, players);
    }
    
    @SafeVarargs
    public Amazons(int width, int height, Player<Amazons>... players) {
        super(new AmazonsState(), players, new Board(width, height));
    }
    
    private static boolean validateQueensMove(State<Amazons> state,
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
        protected void endTurn() { super.endTurn(); }
        
        public static final class MoveAmazon extends Move<Amazons> {
            
            private static final long serialVersionUID = 5008360178081158142L;
            
            public MoveAmazon(State<Amazons> state,
                    int xFrom, int yFrom, int xTo, int yTo) {
                super(state, xFrom, yFrom, xTo, yTo);
            }
            
            public MoveAmazon(State<Amazons> state,
                    Piece<Amazons> piece, int xTo, int yTo) {
                super(piece, xTo, yTo);
            }

            @Override
            protected boolean validate(State<Amazons> state) {
                return state.getNumActions() == 1
                        && super.validate(state);
            }
        }
        
        public static final class ShootArrow extends Place<Amazons> {
            
            private static final long serialVersionUID = -935579828716369553L;
            
            public ShootArrow(State<Amazons> state, int x, int y) {
                //super(state, new Arrow(state.getCurrentPlayer(), x, y));
            }

            @Override
            protected boolean validate(State<Amazons> state) {
                return state.getLatestAction() instanceof MoveAmazon
                        && super.validate(state);
            }

            @Override
            protected void apply(State<Amazons> state) {
                super.apply(state);
                endTurn(state);
            }
        }
    }
    
    public static final class AmazonsController extends Controller<Amazons> {

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
        public boolean validatePlace(State<Amazons> state, int x, int y) {
            return false;
        }

        @Override
        public boolean validateMove(State<Amazons> state, int x, int y) {
            
            return validateQueensMove(state, getX(), getY(), x, y) &&
                state.getNumActions() == 0;
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
        public boolean validatePlace(State<Amazons> state, int x, int y) {
            MoveAmazon move = (MoveAmazon)state.getLatestAction();
            return validateQueensMove(state, move.getXTo(), move.getYTo(), x, y);
        }
        
        @Override
        public boolean validateMove(State<Amazons> state, int x, int y) {
            return false;
        }
    }
}