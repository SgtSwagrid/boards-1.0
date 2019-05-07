package games.util;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import swagui.api.Colour;
import swagui.api.Tile;
import swagui.api.Window;

/**
 * Abstract supertype for abstract board games using a basic grid board.<br>
 * Takes care of turn order progression, the game board and piece movement.
 * 
 * @author Alec Dorrington
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class GridGame extends Game {
    
    /** The window in which the board resides. */
    private final Window window;
    
    /** The board on which the game is played - manages tile layout. */
    private final ChessBoard board;
    
    /** Grid of pieces indexed by position. */
    private final Piece[][] boardPieces;
    private final Set<Piece>[] playerPieces;
    
    /** The dimensions of the board. */
    private final int width, height;
    
    /** Title of the window. */
    private final String title;
    
    /**
     * Constructs a new grid game of the given dimensions and players.
     * @param width the number of tiles wide the board is.
     * @param height the number of tiles high the board is.
     * @param title the title of the window in which the board is to reside.
     * @param players the players (in turn order) participating in this game.
     */
    protected GridGame(int width, int height, String title, Player... players) {
        
        //Load the players.
        super(players);
        
        //Create the game board.
        board = new ChessBoard(width, height, title);
        boardPieces = new Piece[width][height];
        window = board.getWindow();
        
        playerPieces = new Set[players.length];
        for(int i = 0; i < players.length; i++) {
            playerPieces[i] = new HashSet<>();
        }
        
        //Set the dimensions.
        this.width = width;
        this.height = height;
        this.title = title;
        
        //Start the game.
        start();
    }
    
    /**
     * @return the width of the game board.
     */
    public int getWidth() { return width; }
    
    /**
     * @return the height of the game board.
     */
    public int getHeight() { return height; }
    
    /**
     * @return the window in which the game board exists.
     */
    protected Window getWindow() { return window; }
    
    /**
     * @return the board upon which the game is being played.
     */
    protected ChessBoard getBoard() { return board; }
    
    /**
     * Sets the position of a piece.
     * @param piece the piece for which to set the position.
     * @param x the new x position.
     * @param y the new y position.
     */
    protected void setPosition(Piece piece, int x, int y) {
        
        //Remove the piece from its previous board index.
        if(boardPieces[piece.getCol()][piece.getRow()] == piece)
            boardPieces[piece.getCol()][piece.getRow()] = null;
        
        //Add the piece to its new board index.
        boardPieces[x][y] = piece;
        playerPieces[piece.getOwnerId() - 1].add(piece);
        
        //Set the graphical positon of the piece.
        board.setPosition(piece, x, y);
        
        piece.x = x;
        piece.y = y;
    }
    
    /**
     * Finds the piece at a position on the board, if on exists.
     * @param x the x position to check.
     * @param y the y position to check.
     * @return the piece at the given position, if there is one.
     */
    protected Optional<Piece> getPiece(int x, int y) {
        return Optional.ofNullable(boardPieces[x][y]);
    }
    
    /**
     * Returns a set of all the pieces owned by a particular player.
     * @param owner_id the ID of the player whose pieces to return.
     * @return all the pieces owned by this player.
     */
    protected Set<Piece> getPieces(int owner_id) {
        return playerPieces[owner_id - 1];
    }
    
    @Override
    public boolean isRunning() {
        return super.isRunning() && board.getWindow().isOpen();
    }
    
    @Override
    protected void setupTurn() {
        //Set the title to indicate the players' turn.
        window.setTitle(title + " - " + getCurrentPlayer().getName() + "'s Turn ("
                + getColourName(getCurrentPlayerId()) + ")");
    }
    
    @Override
    protected void verifyTurn() {
        //Ensure the player completed their turn.
        if(!turnTaken() && getWindow().isOpen())
            throw new IllegalMoveException("Player did not complete turn.");
    }
    
    @Override
    protected void onFinish() {
        //Display the winner of the game.
        if(getWinner().isPresent()) {
            getBoard().getWindow().setTitle(title + " - " + getWinner().get().getName()
                    + " (" + getColourName(getCurrentPlayerId()) + ") has won!");
            
        }
    }
    
    /**
     * Supertype for pieces which are used on a chess board.
     * @author Alec Dorrington
     * @param <G> the game to which this piece belongs.
     */
    protected abstract class Piece extends Tile {
        
        /** The owner of this piece. */
        private Player<?> owner;
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
        protected Piece(int ownerId, int x, int y) {
            
            super(board.getWindow());
            
            //Set the owner of this piece.
            owner = getPlayer(ownerId);
            this.ownerId = ownerId;
            
            //Update the position of this piece.
            GridGame.this.setPosition(this, x, y);
            
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
        public Player<?> getOwner() { return owner; }
        
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
}