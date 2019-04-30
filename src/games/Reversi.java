package games;

import java.util.Optional;

import games.util.Chessboard;
import swagui.api.Colour;
import swagui.api.Texture;
import swagui.api.Tile;

/**
 * <b>Reversi implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Reversi">Wikipedia</a><br>
 * <br>
 * Bot players can be made by extending 'ReversiPlayer'.<br>
 * Human players can be made by instantiating 'ReversiController'.
 * 
 * @author Alec Dorrington
 */
public class Reversi {
    
    /** Title of the window. */
    private static final String TITLE = "Reversi";
    
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
    
    private void createBoard() {
        
        board = new Chessboard(width, height, TITLE);
        
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
            board.movePiece(this, x, y);
            this.x = x;
            this.y = y;
            
            //Match the size of the piece to the cell size of the chessboard.
            setSize(Chessboard.TILE_SIZE, Chessboard.TILE_SIZE);
            setColour(Colour.WHITE);
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/draughts/white_piece.png" : "res/draughts/black_piece.png"));
        }
    }
}