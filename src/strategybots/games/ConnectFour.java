package strategybots.games;

import strategybots.games.base.Board.Pattern;
import strategybots.graphics.Colour;

/**
 * <b>Connect Four implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Connect_Four">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<ConnectFour>'.<br>
 * Human players can be made by instantiating 'ConnectFourController'.
 * 
 * @author Alec Dorrington
 */
public class ConnectFour extends TicTacToe {
    
    /** Title of the window. */
    private static final String TITLE = "Connect Four";
    
    /** Default board settings. */
    private static final int WIDTH = 7, HEIGHT = 6, TARGET = 4;
    
    /** Texture used for game pieces. */
    private static final String STONE_TEXTURE = "res/misc/white_dot.png";
    
    /** Player piece colours. */
    private static final Colour[] STONE_COLOURS = new Colour[] {
            Colour.rgb(249, 202, 36), Colour.rgb(255, 94, 87)};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Yellow", "Red"};
    
    /** Background tile colours. */
    private static final Colour[] BOARD_COLOURS = new Colour[] {
            Colour.rgb(75, 123, 236), Colour.rgb(56, 103, 214)};
    
    /**
     * Asynchronously runs a new Connect Four instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param target pieces in a row required to win.
     * @param player1 the first (blue/naughts) player to participate.
     * @param player2 the second (red/crosses) player to participate.
     */
    public ConnectFour(int width, int height, int target,
            Player<ConnectFour> player1, Player<ConnectFour> player2) {
        super(width, height, target, player1, player2);
    }
    
    /**
     * Asynchronously runs a new Connect Four instance,
     * using a default board size of 7x6 and a target of 4.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param target pieces in a row required to win.
     * @param player1 the first (blue/naughts) player to participate.
     * @param player2 the second (red/crosses) player to participate.
     */
    public ConnectFour(Player<ConnectFour> player1, Player<ConnectFour> player2) {
        super(WIDTH, HEIGHT, TARGET, player1, player2);
    }
    
    public synchronized boolean placeStone(int x) {
        
        //Ensure x is in bounds and columns isn't full.
        if(!validatePlacement(x)) return false;
        
        //Place the piece at the top of this column.
        return placeStone(x, getStackSize(x));
    }
    
    /**
     * Determines the number of pieces in the given column.
     * @param x the x coordinate of the column to check.
     * @return the number of pieces in this column.
     */
    public int getStackSize(int x) {
        
        for(int y = 0; y < getHeight(); y++) {
            
            if(!getPieceInst(x, y).isPresent()) return y;
        }
        return getHeight();
    }
    
    @Override
    public boolean validatePlacement(int x, int y) {
        
        //Ensure move is a valid Tic-Tac-Toe move.
        if(!super.validatePlacement(x, y)) return false;
        
        //Ensure piece isn't floating.
        if(y!=0 && !getPieceInst(x, y-1).isPresent()) return false;
        
        return true;
    }
    
    /**
     * Determine whether a move is valid.
     * @param x the x position to check.
     * @return whether the given move is valid.
     */
    public boolean validatePlacement(int x) {
        //Ensure x is in bounds and column isn't full.
        return x>=0 && x<getWidth() && getStackSize(x)<getHeight();
    }
    
    @Override
    protected void init() {
        setTitle(TITLE);
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOURS);
    }
    
    @Override
    protected String getPlayerName(int playerId) {
        return getPlayer(playerId).getName() + " ("+COLOUR_NAMES[playerId-1]+")";
    }
    
    @Override
    protected String getStoneTexture(int playerId) {
        return STONE_TEXTURE;
    }
    
    @Override
    protected Colour getStoneColour(int playerId) {
        return STONE_COLOURS[playerId-1];
    }
    
    /**
     * Implementation of Player<ConnectFour> for use in inserting a human-controlled player.<br>
     * Each ConnectFourController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class ConnectFourController extends Controller<ConnectFour> {
        
        public ConnectFourController() {}
        
        public ConnectFourController(String name) { super(name); }
        
        @Override
        public void onTileClicked(ConnectFour game, int playerId, int x, int y) {
            //Place a piece.
            game.placeStone(x);
        }
    }
}