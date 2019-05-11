package games;

import java.util.Optional;

import games.util.GridGame;

import swagui.api.Colour;
import swagui.api.Texture;

/**
 * <b>Game of the Amazons implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Game_of_the_Amazons">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Amazons>'.<br>
 * Human players can be made by instantiating 'AmazonsController'.
 * 
 * @author Alec Dorrington
 */
public class Amazons extends GridGame {
    
    /** Title of the window. */
    private static final String TITLE = "Game of the Amazons";
    
    /** Colour used for selected pieces. */
    public static final Colour HIGHLIGHT_COLOUR1 = Colour.rgb(88, 177, 159);
    /** Colour used for moved pieces. */
    public static final Colour HIGHLIGHT_COLOUR2 = Colour.rgb(249, 127, 81);
    
    /** The piece which was moved on this turn. */
    private Optional<Amazon> movedPiece = Optional.empty();
    /** Whether a amazon has yet been moved on this turn. */
    private boolean amazonMoved = false;
    
    /**
     * Asynchronously runs a new Game of the Amazons instance.
     * @param width the width of the game board.
     * @param height the height of the game board.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Amazons(int width, int height, Player<Amazons> player1, Player<Amazons> player2) {
        super(width, height, TITLE, player1, player2);
    }
    
    /**
     * Moves your amazon at the given position to a new position.<br>
     * Must be called exactly once per turn, before 'shootArrow()' is called.<br>
     * Move must be consistent with the rules of the game, or an exception will be thrown.
     * @param x_from the current x position of the amazon.
     * @param y_from the current y position of the amazon.
     * @param x_to the new x position of the amazon.
     * @param y_to the new y position of the amazon.
     * @throws IllegalMoveException
     */
    public void moveAmazon(int x_from, int y_from, int x_to, int y_to) {
        
        validateMove(x_from, y_from);
        validateMove(x_to, y_to);
        
        //Ensure there is a piece at the from location.
        if(!getPiece(x_from, y_from).isPresent())
            throw new IllegalMoveException("No such piece exists.");
        
        //Ensure amazon hasn't already been moved.
        if(amazonMoved)
            throw new IllegalMoveException("Can't move pieces twice.");
        
        //Move the piece, subject to game constraints.
        getPiece(x_from, y_from).get().movePiece(x_to, y_to);
        
        //Remember the piece which was moved, for the arrow-shooting phase.
        movedPiece = Optional.of((Amazon) getPiece(x_to, y_to).get());
        amazonMoved = true;
    }
    
    /**
     * Shoots an arrow at a given position.<br>
     * Must be called exactly once per turn, after 'moveAmazon()' is called.<br>
     * Arrow will be fired from the most recently moved amazon.<br>
     * Move must be consistent with the rules of the game, or an exception will be thrown.
     * @param x the destination x position of the arrow.
     * @param y the destination y position of the arrow.
     * @throws IllegalMoveException
     */
    public void shootArrow(int x, int y) {
        
        validateMove(x, y);
        
        //Ensure actions are taken in the correct order.
        if(!amazonMoved)
            throw new IllegalMoveException("Must move a amazon before shooting.");
        
        //Fire the arrow, subject to game constraints.
        movedPiece.get().shootArrow(x, y);
        
        setTurnTaken(); amazonMoved = false;
    }
    
    /**
     * Returns the current state of the board.<br>
     * Each array index represents the piece currently at that position.<br>
     * <table border="1">
     * <tr><td>-1</td><td>Empty tile.</td></tr>
     * <tr><td>0</td><td>Arrow.</td></tr>
     * <tr><td>1</td><td>Amazon owned by player 1.</td></tr>
     * <tr><td>2</td><td>Amazon owned by player 2.</td></tr>
     * </table>
     * @return the game state.
     */
    public int[][] getState() {
        
        int[][] board = new int[getWidth()][getHeight()];
        
        //For each grid cell.
        for(int x = 0; x < getWidth(); x++) {
            for(int y = 0; y < getHeight(); y++) {
                
                //Determine index of piece on this tile.
                //Empty tile: '-1'.
                if(!getPiece(x, y).isPresent())
                    board[x][y] = -1;
                
                //Arrow: '0'.
                else if(getPiece(x, y).get() instanceof Arrow)
                    board[x][y] = 0;
                
                //Amazon: ID of owner.
                else board[x][y] = getPiece(x, y).get().getOwnerId();
            }
        }
        return board;
    }
    
    @Override
    protected void init() {
        
        //Determine appropriate spacing for pieces given board size.
        int h_indent = (getWidth() - 1) / 3;
        int v_indent = (getHeight() - 1) / 3;
        
        //Place the amazons in their starting positions.
        new Amazon(1, 0, v_indent);
        new Amazon(1, h_indent, 0);
        new Amazon(1, getWidth() - 1 - h_indent, 0);
        new Amazon(1, getWidth() - 1, v_indent);
        
        new Amazon(2, 0, getHeight() - 1 - v_indent);
        new Amazon(2, h_indent, getHeight() - 1);
        new Amazon(2, getWidth() - 1 - h_indent, getHeight() - 1);
        new Amazon(2, getWidth() - 1, getHeight() - 1 - v_indent);
    }
    
    @Override
    protected void checkWin() {
        
        //Check if any of the opponents pieces are free to move.
        for(Piece piece : getPieces(getCurrentPlayerId() % 2 + 1)) {
            
            //Only consider amazons.
            if(!(piece instanceof Amazon)) continue;
            
            //Look at all the surrounding tiles for each opponent piece.
            for(int x = Math.max(piece.getCol() - 1, 0);
                    x <= Math.min(piece.getCol() + 1, getWidth() - 1); x++) {
                
                for(int y = Math.max(piece.getRow() - 1, 0);
                        y <= Math.min(piece.getRow() + 1, getHeight() - 1); y++) {
                    
                    //Player hasn't won if the opponent has somewhere to move.
                    if(!getPiece(x, y).isPresent()) {
                        return;
                    }
                }
            }
        }
        //The opponent has nowhere to move, this player wins.
        endGame(getCurrentPlayerId());
    }
    
    @Override
    protected void onFinish() {
        
        //Set the window title to reflect the game completion.
        super.onFinish();
        
        //Highlight the amazon pieces of the winner and loser in different colours.
        if(getWinner().isPresent()) {
            
            //Set the colour for the winning pieces.
            for(Piece p : getPieces(getWinnerId())) {
                if(p instanceof Amazon)
                    getBoard().setColour(p.getCol(), p.getRow(), HIGHLIGHT_COLOUR1);
            }
            
            //Set the colour for the losing pieces.
            for(Piece p : getPieces(getWinnerId() % 2 + 1)) {
                if(p instanceof Amazon)
                    getBoard().setColour(p.getCol(), p.getRow(), HIGHLIGHT_COLOUR2);
            }
        }
    }
    
    /**
     * Implementation of Player<Amazons> for use in inserting a human-controlled player.<br>
     * Each AmazonsController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class AmazonsController implements Player<Amazons> {
        
        /** The display name of this player. */
        private String name = "Controller";
        
        /** The piece currently selected by the mouse, if there is any. */
        private Optional<Piece> selected = Optional.empty();
        
        /**
         * Constructs a new AmazonsController with the default name of "Controller".
         */
        public AmazonsController() {}
        
        /** 
         * Constructs a new AmazonsController with the given name.
         * @param name the display name of this controller.
         */
        public AmazonsController(String name) { this.name = name; }
        
        @Override
        public void init(Amazons game, int playerId) {
            
            //Add a click listener to each grid cell on the board.
            game.getBoard().addListenerToAll((x, y) -> {
                
                //Listeners should only be active on your own turn.
                if(playerId != game.getCurrentPlayerId() || !game.isRunning())
                    return;
                
                //Move a amazon.
                if(!game.amazonMoved) {
                    moveAmazon(game, x, y);
                    
                //Shoot an arrow.
                } else if(!game.turnTaken()) {
                    shootArrow(game, x, y);
                }
            });
        }
        
        /**
         * Select or move a amazon as appropriate.<br>
         * To be called when a tile is clicked prior to any amazon movement.
         * @param game the game being played.
         * @param x the x position which was clicked.
         * @param y the y position which was clicked.
         */
        private void moveAmazon(Amazons game, int x, int y) {
            
            //Select a amazon.
            if(game.getPiece(x, y).isPresent()
                    && game.getPiece(x, y).get() instanceof Amazon
                    && game.getPiece(x, y).get().getOwnerId() == game.getCurrentPlayerId()) {
                
                //Set the selected piece and highlight its tile.
                game.getBoard().resetColours();
                
                selected = game.getPiece(x, y);
                game.getBoard().setColour(x, y, HIGHLIGHT_COLOUR1);
                
            //Move the selected amazon.
            } else if(selected.isPresent()) {
                
                try {
                    //Try to move the selected piece to its new tile.
                    game.moveAmazon(selected.get().getCol(), selected.get().getRow(), x, y);
                    game.getBoard().resetColours();
                    game.getBoard().setColour(x, y, HIGHLIGHT_COLOUR2);
                    
                //Invalid moves should be ignored.
                } catch(IllegalMoveException e) {}
            }
        }
        
        /**
         * Fire an arrow from the selected amazon to the given position.
         * To be called when a tile is clicked after the amazon is moved.
         * @param game the game being played.
         * @param x the x position which was clicked.
         * @param y the y position which was clicked.
         */
        private void shootArrow(Amazons game, int x, int y) {
            
            try {
                //Try to spawn an arrow at this location.
                game.shootArrow(x, y);
                game.getBoard().resetColours();
                selected = Optional.empty();
                
            //Invalid moves should be ignored.
            } catch(IllegalMoveException e) {}
        }
        
        @Override
        public void takeTurn(Amazons game, int playerId) {
            //Wait until the turn is complete before returning control to the game.
            //Actual logic is handled asynchronously by the above button listeners.
            while(!game.turnTaken() && game.getWindow().isOpen()) {}
        }
        
        @Override
        public String getName() { return name; }
    }
    
    /**
     * Represents a amazon instance on the board.
     * @author Alec Dorrington
     */
    private class Amazon extends Piece {
        
        Amazon(int ownerId, int x, int y) {
            
            super(ownerId, x, y);
            
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/chess/white_amazon.png" : "res/chess/black_amazon.png"));
        }
        
        @Override
        public void movePiece(int x_to, int y_to) {
            
            //Ensure a move is valid before making it.
            validateMove(getCol(), getRow(), x_to, y_to);
            
            //Update the position of the piece.
            setBoardPos(x_to, y_to);
        }
        
        /**
         * Will fire an arrow from this amazon to the given position, if such a move is valid.<br>
         * Otherwise, an exception will be thrown.
         * @param x_to the x position to shoot at.
         * @param y_to the y position to shoot at.
         * @throws IllegalMoveException
         */
        void shootArrow(int x_to, int y_to) {
            
            validateMove(getCol(), getRow(), x_to, y_to);
            new Arrow(getOwnerId(), x_to, y_to);
        }
        
        /**
         * Ensures that a move is valid.<br>
         * Throws an exception otherwise.
         * @param x_from the x position from which a piece is moving.
         * @param y_from the y position from which a piece is moving.
         * @param x_to the x position to which a piece is moving.
         * @param y_to the y position to which a piece is moving.
         * @throws IllegalMoveException
         */
        void validateMove(int x_from, int y_from, int x_to, int y_to) {
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId())
                throw new IllegalMoveException("Can't move an opponents piece.");
            
            //Ensure piece doesn't move on top of itself.
            if(x_to == x_from && y_to == y_from)
                throw new IllegalMoveException("Must move somewhere else.");
            
            //Ensure piece doesn't move on top of another piece.
            if(getPiece(x_to, y_to).isPresent())
                throw new IllegalMoveException("Can't move onto another piece.");
            
            //Ensure piece moves in a straight line (incl. diagonally).
            if(Math.abs(x_to - x_from) != Math.abs(y_to - y_from)
                    && x_to != x_from && y_to != y_from)
                throw new IllegalMoveException("Must move in a straight line.");
            
            
            int x_sign = (int) Math.signum(x_to - x_from);
            int y_sign = (int) Math.signum(y_to - y_from);
            
            int xx = x_from + x_sign;
            int yy = y_from + y_sign;
            
            //Ensure piece doesn't jump over any other pieces.
            while(xx != x_to || yy != y_to) {
                
                if(getPiece(xx, yy).isPresent())
                    throw new IllegalMoveException("Can't jump over other pieces.");
                
                xx += x_sign;
                yy += y_sign;
            }
        }
    }
    
    /**
     * Represents an arrow instance fired onto the board.
     * @author Alec Dorrington
     */
    private class Arrow extends Piece {
        
        private Arrow(int ownerId, int x, int y) {
            
            super(ownerId, x, y);
            
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/chess/white_pawn.png" : "res/chess/black_pawn.png"));
        }
        
        @Override
        public void movePiece(int x_to, int y_to) {
            //Arrows can't be moved.
            throw new IllegalMoveException("Arrows can't be moved.");
        }
    }
}