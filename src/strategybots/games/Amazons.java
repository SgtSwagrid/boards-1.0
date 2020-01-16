package strategybots.games;

import java.util.Optional;

import strategybots.games.base.Board;
import strategybots.games.base.Game;
import strategybots.games.base.State;
import strategybots.games.base.State.*;
import swagui.graphics.Colour;
import swagui.graphics.Texture;
import swagui.tiles.Tile;

/**
 * <b>Game of the Amazons</b><br>
 * <a href="https://en.wikipedia.org/wiki/Game_of_the_Amazons">Rules (Wikipedia)</a>
 * <br><br>
 * Bot players can be made by implementing 'Player&lt;Amazons&gt;'.<br>
 * Human players can be made by instantiating 'AmazonsController'.
 * 
 * @author Alec Dorrington
 */
public final class Amazons extends Game<Amazons> {
    
    /** The size of the game board. */
    private static final int WIDTH = 10, HEIGHT = 10;
    
    /**
     * Run a new Game of the Amazons instance, blocking until completion.
     * @param player1 the first participating player (white).
     * @param player2 the second participating player (black).
     */
    public Amazons(Player<Amazons> player1, Player<Amazons> player2) {
        
        super(new Board(WIDTH, HEIGHT)
            .setTitle("Game of the Amazons"),
            player1, player2);
        
        start(new AmazonsState(this));
    }
    
    /**
     * Determine whether a move is a valid queen's move.
     * @param state the initial board state.
     * @param xFrom the source x-coordinate.
     * @param yFrom the source y-coordinate.
     * @param xTo the destination x-coordinate.
     * @param yTo the destination y-coordinate.
     * @return whether this is a valid queen's move.
     */
    private static boolean isQueensMove(State<Amazons> state,
            int xFrom, int yFrom, int xTo, int yTo) {
        
        //Ensure piece moves in a straight line.
        if(Math.abs(xTo-xFrom) != Math.abs(yTo-yFrom) &&
            xTo != xFrom && yTo != yFrom) return false;
        
        //Ensure destination is distinct from source.
        if(xTo-xFrom==0 && yTo-yFrom==0) return false;
        
        //Get direction of travel.
        int xSign = (int) Math.signum(xTo-xFrom);
        int ySign = (int) Math.signum(yTo-yFrom);
        int xx = xFrom + xSign, yy = yFrom + ySign;
        
        //Ensure piece has a clear path.
        while(xx != xTo+xSign || yy != yTo+ySign) {
            if(state.getPiece(xx, yy).isPresent()) return false;
            xx += xSign; yy += ySign;
        }
        return true;
    }
    
    /**
     * Game of the Amazons immutable board state.<br>
     * Actions are used to get subsequent board states.
     */
    public static final class AmazonsState extends State<Amazons> {

        private static final long serialVersionUID = 3249133840361374590L;
        
        /**
         * Create a new Amazons state, preparing the initial board position.
         * @param game the amazons game, featuring the board and players.
         */
        public AmazonsState(Amazons game) {
            super(game);
            
            //Setup initial board state.
            placePiece(new Amazon(game.getPlayer(1), 0, 3));
            placePiece(new Amazon(game.getPlayer(1), 3, 0));
            placePiece(new Amazon(game.getPlayer(1), WIDTH-4, 0));
            placePiece(new Amazon(game.getPlayer(1), WIDTH-1, 3));
            placePiece(new Amazon(game.getPlayer(2), 0, HEIGHT-4));
            placePiece(new Amazon(game.getPlayer(2), 3, HEIGHT-1));
            placePiece(new Amazon(game.getPlayer(2), WIDTH-4, HEIGHT-1));
            placePiece(new Amazon(game.getPlayer(2), WIDTH-1, HEIGHT-4));
        }
        
        @Override
        public boolean isTerminal() {
            
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
                            return false;
                        }
                    }
                }
            }
            //The game ends if the player can't move.
            return true;
        }
        
        @Override
        public Optional<Player<Amazons>> getWinner() {
            return isTerminal() ? Optional.of(getNextPlayer())
                    : Optional.empty();
        }
        
        @Override
        public boolean canEndTurn() {
            return getNumActions() == 2;
        }
    }
    
    /**
     * Amazon game piece, looks and moves like a chess queen.<br>
     * Use the 'Move' action upon turn start to move an amazon.
     */
    public static final class Amazon extends Piece<Amazons> {
        
        private static final long serialVersionUID = 6230962660418775376L;
        
        /** Amazon piece textures for players 1 and 2 respectively. */
        private static final Texture WHITE = new Texture("res/chess/white_queen.png");
        private static final Texture BLACK = new Texture("res/chess/black_queen.png");
        
        /**
         * Create a new amazon piece.
         * @param owner of the amazon.
         * @param x coordinate of the amazon.
         * @param y coordinate of the amazon.
         */
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
    
    /**
     * Arrow game piece, shot by an amazon to block off area.<br>
     * Use the 'Place' action after moving an amazon to shoot an arrow.
     */
    public static final class Arrow extends Piece<Amazons> {
        
        private static final long serialVersionUID = -5346286099209106927L;
        
        /** Arrow piece textures for players 1 and 2 respectively. */
        private static final Texture WHITE = new Texture("res/misc/white_dot.png");
        private static final Texture BLACK = new Texture("res/misc/black_dot.png");
        
        /**
         * Create a new arrow piece.
         * @param owner of the arrow.
         * @param x coordinate of the arrow.
         * @param y coordiante of the arrow.
         */
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
    
    /**
     * Implementation of Player for use in inserting a human-controlled player.<br>
     * Makes moves based on mouse input.
     */
    public static final class AmazonsController extends Controller<Amazons> {
        
        /** Background highlight colour for selected pieces. */
        private static final Colour HIGHLIGHT = Colour.NAVAL;
        
        /** The currently-selected amazon piece. */
        private Piece<Amazons> selected;

        @Override
        protected Action<Amazons> onClick(Game<Amazons> game,
                State<Amazons> state, int x, int y, int playerId) {
            
            //The piece at the clicked location, if there is one.
            Piece<Amazons> piece = state.getPiece(x, y).orElse(null);
            
            //First, an amazon should be moved.
            if(state.getNumActions() == 0) {
                
                //If the player clicks their own amazon piece.
                if(piece != null && piece instanceof Amazon &&
                        piece.getOwner() == this) {
                    
                    //Select and highlight the piece.
                    selected = piece;
                    game.getBoard().highlight(x, y, HIGHLIGHT);
                    
                //Otherwise, if an amazon piece is already selected.
                } else if(selected != null && piece == null) {
                    
                    //Move the selected piece to the clicked tile.
                    Action<Amazons> action = new Move<>(selected, x, y);
                    
                    //If this is a valid move.
                    if(state.takeAction(action).isValid()) {
                        //Move the piece and highlight the new tile.
                        game.getBoard().highlight(x, y, HIGHLIGHT);
                        return new Move<>(selected, x, y);
                    }
                }
                
            //Then, an arrow should be shot.
            } else if(state.getNumActions() == 1) {
                
                //Shoot an arrow.
                Action<Amazons> action = new Place<>(new Arrow(this, x, y))
                        .andThen(new EndTurn<>());
                
                //If this is a valid shot.
                if(state.takeAction(action).isValid()) {
                    //Unselect the piece, shoot the arrow and end the turn.
                    selected = null;
                    game.getBoard().clearHighlights();
                    return action;
                }
            }
            return new None<>();
        }
    }
}