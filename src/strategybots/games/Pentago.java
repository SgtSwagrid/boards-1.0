package strategybots.games;

import java.util.HashSet;
import java.util.Set;

import strategybots.games.util.Board.Pattern;
import strategybots.graphics.Button;
import strategybots.graphics.Colour;
import strategybots.graphics.Tile;
import strategybots.graphics.Window;

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
    
    /** Board size, quadrant size and target. */
    private static final int WIDTH = 6, HEIGHT = 6, QUADRANT_SIZE = 3, TARGET = 5;
    
    /** Textures used for game pieces. */
    private static final String[] STONE_TEXTURES = new String[] {
            "res/misc/white_dot.png", "res/misc/black_dot.png"};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "White", "Black"};
    
    /** Background tile colours. */
    private static final Colour[] BOARD_COLOURS = new Colour[] {
            Colour.rgb(248, 239, 186), Colour.rgb(234, 181, 67)};
    
    private volatile boolean piecePlaced = false;
    
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
    public boolean placeStone(int x, int y) {
        
        //Ensure move is valid.
        if(!validatePlacement(x, y)) return false;
        
        //Place a new stone at the specified location.
        new Stone(getCurrentPlayerId(), x, y);
        
        piecePlaced = true;
        checkWin(getCurrentPlayerId(), x, y);
        return true;
    }
    
    /**
     * Rotates a quadrant by 90 degrees.<br>
     * Must be called exactly once per turn, AFTER a piece is placed.
     * @param x the x coordinate of the quadrant to rotate.
     * @param y the y coordinate of the quadrant to rotate.
     * @param clockwise true=clockwise, false=anticlockwise.
     * @return whether the move was valid and successful.
     */
    public boolean rotateQuadrant(int x, int y, boolean clockwise) {
        
        //Ensure rotation is valid.
        if(!validateRotation(x, y, clockwise)) return false;
        
        int[][] pieces = new int[QUADRANT_SIZE][QUADRANT_SIZE];
        
        //For each square in the quadrant.
        for(int xx = 0; xx < QUADRANT_SIZE; xx++) {
            for(int yy = 0; yy < QUADRANT_SIZE; yy++) {
                
                //Get the piece in this position.
                int ownerId = getStone(x*QUADRANT_SIZE+xx, y*QUADRANT_SIZE+yy);
                
                //Add the piece to the pieces array, rotated by 90 degrees.
                pieces[clockwise?y:QUADRANT_SIZE-y-1][clockwise?QUADRANT_SIZE-x-1:x] = ownerId;
                
                //Delete the piece.
                if(ownerId != 0) {
                    getPieceInst(x*QUADRANT_SIZE+xx, y*QUADRANT_SIZE+yy).get().delete();
                }
            }
        }
        //For each square in the quadrant.
        for(int xx = 0; xx < QUADRANT_SIZE; xx++) {
            for(int yy = 0; yy < QUADRANT_SIZE; yy++) {
                
                if(pieces[xx][yy] != 0) {
                    //Move each piece to its new rotated position.
                    new Stone(getCurrentPlayerId(),
                            x*QUADRANT_SIZE+xx, y*QUADRANT_SIZE+yy);
                }
            }
        }
        endTurn();
        return true;
    }
    
    /**
     * Determines whether a rotation is valid.
     * @param x the x coordinate of the quadrant to rotate.
     * @param y the y coordinate of the quadrant to rotate.
     * @param clockwise true=clockwise, false=anticlockwise.
     * @return whether the given move is valid.
     */
    public boolean validateRotation(int x, int y, boolean clockwise) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone() || !piecePlaced) return false;
        //Ensure quadrant coordinates are in bounds.
        if(x<0 || y<0 || x>=getWidth()/QUADRANT_SIZE ||
                y>=getHeight()/QUADRANT_SIZE) return false;
        return true;
    }
    
    @Override
    public boolean validatePlacement(int x, int y) {
        
        //Ensure piece hasn't already been placed this turn.
        if(piecePlaced) return false;
        //Ensure placement is valid.
        return super.validatePlacement(x, y);
    }
    
    /**
     * @return whether a piece has yet been placed on this turn.
     */
    public boolean stonePlaced() { return piecePlaced; }
    
    @Override
    protected void init() {
        setTitle(TITLE);
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOURS);
    }
    
    @Override
    protected void preTurn() {
        super.preTurn();
        piecePlaced = false;
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
            if(game.placeStone(x, y)) {
                
                Set<Tile> rotateArrows = new HashSet<>();
                
                
            }
        }
    }
    
    private class RotateArrow extends Button {

        public RotateArrow(Window window, int x, int y) {
            super(window);
            //setSize();
        }
        
        
    }
}