package games;

import games.util.ChessBoard;
import games.util.Game;
import games.util.Piece;
import games.util.Player;

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
public class Chess extends Game {
    
    /** The dimensions of the board. */
    private static final int WIDTH = 8, HEIGHT = 8;
    
    /** Title of the window. */
    private static final String TITLE = "Chess";
    
    /** The board on which the game is played - manages tile layout. */
    private ChessBoard board = new ChessBoard(8, 8, TITLE);
    
    /** Array of pieces on the board. */
    @SuppressWarnings("unchecked")
    private Piece<Chess>[][] pieces = new Piece[WIDTH][HEIGHT];
    
    /** Whether the current turn has been completed. */
    private volatile boolean turnTaken = false;
    
    /**
     * Asynchronously runs a new Chess instance.
     * @param whitePlayer the player controlling the white pieces.
     * @param blackPlayer the player controlling the black pieces.
     */
    public Chess(Player<Chess> whitePlayer, Player<Chess> blackPlayer) {
        super(new Player[] {whitePlayer, blackPlayer});
        //Start the game.
        start();
    }
    
    @Override
    protected void init() {
        
        
    }
    
    @Override
    protected void setupTurn() {
        
        
    }
    
    @Override
    protected void postTurn() {
        
    }
    
    
}