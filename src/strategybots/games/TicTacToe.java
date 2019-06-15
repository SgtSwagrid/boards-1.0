package strategybots.games;

import java.util.HashSet;
import java.util.Set;

import strategybots.games.util.Board.Pattern;
import strategybots.games.util.TileGame;
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
    public boolean placeStone(int x, int y) {
        
        //Ensure move is valid.
        if(!validateMove(x, y)) return false;
        
        //Place a new stone at the specified location.
        new Stone(getCurrentPlayerId(), x, y);
        
        endTurn();
        checkWin(getCurrentPlayerId(), x, y);
        return true;
    }
    
    /**
     * Determine whether a move is valid.
     * @param x the x position to check.
     * @param y the y position to check.
     * @return whether the given move is valid.
     */
    public boolean validateMove(int x, int y) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone()) return false;
        
        //Ensure position is in bounds.
        if(x<0 || y<0 || x>=getWidth() || y>=getHeight()) return false;
        
        //Ensure specified location is empty.
        if(getPiece(x, y).isPresent()) return false;
        
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
        return getPiece(x, y).isPresent() ? getPiece(x, y).get().getOwnerId() : 0;
    }
    
    @Override
    protected void init() {
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOURS);
    }
    
    /**
     * Determines if the piece placed at the given position has caused a win.<br>
     * End the game and highlight the streak if this is the case.
     * @param x the x position to check for a win.
     * @param y the y position to check for a win.
     */
    private void checkWin(int playerId, int x, int y) {
        
        //For each possible streak direction.
        for(int xx = -1; xx <= 1; xx++) {
            yy_loop: for(int yy = -1; yy <= 1; yy++) {
                
                //(0, 0) is not a valid streak direction.
                if(xx == 0 && yy == 0) continue yy_loop;
                
                //Skip this direction if no streak can possibly fit in the bounds of the board.
                if(x+xx*(target-1)<0 || y+yy*(target-1)<0 || x+xx*(target-1)>=getWidth()
                        || y+yy*(target-1)>=getHeight()) continue yy_loop;
                
                Set<Stone> streak = new HashSet<>();
                
                //For each tile in the streak kernel.
                for(int i = 0; i < target; i++) {
                    
                    //Skip if there exists a tile in the streak kernel which doesn't
                    //contain a piece belonging to the current player.
                    if(!getPiece(x+xx*i, y+yy*i).isPresent()) continue yy_loop;
                    if(getPiece(x+xx*i, y+yy*i).get().getOwnerId()!=playerId) continue yy_loop;
                    
                    streak.add((Stone)getPiece(x+xx*i, y+yy*i).get());
                }
                //A winning streak has been found. This player is the winner.
                endGame(playerId);
                
                //Highlight the pieces in the winning streak.
                for(Stone piece : streak) {
                    getBoard().setColour(piece.getCol(), piece.getRow(),
                            (piece.getCol()+piece.getRow())%2==0 ?
                            HIGHLIGHT_COLOUR : HIGHLIGHT_COLOUR.darken(0.05F));
                }
            }
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
        }

        @Override
        public boolean movePiece(int x_to, int y_to) { return false; }
    }
}