package games;

import java.util.Optional;

import games.util.GridGame;
import swagui.api.Colour;

/**
 * <b>Clobber implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Clobber">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Clobber>'.<br>
 * Human players can be made by instantiating 'ClobberController'.
 * 
 * @author Alec Dorrington
 */
public class Clobber extends GridGame {
    
    /** Title of the window. */
    private static final String TITLE = "Clobber";
    
    /** Textures used for game pieces. */
    private static final String[] STONE_TEXTURES = new String[] {
            "res/chess/white_pawn.png", "res/chess/black_pawn.png"};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "White", "Black"};
    
    /** Colour used for selected pieces. */
    private static final Colour HIGHLIGHT_COLOUR = Colour.rgb(88, 177, 159);
    /** Colour used to highlight the losing players pieces. */
    private static final Colour LOSER_COLOUR = Colour.rgb(249, 127, 81);
    
    /**
     * Asynchronously runs a new Clobber instance.
     * @param width the width of the game board.
     * @param height the height of the game board.
     * @players the players who are to participate.
     */
    public Clobber(int width, int height, Player<Clobber> player1, Player<Clobber> player2) {
        super(width, height, TITLE, player1, player2);
    }
    
    /**
     * Moves your stone at the given position to a new position.<br>
     * Must be called exactly once per turn.<br>
     * Move must be consistent with the rules of the game, or an exception will be thrown.
     * @param x_from the current x position of the piece.
     * @param y_from the current y position of the piece.
     * @param x_to the new x position of the piece.
     * @param y_to the new y position of the piece.
     * @throws IllegalMoveException
     */
    public void moveStone(int x_from, int y_from, int x_to, int y_to) {
        
        validateMove(x_from, y_from);
        validateMove(x_to, y_to);
        
        //Ensure there is a piece at the from location.
        if(!getPiece(x_from, y_from).isPresent())
            throw new IllegalMoveException("No such piece exists.");
        
        //Move the piece, subject to game constraints.
        getPiece(x_from, y_from).get().movePiece(x_to, y_to);
        
        setTurnTaken();
    }
    
    /**
     * Returns the stone currently at the given position.<br>
     * <table border="1">
     * <tr><td>0</td><td>Empty tile.</td></tr>
     * <tr><td>1</td><td>Piece owned by player 1.</td></tr>
     * <tr><td>2</td><td>Piece owned by player 2.</td></tr>
     * </table>
     * @param x the x position at which to check for a piece.
     * @param y the y position at which to check for a piece.
     * @return the piece at (x, y) on the board.
     */
    public int getStone(int x, int y) {
        return getPiece(x, y).isPresent() ? getPiece(x, y).get().getOwnerId() : 0;
    }
    
    @Override
    protected void init() {
        
        //Place the initial pieces on the board.
        for(int x = 0; x < getWidth(); x++) {
            for(int y = 0; y < getHeight(); y++) {
                
                //The stones should be placed in an alternating pattern.
                new Stone((x + y + 1) % 2 + 1, x, y);
            }
        }
    }
    
    @Override
    protected void checkWin() {
        
        //Check if any of the opponents pieces have any possible moves.
        for(Piece piece : getPieces(getCurrentPlayerId() % 2 + 1)) {
            
            //Look at all the surrounding tiles for each opponent piece.
            for(int x = Math.max(piece.getCol() - 1, 0);
                    x <= Math.min(piece.getCol() + 1, getWidth() - 1); x++) {
                
                for(int y = Math.max(piece.getRow() - 1, 0);
                        y <= Math.min(piece.getRow() + 1, getHeight() - 1); y++) {
                    
                    //If this piece is adjacent to the opponents piece,
                    if((x == piece.getCol() ^ y == piece.getRow()) &&
                    //And is itself a friendly piece:
                            getPiece(x, y).isPresent() &&
                            getPiece(x, y).get().getOwnerId() == getCurrentPlayerId()) {
                        
                        //Then there is a piece left for the opponent to clobber.
                        //Thus, you haven't yet won.
                        return;
                    }
                }
            }
        }
        endGame(getCurrentPlayerId());
    }
    
    @Override
    protected void onFinish() {
        
        //Set the window title to reflect the game completion.
        super.onFinish();
        
        //Highlight the stones of the winner and loser in different colours.
        if(getWinner().isPresent()) {
            
            //Set the colour for the winning pieces.
            for(Piece p : getPieces(getWinnerId())) {
                getBoard().setColour(p.getCol(), p.getRow(), HIGHLIGHT_COLOUR);
            }
            
            //Set the colour for the losing pieces.
            for(Piece p : getPieces(getWinnerId() % 2 + 1)) {
                getBoard().setColour(p.getCol(), p.getRow(), LOSER_COLOUR);
            }
        }
    }
    
    @Override
    protected String getPlayerName(int playerId) {
        return getPlayer(playerId).getName() + " (" + COLOUR_NAMES[playerId - 1] + ")";
    }
    
    /**
     * Implementation of Player<Clobber> for use in inserting a human-controlled player.<br>
     * Each ClobberController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class ClobberController implements Player<Clobber> {
        
        /** The display name of this player. */
        private String name = "Controller";
        
        /** The piece currently selected by the mouse, if there is any. */
        private Optional<Piece> selected = Optional.empty();
        
        /**
         * Constructs a new ClobberController with the default name of "Controller".
         */
        public ClobberController() {}
        
        /** 
         * Constructs a new ClobberController with the given name.
         * @param name the display name of this controller.
         */
        public ClobberController(String name) { this.name = name; }
        
        @Override
        public void init(Clobber game, int playerId) {
            
            //Add a click listener to each grid cell on the board.
            game.getBoard().addListenerToAll((x, y) -> {
                
                //Listeners should only be active on your own turn.
                if(playerId != game.getCurrentPlayerId() || !game.isRunning())
                    return;
                
                if(game.getPiece(x, y).isPresent()) {
                    
                    if(game.getPiece(x, y).get().getOwnerId() == playerId) {
                        
                        //Select this piece.
                        selected = game.getPiece(x, y);
                        game.getBoard().resetColours();
                        game.getBoard().setColour(x, y, HIGHLIGHT_COLOUR);
                        
                    } else if(selected.isPresent()) {
                        
                        try {
                            //Move the selected piece to this location.
                            game.moveStone(selected.get().getCol(), selected.get().getRow(), x, y);
                            game.getBoard().resetColours();
                            selected = Optional.empty();
                            
                        //Invalid moves should be ignored.
                        } catch(IllegalMoveException e) {}
                    }
                }
            });
        }
        
        @Override
        public void takeTurn(Clobber game, int playerId) {
            //Wait until the turn is complete before returning control to the game.
            //Actual logic is handled asynchronously by the above button listeners.
            while(!game.turnTaken() && game.getWindow().isOpen()) {}
        }
        
        @Override
        public String getName() { return name; }
    }
    
    /**
     * Represents a Clobber game piece.
     * @author Alec Dorrington
     */
    private class Stone extends Piece {
        
        Stone(int ownerId, int x, int y) {
            super(ownerId, x, y, STONE_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId())
                throw new IllegalMoveException("Can't move an opponents piece.");
            
            //Ensure piece moves on top of an opponent piece.
            if(!getPiece(x_to, y_to).isPresent() ||
                    getPiece(x_to, y_to).get().getOwnerId() == getCurrentPlayerId())
                throw new IllegalMoveException("Must move onto an opponent piece.");
            
            //Ensure piece moves to an adjacent piece.
            if(!(Math.abs(getCol() - x_to) == 0 && Math.abs(getRow() - y_to) == 1) &&
                    !(Math.abs(getCol() - x_to) == 1 && Math.abs(getRow() - y_to) == 0)) {
                throw new IllegalMoveException("Must move onto an adjacent piece.");
            }
            
            //Delete the tile being captured.
            getPiece(x_to, y_to).get().delete();
            
            //Move the piece on top of the captured piece.
            setBoardPos(x_to, y_to);
        }
    }
}