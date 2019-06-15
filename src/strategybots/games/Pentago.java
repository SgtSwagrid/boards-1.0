package strategybots.games;

import strategybots.games.util.Board.Pattern;
import strategybots.graphics.Colour;

/**
 * <b>Pentago implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Pentago">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Pentago>'.<br>
 * Human players can be made by instantiating 'PentagoController'.
 * 
 * @author Alec Dorrington
 */
public class Pentago extends TicTacToe {
    
    /** Title of the window. */
    private static final String TITLE = "Pentago";
    
    /** Board size and target. */
    private static final int WIDTH = 6, HEIGHT = 6, TARGET = 5;
    
    /** Textures used for game pieces. */
    private static final String[] STONE_TEXTURES = new String[] {
            "res/misc/white_dot.png", "res/misc/black_dot.png"};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "White", "Black"};
    
    /** Background tile colours. */
    private static final Colour[] BOARD_COLOURS = new Colour[] {
            Colour.rgb(248, 239, 186), Colour.rgb(234, 181, 67)};
    
    /**
     * Asynchronously runs a new Pentago instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param target pieces in a row required to win.
     * @param player1 the first (blue/naughts) player to participate.
     * @param player2 the second (red/crosses) player to participate.
     */
    public Pentago(Player<Pentago> player1, Player<Pentago> player2) {
        super(WIDTH, HEIGHT, TARGET, player1, player2);
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
        return STONE_TEXTURES[playerId-1];
    }
    
    @Override
    protected Colour getStoneColour(int playerId) {
        return Colour.WHITE;
    }
    
    /**
     * Implementation of Player<Pentago> for use in inserting a human-controlled player.<br>
     * Each PentagoController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class PentagoController extends Controller<Pentago> {
        
        public PentagoController() {}
        
        public PentagoController(String name) { super(name); }
        
        @Override
        public void onTileClicked(Pentago game, int playerId, int x, int y) {
            //Place a piece.
            game.placeStone(x, y);
        }
    }
}