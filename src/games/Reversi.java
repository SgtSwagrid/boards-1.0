package games;

import java.util.Optional;

import games.util.Chessboard;
import games.util.IllegalMoveException;
import swagui.api.Colour;
import swagui.api.Texture;
import swagui.api.Tile;

/**
 * <b>Reversi implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Reversi">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Reversi>'.<br>
 * Human players can be made by instantiating 'ReversiController'.
 * 
 * @author Alec Dorrington
 */
public class Reversi {
    
    /** Title of the window. */
    private static final String TITLE = "Reversi";
    
    /** Colour used for recently placed piece */
    public static final Colour HIGHLIGHT_COLOUR1 = Colour.rgb(249, 127, 81);
    
    /** Chessboard instance, manages the window, and piece layout. */
    private Chessboard board;
    
    /** Grid of pieces indexed by position. */
    private Piece[][] pieces;
    
    /** Players participating in this game. */
    private ReversiPlayer player1, player2;
    /** The winner of the game. */
    private Optional<ReversiPlayer> winner = Optional.empty();
    /** The player whose turn is currently active. */
    private ReversiPlayer currentPlayer;
    /** The ID of the player whose turn is currently active. */
    private volatile int currentPlayerId = 1;
    
    /** The dimensions of the game board. */
    private int width, height;
    
    /**
     * Asynchronously runs a new Reversi instance.
     * @param width the width of the game board.
     * @param height the height of the game board.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Reversi(int width, int height, ReversiPlayer player1, ReversiPlayer player2) {
        
        //Set the board dimensions.
        this.width = width;
        this.height = height;
        
        //Set the participating players.
        this.player1 = player1;
        this.player2 = player2;
        
        //Create a new game board, opening a window and placing initial pieces.
        createBoard();
        
        //Run the game simulation in a new thread.
        new Thread("Reversi") {
            @Override public void run() {
                Reversi.this.start();
            }
        }.start();
    }
    
    /**
     * Runs the game simulation to completion.<br>
     * Assumes the game board has already been created.
     */
    private void start() {
        
        //Call the implementation-specific initialisation code for each player.
        player1.init(this, 1);
        player2.init(this, 2);
        
        while(!winner.isPresent() && board.getWindow().isOpen()) {
            
          //Determine who is to be the next player.
          currentPlayer = currentPlayerId == 1 ? player1 : player2;
          String colour = currentPlayerId == 1 ? "White" : "Black";
          board.getWindow().setTitle(TITLE + " - " + currentPlayer.getName()
                  + "'s Turn (" + colour + ")");
            
        }
    }
    
    /**
     * Creates the board and lays out the initial 4 Pieces 
     */
    private void createBoard() {
        
        board = new Chessboard(width, height, TITLE);
        
        pieces = new Piece[width][height];
        
        // Setup the middle initial pieces, player1, then player2
        pieces[width / 2][height / 2] = new Piece(player1, 1, width / 2, height / 2);
        pieces[width / 2 - 1][height / 2 - 1] = new Piece(player1, 1, width / 2 - 1, height / 2 - 1);
        
        pieces[width / 2 - 1][height / 2] = new Piece(player2, 2, width / 2 - 1, height / 2);
        pieces[width / 2][height / 2 - 1] = new Piece(player2, 2, width / 2, height / 2 - 1);
    }
    
    /**
     * Returns the current state of the board.<br>
     * Each array index represents the piece currently at that position.<br>
     * <table border="1">
     * <tr><td>0</td><td>Empty Tile.</td></tr>
     * <tr><td>1</td><td>Piece owned by player 1.</td></tr>
     * <tr><td>2</td><td>Piece owned by player 2.</td></tr>
     * </table>
     * @return the game state.
     */
    public int[][] getState() {
        
        int[][] board = new int[width][height];
        
        //For each grid cell.
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                
                //Determine index of piece on this tile.
                board[x][y] = getPiece(x, y);
            }
        }
        return board;
    }
    
    /**
     * Returns the ID of the piece at a position.<br>
     * See 'getState()' for details.
     * @param x the x position of the piece.
     * @param y the y position of the piece.
     * @return the ID of the piece at this position.
     */
    private int getPiece(int x, int y) {
        
        //Empty tile: '-1'.
        if(pieces[x][y] == null)
            return 0;
        
        //Queen: ID of owner.
        else return pieces[x][y].ownerId;
    }
    
    /**
     * Supertype for all reversi player implementations.
     * @author Alec Dorrington
     */
    public interface ReversiPlayer {
        
        /**
         * Called once before the game begins.
         * @param game the game being played.
         * @param playerId the ID of this player.
         */
        default void init(Reversi game, int playerId) {}
        
        /**
         * Called once whenever the player is expected to take a turn.<br>
         * 
         * TODO turn expectations in this comment
         * 
         * exactly once each, in order, before returning.
         * @param game the game being played.
         * @param playerId the ID of this player.
         */
        void takeTurn(Reversi game, int playerId);
        
        /**
         * Called to determine the name of this player.<br>
         * Used only for display purposes.
         * @return the player name (defaults to "Bot").
         */
        default String getName() { return "Bot"; }
    }
    
    public static final class ReversiController implements ReversiPlayer {
        
        /** The display name of this player. */
        private String name = "Controller";
        
        /**
         * Constructs a new ReversiController with the default name of "Controller".
         */
        public ReversiController() {}
        
        /** 
         * Constructs a new ReversiController with the given name.
         * @param name the display name of this controller.
         */
        public ReversiController(String name) { this.name = name; }
        
        @Override
        public void init(Reversi game, int playerId) {
            
        	//Add a click listener to each grid cell on the board.
            for(int i = 0; i < game.width; i++) {
                for(int j = 0; j < game.height; j++) {
                    
                    int x = i, y = j;
                    game.board.addListener(x, y, () -> {
                        
                        //Listeners should only be active on your own turn.
                        if(playerId != game.currentPlayerId)
                            return;
                        
                        // Place a piece
                        placePiece(game, playerId, x, y);
                    });
                }
            }
        }
        
        /**
         * Place a piece once per turn
         * @param game The Reversi Game
         * @param playerId The Id of the placing player
         * @param x X coordinate of the piece to be placed
         * @param y Y coordinate of the piece to be placed
         */
        public void placePiece(Reversi game, int playerId, int x, int y) {
            
        	//Select an empty location.
            if(game.pieces[x][y] == null) {
                
            	try {
            		//Try to place the selected piece to its new tile.
            		game.placePiece(playerId, x, y);
            	
            		//Set the selected piece and highlight its tile.
            		game.board.resetColours();
            		game.board.setColour(x, y, HIGHLIGHT_COLOUR1);
                    
                //Invalid moves should be ignored.
                } catch(IllegalMoveException e) {}
            }
        }
        
        @Override
        public void takeTurn(Reversi game, int playerId) {
            
        }
        
        @Override
        public String getName() { return name; }
    }
    
    private class Piece extends Tile {
        
        /** The owner of this piece. */
        ReversiPlayer owner;
        /** The ID of the owner of this piece. */
        int ownerId;
        
        /** The board position of this piece. */
        int x, y;
        
        /**
         * Constructs a new piece of a particular owner at a particular position.
         * @param owner the owner of this piece.
         * @param ownerId the ID of the owner of this piece.
         * @param x the x position of this piece.
         * @param y the y position of this piece.
         */
        Piece(ReversiPlayer owner, int ownerId, int x, int y) {
            
            super(board.getWindow());
            
            //Set the owner of this piece.
            this.owner = owner;
            this.ownerId = ownerId;
            
            //Store this piece in the pieces array.
            pieces[x][y] = this;
            //Set the graphical position of this tile.
            board.addPiece(this, x, y);
            this.x = x;
            this.y = y;
            
            //Match the size of the piece to the cell size of the chessboard.
            setSize(Chessboard.TILE_SIZE, Chessboard.TILE_SIZE);
            setColour(Colour.WHITE);
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/draughts/white_piece.png" : "res/draughts/black_piece.png"));
        }
        
        /**
         * Ensures that a move is valid.<br>
         * Throws an exception otherwise.
         * @param x the x position to which a piece is being placed.
         * @param y the y position to which a piece is being placed.
         * @throws IllegalMoveException
         */
        void validateMove(int x, int y) {
            
            //Ensure piece is owned by the current player.
            if(ownerId != currentPlayerId)
                throw new IllegalMoveException("Can't move an opponents piece.");
            
            //Ensure piece doesn't move on top of another piece.
            if(pieces[x][y] != null)
                throw new IllegalMoveException("Position is not empty.");
            
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
                
                if(pieces[xx][yy] != null)
                    throw new IllegalMoveException("Can't jump over other pieces.");
                
                xx += x_sign;
                yy += y_sign;
            }
        }
    }
}