package strategybots.games;

import java.util.HashSet;
import java.util.Set;

import strategybots.games.base.TileGame;
import strategybots.games.base.Board.Pattern;
import strategybots.graphics.Colour;

/**
 * <b>Tic-Tac-Toe/M,N,K-Game implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/M,n,k-game">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<TicTacToe>'.<br>
 * Human players can be made by instantiating 'TicTacToeController'.
 * 
 * @author Alec Dorrington
 */
public class TicTacToe extends TileGame {
    
    /** Title of the window. */
    private static final String TITLE = "Tic Tac Toe";
    
    /** Default board settings. */
    private static final int WIDTH = 7, HEIGHT = 7, TARGET = 5;
    
    /** Textures used for game pieces. */
    private static final String[] STONE_TEXTURES = new String[] {
            "res/misc/naught.png", "res/misc/cross.png"};
    
    /** Player piece colours. */
    private static final Colour[] STONE_COLOURS = new Colour[] {
            Colour.rgb(87, 95, 207), Colour.rgb(255, 94, 87)};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Blue", "Red"};
    
    /** Background tile colours. */
    private static final Colour[] BOARD_COLOURS = new Colour[] {
            Colour.rgb(245, 246, 250), Colour.rgb(220, 221, 225)};
    
    /** Colour used for highlighted pieces. */
    private static Colour HIGHLIGHT_COLOUR = Colour.rgb(126, 214, 223);
    
    /** Number of pieces in a row required to win. */
    private int target;
    
    /** The current number of pieces on the board. */
    private volatile int numPieces = 0;
    
    /**
     * Asynchronously runs a new Tic-Tac-Toe instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param target pieces in a row required to win.
     * @param player1 the first (blue/naughts) player to participate.
     * @param player2 the second (red/crosses) player to participate.
     */
    public TicTacToe(int width, int height, int target,
            Player<? extends TicTacToe> player1,
            Player<? extends TicTacToe> player2) {
        super(width, height, TITLE, player1, player2);
        this.target = target;
    }
    
    /**
     * Asynchronously runs a new Tic-Tac-Toe instance,
     * using a default board size of 7x7 and a target of 5.
     * @param player1 the first (blue/naughts) player to participate.
     * @param player2 the second (red/crosses) player to participate.
     */
    public TicTacToe(Player<? extends TicTacToe> player1,
            Player<? extends TicTacToe> player2) {
        this(WIDTH, HEIGHT, TARGET, player1, player2);
    }
    
    /**
     * Places a new stone at the given position.<br>
     * Must be called exactly once per turn.<br>
     * @param x the x position to place the piece at.
     * @param y the y position to place the piece at.
     * @return whether the move was valid and successful.
     */
    public synchronized boolean placeStone(int x, int y) {
        
        //Ensure move is valid.
        if(!validatePlacement(x, y)) return false;
        
        //Place a new stone at the specified location.
        new Stone(getCurrentPlayerId(), x, y);
        
        checkWinAtPiece(getCurrentPlayerId(), x, y);
        
        endTurn();
        return true;
    }
    
    /**
     * Determine whether a move is valid.
     * @param x the x position to check.
     * @param y the y position to check.
     * @return whether the given move is valid.
     */
    public boolean validatePlacement(int x, int y) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone()) return false;
        
        //Ensure position is in bounds.
        if(x<0 || y<0 || x>=getWidth() || y>=getHeight()) return false;
        
        //Ensure specified location is empty.
        if(getPieceInst(x, y).isPresent()) return false;
        
        return true;
    }
    
    /**
     * Returns the owner of the piece currently at the given position.<br>
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
        return getPieceInst(x, y).isPresent() ? getPieceInst(x, y).get().getOwnerId() : 0;
    }
    
    /**
     * @return the number of stones in a row required to win.
     */
    public int getTarget() { return target; }
    
    @Override
    protected void init() {
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOURS);
    }
    
    @Override
    protected void checkEnd() {
        
        //The game is a draw if the board is full.
        if(numPieces == getWidth() * getHeight()) endGame(0);
    }
    
    /**
     * Determines if the piece placed at the given position has caused a win.<br>
     * End the game and highlight the streak if this is the case.
     * @param playerId the ID of the player to check for a win.
     * @param x the x position to check for a win.
     * @param y the y position to check for a win.
     * @return whether a win has occurred.
     */
    protected void checkWinAtPiece(int playerId, int x, int y) {
        
        if(getStone(x, y) != playerId) return;
        
        //For each of the four directions in which a streak could occur.
        for(int dir = 0; dir < 4; dir++) {
            
            //The x and y components of this direction.
            int x_dir = dir<2 ? 1 : dir==2 ? 0:-1;
            int y_dir = dir==0 ? 0:1;
            
            //The set of pieces in the streak in this orientation.
            Set<Piece> streak = new HashSet<>();
            streak.add(getPieceInst(x, y).get());
            
            //For each sub-streak on the 2 sides of the piece.
            for(int sign = -1; sign <= 1; sign += 2) {
                
                //Keep searching until the end of the streak is found.
                for(int i = 1;; i++) {
                    
                    //The position currently being checked for a piece.
                    int xx = x + i*x_dir*sign;
                    int yy = y + i*y_dir*sign;
                    
                    //The streak has ended if the edge of the board is reached.
                    if(!inBounds(xx, yy)) break;
                    //The streak has ended if an enemy piece or empty square has been reached.
                    if(getStone(xx, yy) != playerId) break;
                    
                    streak.add(getPieceInst(xx, yy).get());
                }
            }
            
            //If this streak is sufficient in size to win the game.
            if(streak.size() >= target) {
                endGame(playerId);
                highlightStreak(streak);
            }
        }
    }
    
    /**
     * Highlights the given set of pieces on the board.
     * For use in providing visual indication of winning streaks.
     * @param streak the set of pieces to highlight.
     */
    private void highlightStreak(Set<Piece> streak) {
        
        for(Piece piece : streak) {
            
            //Alternate colour slightly according to checkerboard pattern.
            Colour colour = (piece.getCol()+piece.getRow())%2==0 ?
                    HIGHLIGHT_COLOUR : HIGHLIGHT_COLOUR.darken(0.05F);
            
            //Highlight board square on which the piece resides.
            getBoard().setColour(piece.getCol(), piece.getRow(), colour);
        }
    }
    
    @Override
    protected String getPlayerName(int playerId) {
        return getPlayer(playerId).getName() + " ("+COLOUR_NAMES[playerId-1]+")";
    }
    
    /**
     * @param playerId the player whose stone texture to get.
     * @return the texture for use by this players' stones.
     */
    protected String getStoneTexture(int playerId) {
        return STONE_TEXTURES[playerId-1];
    }
    
    /**
     * @param playerId the player whose colour to get.
     * @return the colour for use by this players' stones.
     */
    protected Colour getStoneColour(int playerId) {
        return STONE_COLOURS[playerId-1];
    }
    
    /**
     * Implementation of Player<TicTacToe> for use in inserting a human-controlled player.<br>
     * Each TicTacToeController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class TicTacToeController extends Controller<TicTacToe> {
        
        public TicTacToeController() {}
        
        public TicTacToeController(String name) { super(name); }
        
        @Override
        public void onTileClicked(TicTacToe game, int playerId, int x, int y) {
            //Place a piece.
            game.placeStone(x, y);
        }
    }
    
    /**
     * Represents a TicTacToe game piece.
     * @author Alec Dorrington
     */
    protected class Stone extends Piece {
        
        Stone(int ownerId, int x, int y) {
            super(ownerId, x, y, getStoneTexture(ownerId));
            setColour(getStoneColour(ownerId));
            numPieces++;
        }

        @Override
        public boolean movePiece(int x_to, int y_to) { return false; }
        
        @Override
        public void delete() {
            super.delete();
            numPieces--;
        }
    }
}