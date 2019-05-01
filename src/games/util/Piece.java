package games.util;

import swagui.api.Colour;
import swagui.api.Tile;

/**
 * Supertype for pieces which are used on a chess board.
 * 
 * @author Alec Dorrington
 *
 * @param <G> the game to which this piece belongs.
 */
public abstract class Piece<G extends Game> extends Tile {
    
    /** The chessboard on which this piece resides. */
    private ChessBoard board;
    
    /** The owner of this piece. */
    private Player<G> owner;
    /** The ID of the owner of this piece. */
    private int ownerId;
    
    /** The board position of this piece. */
    protected int x, y;
    
    /**
     * Constructs a new piece of a particular owner at a particular position.
     * @param board the chessboard on which this piece resides.
     * @param owner the owner of this piece.
     * @param ownerId the ID of the owner of this piece.
     * @param x the x position of this piece.
     * @param y the y position of this piece.
     */
    protected Piece(ChessBoard board, Player<G> owner, int ownerId, int x, int y) {
        
        super(board.getWindow());
        this.board = board;
        
        //Set the owner of this piece.
        this.owner = owner;
        this.ownerId = ownerId;
        
        //Add the piece to the board.
        board.setPosition(this, x, y);
        
        this.x = x;
        this.y = y;
        
        //Match the size of the piece to the grid size of the board.
        setSize(ChessBoard.TILE_SIZE, ChessBoard.TILE_SIZE);
        setColour(Colour.WHITE);
    }
    
    /**
     * @return the board on which this piece resides.
     */
    public ChessBoard getBoard() { return board; }
    
    /**
     * @return the player to whom this piece belongs.
     */
    public Player<G> getOwner() { return owner; }
    
    /**
     * @return the ID of the player to whom this piece belongs.
     */
    public int getOwnerId() { return ownerId; }
    
    /**
     * @return the current x position of this piece on the board.
     */
    public int getCol() { return x; }
    
    /**
     * @return the current y position of this piece on the board.
     */
    public int getRow() { return y; }
    
    /**
     * Called by the game to move a piece to a particular position.<br>
     * Implementations of this method should provide their own move validation.<br>
     * An exception may be thrown if a move is invalid.
     * @param x_to the x position to move to.
     * @param y_to the y position to move to.
     * @throws IllegalMoveException
     */
    public abstract void movePiece(int x_to, int y_to);
}