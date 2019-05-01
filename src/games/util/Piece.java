package games.util;

import swagui.api.Colour;
import swagui.api.Tile;

public abstract class Piece<G extends Game> extends Tile {
    
    /** The chessboard on which this piece resides. */
    private Chessboard board;
    
    /** The owner of this piece. */
    private Player<G> owner;
    /** The ID of the owner of this piece. */
    private int ownerId;
    
    /** The board position of this piece. */
    private int x, y;
    
    /**
     * Constructs a new piece of a particular owner at a particular position.
     * @param board the chessboard on which this piece resides.
     * @param owner the owner of this piece.
     * @param ownerId the ID of the owner of this piece.
     * @param x the x position of this piece.
     * @param y the y position of this piece.
     */
    protected Piece(Chessboard board, Player<G> owner, int ownerId, int x, int y) {
        
        super(board.getWindow());
        
        this.board = board;
        
        //Set the owner of this piece.
        this.owner = owner;
        this.ownerId = ownerId;
        
        //Add the piece to the board.
        board.addPiece(this, x, y);
        
        this.x = x;
        this.y = y;
        
        //Match the size of the piece to the grid size of the board.
        setSize(Chessboard.TILE_SIZE, Chessboard.TILE_SIZE);
        setColour(Colour.WHITE);
    }
    
    public Chessboard getBoard() { return board; }
    
    public Player<G> getOwner() { return owner; }
    
    public int getOwnerId() { return ownerId; }
    
    public int getBoardX() { return x; }
    
    public int getBoardY() { return y; }
    
    protected void setPos(int x, int y) {
        
        board.movePiece(this.x, this.y);
        board.addPiece(this, x, y);
        this.x = x;
        this.y = y;
    }
}