package strategybots.games;

import strategybots.games.util.TileGame;
import strategybots.games.util.Board.Pattern;
import strategybots.graphics.Colour;
import strategybots.graphics.Texture;
import strategybots.graphics.Tile;

/**
 * <b>Chomp implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Chomp">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Chomp>'.<br>
 * Human players can be made by instantiating 'ChompController'.
 * 
 * @author Alec Dorrington
 */
public class Chomp extends TileGame {
    
    /** Title of the window. */
    private static final String TITLE = "Chomp";
    
    /** Colours of the chomped tiles of each player. */
    private static final Colour[] PLAYER_COLOURS = new Colour[] {
            Colour.rgb(87, 95, 207), Colour.rgb(5, 196, 107)};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Blue", "Green"};
    
    /** Background tile colours. */
    private static final Colour BOARD_COLOUR1 = Colour.rgb(141, 110, 99);
    private static final Colour BOARD_COLOUR2 = Colour.rgb(93, 64, 55);
    
    /** Stores whether the tile at each position has been chomped. */
    private boolean[][] chomped;
    /** The running total number of tiles which have been chomped. */
    private int numChomped = 0;
    
    /**
     * Asynchronously runs a new Chomp instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param player1 the first (blue) player to participate.
     * @param player2 the second (green) player to participate.
     */
    public Chomp(int width, int height, Player<Chomp> player1, Player<Chomp> player2) {
        super(width, height, TITLE, player1, player2);
    }
    
    /**
     * Will chomp (remove from play) the tile at the given coordinates,
     * along with all other tiles above and to the right of it.
     * @param x the x position to chomp from.
     * @param y the y position to chomp from.
     * @return whether the move was valid and successful.
     */
    public boolean chompTile(int x, int y) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnTaken()) return false;
        
        //Ensure position is in bounds.
        if(!inBounds(x, y)) return false;
        
        //Ensure chomp location isn't already chomped.
        if(chomped[x][y]) return false;
        
        //Perform a chomp on all tiles above and to the right of the given location.
        for(int xx = x; xx < getWidth(); xx++) {
            for(int yy = y; yy < getHeight(); yy++) {
                
                //For each tile which isn't already chomped.
                if(!chomped[xx][yy]) {
                    
                    //Set the tile to chomped and increment the chomp counter.
                    chomped[xx][yy] = true;
                    numChomped++;
                    
                    //Recolour the tile to indicate that it was chomped by the current player.
                    getBoard().setColour(xx, yy, (xx + yy) % 2 == 0 ?
                            PLAYER_COLOURS[getCurrentPlayerId() - 1].lighten(0.1F) :
                            PLAYER_COLOURS[getCurrentPlayerId() - 1]);
                }
            }
        }
        setTurnTaken();
        return true;
    }
    
    /**
     * Returns whether the tile at the given grid position is chomped.
     * @param x the x position to check.
     * @param y the y position to check.
     * @return true if this tile is chomped, false otherwise.
     */
    public boolean isTileChomped(int x, int y) { return chomped[x][y]; }
    
    @Override
    protected void init() {
        
        chomped = new boolean[getWidth()][getHeight()];
        
        //Set the board colours.
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOUR1, BOARD_COLOUR2);
        
        //Create a poison marker on the bottom-left tile.
        Tile poison = new Tile(getWindow());
        poison.setTexture(Texture.getTexture("res/misc/poison.png"));
        poison.setColour(Colour.WHITE);
        getBoard().setPosition(poison, 0, 0);
    }
    
    @Override
    protected void checkWin() {
        
        //The player loses if they chomp the poison tile.
        if(chomped[0][0]) {
            endGame(getCurrentPlayerId() % 2 + 1);
            
        //The player wins if there are no available tiles to chomp.
        } else if(numChomped == getWidth() * getHeight() - 1) {
            endGame(getCurrentPlayerId());
        }
    }
    
    @Override
    protected String getPlayerName(int playerId) {
        return getPlayer(playerId).getName() + " ("+COLOUR_NAMES[playerId-1]+")";
    }
    
    /**
     * Implementation of Player<Chomp> for use in inserting a human-controlled player.<br>
     * Each ChompController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class ChompController extends Controller<Chomp> {
        
        public ChompController() {}
        
        public ChompController(String name) { super(name); }
        
        @Override
        public void onTileClicked(Chomp game, int playerId, int x, int y) {
            //Attempt to chomp this tile.
            game.chompTile(x, y);
        }
    }
}