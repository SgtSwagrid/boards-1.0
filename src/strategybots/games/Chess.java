package strategybots.games;

import java.util.Optional;

import strategybots.games.base.TileGame;

/**
 * THIS IMPLEMENTATION IS INCOMPLETE. DO NOT USE.
 * 
 * <b>Chess implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Chess">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Chess>'.<br>
 * Human players can be made by instantiating 'ChessController'.
 * 
 * @author Alec Dorrington
 */
public class Chess extends TileGame {
    
    private static final long serialVersionUID = 3768927483479016678L;

    /** The set of piece types in chess. */
    public enum Chessman { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }
    
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
    
    /** The pawn to be promoted if one exists. */
    private Optional<Piece> promotion = Optional.empty();
    
    /**
     * Asynchronously runs a new Chess instance.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Chess(Player<Chess> player1, Player<Chess> player2) {
        super(WIDTH, HEIGHT, TITLE, player1, player2);
    }
    
    /**
     * Moves the piece at the given position to a new position.<br>
     * Both positions must be within the bounds of the board.<br>
     * Can only be used while the turn is active and the game is running.<br>
     * @param x_from the x position at which the piece currently resides.
     * @param y_from the y position at which the piece currently resides.
     * @param x_to the x position to which the piece should be moved.
     * @param y_to the y position to which the piece should be moved.
     * @return whether the move was valid and successful.
     */
    public boolean movePiece(int x_from, int y_from, int x_to, int y_to) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone()) return false;
        
        //Ensure positions are in bounds.
        if(!inBounds(x_from, y_from) || !inBounds(x_to, y_to)) return false;
        
        //Ensure there is a piece at the from location.
        if(!getPieceInst(x_from, y_from).isPresent()) return false;
        
        //Move the piece, subject to game constraints.
        if(!getPieceInst(x_from, y_from).get().movePiece(x_to, y_to)) return false;
        
        endTurn();
        return true;
    }
    
    public boolean promotePawn(Chessman newType) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone()) return false;
        
        //Ensure there is a piece ready to be promoted.
        if(!promotion.isPresent()) return false;
        
        //switch(newType) {
            
            //TODO
            
            
            
            
        //}
        
        return true;
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
            if(game.getPieceInst(x, y).isPresent() &&
                    game.getPieceInst(x, y).get().getOwnerId() == playerId) {
                
                //Select this piece.
                selectPiece(game, game.getPieceInst(x, y).get());
                
            //If the current player has a piece selected.
            } else if(getSelected().isPresent()) {
                
                //Move the selected piece to this location.
                if(game.movePiece(getSelected().get().getCol(),
                        getSelected().get().getRow(), x, y)) {
                    
                    unselectPiece(game);
                }
            }
        }
    }
    
    /**
     * Represents a pawn chess piece.
     * @author Alec Dorrington
     */
    private class Pawn extends Piece {
         
        private static final long serialVersionUID = -761873584935853190L;
        
        /** Whether the pawn moved forwards 2 spaces on the previous turn. Used for en passant. */
        private boolean movedDouble = false;
        
        Pawn(int ownerId, int x, int y) {
            super(ownerId, x, y, PAWN_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            
            //Unit value in direction pawn should be moving.
            int dir = getOwnerId() == 1 ? 1 : -1;
            //The y value at which the pawn started.
            int home = getOwnerId() == 1 ? 1 : 6;
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId()) return false;
            
            //Ensure players can't capture their own pieces.
            if(getPieceInst(x_to, y_to).isPresent() && getPieceInst(getCol(), getRow()).get().getOwnerId()
                    == getPieceInst(x_to, y_to).get().getOwnerId()) return false;
            
            //Ensure pawns move forward the correct number of spaces.
            if(Math.abs(dy) < 1 || Math.abs(dy) > 2) return false;
            
            //Ensure pawns don't move forward multiple spaces after their first move.
            if(Math.abs(dy) == 2 && getRow() != home) return false;
            
            //Ensure pawns move forward.
            if(dy * dir < 0) return false;
            
            //Ensure pawns can't jump over other pieces.
            if(Math.abs(dy) == 2 && getPieceInst(getCol(), getRow() + dir).isPresent()) return false;
            
            //Ensure pawns don't move sideways more than 1 space.
            if(Math.abs(dx) > 1) return false;
            
            //Ensure pawns can't move sideways/capture while also moving 2 spaces forward.
            if(Math.abs(dx) == 1 && Math.abs(dy) == 2) return false;
            
            //Ensure pawns can't capture when moving straight.
            if(Math.abs(dx) == 0 && getPieceInst(x_to, y_to).isPresent()) return false;
            
            //Ensure pawns can't move diagonally without capturing.
            if(Math.abs(dx) == 1 && !getPieceInst(x_to, y_to).isPresent()) {
                
                //If no piece is directly captured, an en passant capture must have been made.
                
                //Ensure piece captured by en passant exsits.
                if(!getPieceInst(x_to, y_to-dy).isPresent()) return false;
                
                //Ensure piece captured by en passant is a pawn.
                if(!(getPieceInst(x_to, y_to-dy).get() instanceof Pawn)) return false;
                
                //Ensure piece captured by en passant moved forward 2 spaces.
                if(!((Pawn)getPieceInst(x_to, y_to-dy).get()).movedDouble) return false;
                
                //Ensure piece captured by en passant isn't your own piece.
                if(getPieceInst(x_to, y_to-dy).get().getOwnerId() == getOwnerId()) return false;
                
                //Remove a piece captured by en passant.
                getPieceInst(x_to, y_to-dy).get().delete();
            }
            
            //Move the pawn.
            movedDouble = Math.abs(dy) == 2;
            setBoardPos(x_to, y_to);
            return true;
        }
    }
    
    /**
     * Represents a rook chess piece.
     * @author Alec Dorrington
     */
    private class Rook extends Piece {
        
        private static final long serialVersionUID = 4875419301198346425L;
        
        /** Whether the piece has yet been moved. Used for castling. */
        private boolean moved = false;
        
        Rook(int ownerId, int x, int y) {
            super(ownerId, x, y, ROOK_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId()) return false;
            
            //Ensure players can't capture their own pieces.
            if(getPieceInst(x_to, y_to).isPresent() && getPieceInst(getCol(), getRow()).get().getOwnerId()
                    == getPieceInst(x_to, y_to).get().getOwnerId()) return false;
            
            //Ensure rooks move at least one space along only one dimension.
            if(dx==0 ^ dy!=0) return false;
            
            int xx = getCol() + (int)Math.signum(dx);
            int yy = getRow() + (int)Math.signum(dy);
            
            //For each position between the from and to locations.
            while(xx != x_to || yy != y_to) {
                
                //Ensure rooks can't jump over other pieces.
                if(getPieceInst(xx, yy).isPresent()) return false;
                
                xx += Math.signum(dx);
                yy += Math.signum(dy);
            }
            moved = true;
            setBoardPos(x_to, y_to);
            return true;
        }
    }
    
    /**
     * Represents a knight chess piece.
     * @author Alec Dorrington
     */
    private class Knight extends Piece {
        
        private static final long serialVersionUID = 5733044206284824747L;

        Knight(int ownerId, int x, int y) {
            super(ownerId, x, y, KNIGHT_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId()) return false;
            
            //Ensure players can't capture their own pieces.
            if(getPieceInst(x_to, y_to).isPresent() && getPieceInst(getCol(), getRow()).get().getOwnerId()
                    == getPieceInst(x_to, y_to).get().getOwnerId()) return false;
            
            //Ensure knights move 1 square in one direction and 2 squares in the other.
            if(!(Math.abs(dx) == 1 && Math.abs(dy) == 2) &&
                    !(Math.abs(dx) == 2 && Math.abs(dy) == 1)) return false;
            
            setBoardPos(x_to, y_to);
            return true;
        }
    }
    
    /**
     * Represents a bishop chess piece.
     * @author Alec Dorrington
     */
    private class Bishop extends Piece {
        
        private static final long serialVersionUID = -3070814962520252515L;

        Bishop(int ownerId, int x, int y) {
            super(ownerId, x, y, BISHOP_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId()) return false;
            
            //Ensure players can't capture their own pieces.
            if(getPieceInst(x_to, y_to).isPresent() && getPieceInst(getCol(), getRow()).get().getOwnerId()
                    == getPieceInst(x_to, y_to).get().getOwnerId()) return false;
            
            //Ensure bishops move diagonally.
            if(Math.abs(dx) != Math.abs(dy)) return false;
            
            //A move can't have a piece remain still.
            if(dx == 0 && dy == 0) return false;
            
            int xx = getCol() + (int)Math.signum(dx);
            int yy = getRow() + (int)Math.signum(dy);
            
            //For each position between the from and to locations.
            while(xx != x_to || yy != y_to) {
                
                //Ensure bishops can't jump over other pieces.
                if(getPieceInst(xx, yy).isPresent()) return false;
                
                xx += Math.signum(dx);
                yy += Math.signum(dy);
            }
            setBoardPos(x_to, y_to);
            return true;
        }
    }
    
    /**
     * Represents a queen chess piece.
     * @author Alec Dorrington
     */
    private class Queen extends Piece {
        
        private static final long serialVersionUID = -2344156684729093731L;

        Queen(int ownerId, int x, int y) {
            super(ownerId, x, y, QUEEN_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId()) return false;
            
            //Ensure players can't capture their own pieces.
            if(getPieceInst(x_to, y_to).isPresent() && getPieceInst(getCol(), getRow()).get().getOwnerId()
                    == getPieceInst(x_to, y_to).get().getOwnerId()) return false;
            
            //Ensure queens move straight or diagonally.
            if(Math.abs(dx) != Math.abs(dy) && dx==0 ^ dy!=0) return false;
            
            //A move can't have a piece remain still.
            if(dx == 0 && dy == 0) return false;
            
            int xx = getCol() + (int)Math.signum(dx);
            int yy = getRow() + (int)Math.signum(dy);
            
            //For each position between the from and to locations.
            while(xx != x_to && yy != y_to) {
                
                //Ensure queens can't jump over other pieces.
                if(getPieceInst(xx, yy).isPresent()) return false;
                
                xx += Math.signum(dx);
                yy += Math.signum(dy);
            }
            setBoardPos(x_to, y_to);
            return true;
        }
    }
    
    /**
     * Represents a king chess piece.
     * @author Alec Dorrington
     */
    private class King extends Piece {
        
        private static final long serialVersionUID = -7906130242443041647L;
        /** Whether the piece has yet been moved. Used for castling. */
        private boolean moved = false;
        
        King(int ownerId, int x, int y) {
            super(ownerId, x, y, KING_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            
            //The difference between the current position and the new position.
            int dx = x_to - getCol(), dy = y_to - getRow();
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != getCurrentPlayerId()) return false;
            
            //Ensure players can't capture their own pieces.
            if(getPieceInst(x_to, y_to).isPresent() && getPieceInst(getCol(), getRow()).get().getOwnerId()
                    == getPieceInst(x_to, y_to).get().getOwnerId()) return false;
            
            //A move can't have a piece remain still.
            if(dx == 0 && dy == 0) return false;
            
            //The king must castle if it moves 2 spaces horizontally.
            if(Math.abs(dx) == 2 && Math.abs(dy) == 0) {
                
                //Ensure king hasn't been moved.
                if(moved) return false;
                
                //Ensure piece exists at the expected location for castling.
                if(!getPieceInst(dx<0?0:7, getRow()).isPresent()) return false;
                
                //Ensure other piece used for castling is a rook.
                if(!(getPieceInst(dx<0?0:7, getRow()).get() instanceof Rook)) return false;
                
                //Ensure the rook hasn't been moved.
                if(((Rook)getPieceInst(dx<0?0:7, getRow()).get()).moved) return false;
                
                //Ensure space between king and rook is empty.
                for(int xx = getCol() + dx/2; xx>0 && xx<7; xx += dx/2) {
                    if(getPieceInst(xx, getRow()).isPresent()) return false;
                }
                
                //Move the rook to the position over which the king jumped.
                getPieceInst(dx<0?0:7, getRow()).get().setBoardPos(getCol() + dx/2, getRow());
                
            //Ensure a king can't move further than one space.
            } else if(Math.abs(dx)>1 || Math.abs(dy)>1) return false;
            
            moved = true;
            setBoardPos(x_to, y_to);
            return true;
        }
    }
}