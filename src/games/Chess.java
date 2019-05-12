package games;

import games.util.GridGame;

/**
 * <b>Chess implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Chess">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Chess>'.<br>
 * Human players can be made by instantiating 'ChessController'.
 * 
 * @author Alec Dorrington
 */
public class Chess extends GridGame {
    
    /** Title of the window. */
    private static final String TITLE = "Chess";
    
    /** The dimensions of the board. */
    private static final int WIDTH = 8, HEIGHT = 8;
    
    private static final String[] PAWN_TEXTURES = new String[] {
            "res/chess/white_pawn.png", "res/chess/black_pawn.png"};
    
    private static final String[] ROOK_TEXTURES = new String[] {
            "res/chess/white_rook.png", "res/chess/black_rook.png"};
    
    private static final String[] KNIGHT_TEXTURES = new String[] {
            "res/chess/white_knight.png", "res/chess/black_knight.png"};
    
    private static final String[] BISHOP_TEXTURES = new String[] {
            "res/chess/white_bishop.png", "res/chess/black_bishop.png"};
    
    private static final String[] QUEEN_TEXTURES = new String[] {
            "res/chess/white_queen.png", "res/chess/black_queen.png"};
    
    private static final String[] KING_TEXTURES = new String[] {
            "res/chess/white_king.png", "res/chess/black_king.png"};
    
    /**
     * Asynchronously runs a new Chess instance.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Chess(Player<Chess> player1, Player<Chess> player2) {
        super(WIDTH, HEIGHT, TITLE, player1, player2);
    }
    
    public void movePiece(int x_from, int y_from, int x_to, int y_to) {
        
        validateMove(x_from, y_from);
        validateMove(y_to, y_to);
        
        //Ensure there is a piece at the from location.
        if(!getPiece(x_from, y_from).isPresent())
            throw new IllegalMoveException("No such piece exists.");
        
        //Ensure pieces don't capture other pieces from their own team.
        if(getPiece(x_to, y_to).isPresent() && getPiece(x_from, y_from).get().getOwnerId()
                == getPiece(x_to, y_to).get().getOwnerId())
            throw new IllegalMoveException("You may not capture your own pieces.");
        
        //Move the piece, subject to game constraints.
        getPiece(x_from, y_from).get().movePiece(x_to, y_to);
        
        setTurnTaken();
    }
    
    @Override
    protected void init() {
        
        for(int i = 0; i < WIDTH; i++) {
            new Pawn(1, i, 1);
            new Pawn(2, i, HEIGHT - 2);
        }
        
        for(int i = 0; i < 2; i++) {
            new Rook(i + 1, 0, i * 7);
            new Knight(i + 1, 1, i * 7);
            new Bishop(i + 1, 2, i * 7);
            new King(i + 1, 3, i * 7);
            new Queen(i + 1, 4, i * 7);
            new Bishop(i + 1, 5, i * 7);
            new Knight(i + 1, 6, i * 7);
            new Rook(i + 1, 7, i * 7);
        }
    }
    
    /**
     * Implementation of Player<Chess> for use in inserting a human-controlled player.<br>
     * Each ChessController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static class ChessController extends Controller<Chess> {
        
        public ChessController() { super(); }
        
        public ChessController(String name) { super(name); }
        
        @Override
        protected void onTileClicked(Chess game, int playerId, int x, int y) {
            
            //If the piece clicked belongs to the current player.
            if(game.getPiece(x, y).isPresent() &&
                    game.getPiece(x, y).get().getOwnerId() == playerId) {
                
                //Select this piece.
                selectPiece(game, game.getPiece(x, y).get());
                
            //If the current player has a piece selected.
            } else if(getSelected().isPresent()) {
                
                try {
                    //Move the selected piece to this location.
                    game.movePiece(getSelected().get().getCol(), getSelected().get().getRow(), x, y);
                    deselectPiece(game);
                    
                //Invalid moves should be ignored.
                } catch(IllegalMoveException e) {}
            }
        }
    }
    
    /**
     * Represents a pawn chess piece.
     * @author Alec Dorrington
     */
    private class Pawn extends Piece {
        
        Pawn(int ownerId, int x, int y) {
            super(ownerId, x, y, PAWN_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            
            //Unit value in direction pawn should be moving.
            int dir = getOwnerId() == 1 ? 1 : -1;
            //The y value at which the pawn started.
            int home = getOwnerId() == 1 ? 1 : 6;
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure pawns move forward the correct number of spaces.
            if(Math.abs(dy) < 1 || Math.abs(dy) > 2)
                throw new IllegalMoveException("Pawns must move forward 1 or 2 spaces.");
            
            //Ensure pawns don't move forward multiple spaces after their first move.
            if(Math.abs(dy) == 2 && getRow() != home)
                throw new IllegalMoveException("Pawns can't move 2 spaces after the first move.");
            
            //Ensure pawns move forward.
            if(dy * dir < 0)
                throw new IllegalMoveException("Pawns must move in the forward direction.");
            
            //Ensure pawns don't move sideways more than 1 space.
            if(Math.abs(dx) > 1)
                throw new IllegalMoveException("Pawns can't move more than 1 space to the side.");
            
            //Ensure pawns can't move sideways while also moving 2 spaces forward.
            if(Math.abs(dx) == 1 && Math.abs(dy) == 2)
                throw new IllegalMoveException("Pawns can't move diagonally 2 spaces forward.");
            
            //Ensure pawns can't capture when moving straight.
            if(Math.abs(dx) == 0 && getPiece(x_to, y_to).isPresent())
                throw new IllegalMoveException("Pawns can't capture when moving straight.");
            
            //Ensure pawns can't move diagonally without capturing.
            if(Math.abs(dx) == 1 && !getPiece(x_to, y_to).isPresent())
                throw new IllegalMoveException("Pawns must capture when moving diagonally.");
            
            //Move the pawn.
            setBoardPos(x_to, y_to);
        }
    }
    
    /**
     * Represents a rook chess piece.
     * @author Alec Dorrington
     */
    private class Rook extends Piece {
        
        Rook(int ownerId, int x, int y) {
            super(ownerId, x, y, ROOK_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    /**
     * Represents a knight chess piece.
     * @author Alec Dorrington
     */
    private class Knight extends Piece {
        
        Knight(int ownerId, int x, int y) {
            super(ownerId, x, y, KNIGHT_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            // TODO Auto-generated method stub
            
        }
    }
    
    /**
     * Represents a bishop chess piece.
     * @author Alec Dorrington
     */
    private class Bishop extends Piece {
        
        Bishop(int ownerId, int x, int y) {
            super(ownerId, x, y, BISHOP_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            // TODO Auto-generated method stub
            
        }
    }
    
    /**
     * Represents a queen chess piece.
     * @author Alec Dorrington
     */
    private class Queen extends Piece {
        
        Queen(int ownerId, int x, int y) {
            super(ownerId, x, y, QUEEN_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            // TODO Auto-generated method stub
            
        }
    }
    
    /**
     * Represents a king chess piece.
     * @author Alec Dorrington
     */
    private class King extends Piece {
        
        King(int ownerId, int x, int y) {
            super(ownerId, x, y, KING_TEXTURES[ownerId - 1]);
        }

        @Override
        public void movePiece(int x_to, int y_to) {
            // TODO Auto-generated method stub
            
        }
    }
}