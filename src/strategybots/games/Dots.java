package strategybots.games;

import strategybots.games.util.TileGame;
import strategybots.games.util.Board.Pattern;
import strategybots.graphics.Colour;

/**
 * <b>Dots and Boxes implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Dots_and_Boxes">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Dots>'.<br>
 * Human players can be made by instantiating 'DotsController'.
 * 
 * @author Alec Dorrington
 */
public class Dots extends TileGame {
    
    /** Title of the window. */
    private static final String TITLE = "Dots and Boxes";
    
    /** Colours of the chomped tiles of each player. */
    private static final Colour[] PLAYER_COLOURS = new Colour[] {
            Colour.rgb(87, 95, 207), Colour.rgb(5, 196, 107)};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Blue", "Green"};
    
    /** Background colours. */
    private static final Colour BACKGROUND_COLOUR = Colour.rgb(223, 249, 251);
    private static final Colour LINE_COLOUR = Colour.BLACK;
    private static final Colour BORDER_COLOUR = Colour.rgb(149, 175, 192);
    
    /** The size of the board, in number of squares. */
    private final int WIDTH, HEIGHT;
    
    /** The current state of the board regarding line placements.
     *  True for each location at which a horizontal or
     *  vertical line has been placed respectively. */
    private boolean[][] h_lines, v_lines;
    
    private int[] scores = new int[2];
    
    public Dots(int width, int height, Player<Dots> player1, Player<Dots> player2) {
        super(2 * width + 1, 2 * height + 1, TITLE, player1, player2);
        WIDTH = width;
        HEIGHT = height;
    }
    
    @Override
    protected void init() {
        
        getBoard().setBackground(BACKGROUND_COLOUR, BORDER_COLOUR, Pattern.TABLE);
        
        for(int x = 1; x < getBoard().getWidth(); x += 2) {
            getBoard().setColWidth(x, 3);
        }
        
        for(int y = 1; y < getBoard().getWidth(); y += 2) {
            getBoard().setRowHeight(y, 3);
        }
        
        //Create boolean arrays for storing lines.
        h_lines = new boolean[WIDTH + 1][HEIGHT];
        v_lines = new boolean[WIDTH][HEIGHT + 1];
    }
    
    /**
     * Implementation of Player<Dots> for use in inserting a human-controlled player.<br>
     * Each DotsController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class DotsController extends Controller<Dots> {
        
        public DotsController() {}
        
        public DotsController(String name) { super(name); }
        
        @Override
        public void onTileClicked(Dots game, int playerId, int x, int y) {
            
        }
    }
}