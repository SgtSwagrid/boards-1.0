package strategybots.games;

import strategybots.games.base.TileGame;
import strategybots.games.base.Board.Pattern;
import strategybots.games.graphics.Colour;

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
public class DotsAndBoxes extends TileGame {
    
    private static final long serialVersionUID = 6284684201192673782L;

    /** Represents an orientation, horizontal or vertical. */
    public enum Orien { HORZ, VERT }
    
    /** Represents a particular side of a square. */
    public enum Side { RIGHT, TOP, LEFT, BOTTOM }
    
    /** Title of the window. */
    private static final String TITLE = "Dots and Boxes";
    
    /** Default board dimensions. */
    private static final int WIDTH = 10, HEIGHT = 10;
    
    /** Colours of the chomped tiles of each player. */
    private static final Colour[] PLAYER_COLOURS = new Colour[] {
            Colour.rgb(87, 95, 207), Colour.rgb(255, 94, 87)};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Blue", "Red"};
    
    /** Background colours. */
    private static final Colour BACKGROUND_COLOUR = Colour.rgb(245, 246, 250);
    private static final Colour LINE_COLOUR = Colour.rgb(220, 221, 225);
    private static final Colour DOT_COLOUR = Colour.rgb(255, 177, 66);
    
    /** Square:Line width ratio (x10). */
    private static final int SQUARE_WIDTH = 30;
    
    /** The size of the board, in number of squares. */
    private final int width, height;
    
    /** The current state of the board regarding line placements.
     *  True for each location at which a horizontal or
     *  vertical line has been placed respectively. */
    private boolean[][] h_lines, v_lines;
    
    /** The number of walls adjacent to each square. */
    private int[][] numSides;
    
    private int totalScore = 0;
    private int[] scores;
    
    /**
     * Asynchronously runs a new Dots and Boxes instance.
     * @param width the width of the board.
     * @param height the height of the board.
     * @param player1 the first (blue) player to participate.
     * @param player2 the second (red) player to participate.
     */
    public DotsAndBoxes(int width, int height, Player<DotsAndBoxes> player1, Player<DotsAndBoxes> player2) {
        super(2 * width + 1, 2 * height + 1, TITLE, player1, player2);
        this.width = width;
        this.height = height;
    }
    
    /**
     * Asynchronously runs a new Dots and Boxes instance,
     * using a default board size of 6x6 squares.
     * @param player1 the first (blue) player to participate.
     * @param player2 the second (red) player to participate.
     */
    public DotsAndBoxes(Player<DotsAndBoxes> player1, Player<DotsAndBoxes> player2) {
        this(WIDTH, HEIGHT, player1, player2);
    }
    
    /**
     * Draws a new line segment in the given orientation at the given position.<br>
     * The horizontal and vertical lines operate on two independent coordinate systems.<br>
     * Coordinates all start from (0, 0) in the bottom-left corner.<br>
     * The coordinates given must be valid and inside the bounds of the board.
     * @param orien the orientation of the line to draw.
     * @param x the x position of the line to draw.
     * @param y the y position of the line to draw.
     * @return whether the move was valid and successful.
     */
    public synchronized boolean drawLine(Orien orien, int x, int y) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone()) return false;
        
        //Determine which set of lines to modify based on the orientation.
        boolean[][] lines = orien == Orien.HORZ ? h_lines : v_lines;
        
        //Ensure move is in bounds.
        if(x >= lines.length || y >= lines[0].length) return false;
        
        //Ensure no such line already exists.
        if(lines[x][y]) return false;
        
        //Mark the line as having been drawn.
        lines[x][y] = true;
        
        //Draw the line.
        int xx = x*2 + (orien==Orien.HORZ?1:0);
        int yy = y*2 + (orien==Orien.VERT?1:0);
        getBoard().setColour(xx, yy, PLAYER_COLOURS[getCurrentPlayerId()-1].darken(0.1F));
        
        //Capture any squares which this move completed.
        //The turn is over when a move is made which captures no squares.
        if(captureSquares(orien, x, y) == 0 || totalScore == width * height)
            endTurn();
        return true;
    }
    
    /**
     * Draws a new line segment on the given side of the square at the given position.<br>
     * Coordinates start from (0, 0) on the bottom-left corner.<br>
     * The coordinates given must be valid and inside the bounds of the board.
     * @param side the side of the square on which a line is drawn.
     * @param x the x position of the square next to which a line is drawn.
     * @param y the y position of the square next to which a line is drawn.
     * @return whether the move was valid and successful.
     */
    public synchronized boolean drawLine(Side side, int x, int y) {
        
        switch(side) {
            
            //Determine the actual position of the line based on the direction.
            //Use the actual position to call the previous drawLine() method.
            case RIGHT: return drawLine(Orien.HORZ, x+1, y);
            case TOP: return drawLine(Orien.VERT, x, y+1);
            case LEFT: return drawLine(Orien.HORZ, x, y);
            case BOTTOM: return drawLine(Orien.VERT, x, y);
            default: return false;
        }
    }
    
    /**
     * Determines whether a line segment has yet been drawn at the given position.<br>
     * The horizontal and vertical lines operate on two independent coordinate systems.<br>
     * Coordinates all start from (0, 0) in the bottom-left corner.<br>
     * The coordinates given must be valid and inside the bounds of the board.
     * @param orien the orientation of the line to check for.
     * @param x the x position to check for a line.
     * @param y the y position to check for a line.
     * @return whether there is a line at this position.
     */
    public boolean hasLine(Orien orien, int x, int y) {
        return (orien == Orien.HORZ ? h_lines : v_lines)[x][y];
    }
    
    /**
     * Determines whether a line segment has yet been drawn at the given position.<br>
     * Coordinates start from (0, 0) on the bottom-left corner.<br>
     * The coordinates given must be valid and inside the bounds of the board.
     * @param side the side of the square on which to check for a line.
     * @param x the x position of the square next to which to check for a line.
     * @param y the y position of the square next to which to check for a line.
     * @return whether there is a line at this position.
     */
    public boolean hasLine(Side side, int x, int y) {
        
        switch(side) {
            
            //Determine the actual position of the line based on the direction.
            //Use the actual position to call the previous hasLine() method.
            case RIGHT: return drawLine(Orien.HORZ, x+1, y);
            case TOP: return drawLine(Orien.VERT, x, y+1);
            case LEFT: return drawLine(Orien.HORZ, x, y);
            case BOTTOM: return drawLine(Orien.VERT, x, y);
            default: return false;
        }
    }
    
    @Override
    public int getWidth() { return width; }
    
    @Override
    public int getHeight() { return height; }
    
    /**
     * Checks for captures on the squares adjacent to a placed line.
     * @param orien the orientation of the line.
     * @param x the x position of the line.
     * @param y the y position of the line.
     * @return the number of captures which were made (0-2).
     */
    private int captureSquares(Orien orien, int x, int y) {
        
        Colour colour = PLAYER_COLOURS[getCurrentPlayerId()-1].lighten(0.2F);
        
        //Increment the number of sides for each adjacent square.
        //Increment score for each square which reaches 4 sides.
        
        int score = 0;
        
        int xx = orien == Orien.VERT?x-1:x;
        int yy = orien == Orien.HORZ?y-1:y;
        
        //Check the square on the negative side of the line.
        if(xx>=0 && yy>=0 && ++numSides[xx][yy] == 4) {
            score++; totalScore++;
            getBoard().setColour(2*xx+1, 2*yy+1, colour);
        }
        
        //Check the square on the positive side of the line.
        if(x<width && y<height && ++numSides[x][y] == 4) {
            score++; totalScore++;
            getBoard().setColour(2*x+1, 2*y+1, colour);
        }
        scores[getCurrentPlayerId()-1] += score;
        return score;
    }
    
    @Override
    protected void init() {
        
        //Set the background colours on the board.
        getBoard().setBackground(Pattern.GINGHAM, BACKGROUND_COLOUR, LINE_COLOUR, DOT_COLOUR);
        
        //Increase the relative width of columns with horizontal lines.
        for(int x = 1; x < getBoard().getWidth(); x += 2) {
            getBoard().setColWidth(x, SQUARE_WIDTH);
        }
        
        //Increase the relative height of rows with vertical lines.
        for(int y = 1; y < getBoard().getWidth(); y += 2) {
            getBoard().setRowHeight(y, SQUARE_WIDTH);
        }
        
        //Create boolean arrays for storing lines.
        h_lines = new boolean[width][height+1];
        v_lines = new boolean[width+1][height];
        
        numSides = new int[width][height];
        scores = new int[getNumPlayers()];
    }
    
    @Override
    protected void checkEnd() {
        
        int maxScore = 0, winner = 0;
        
        for(int i = 0; i < scores.length; i++) {
            
            //Determine the maximum score.
            
            if(scores[i] == maxScore) {
                winner = 0;
            }
            
            if(scores[i] > maxScore) {
                maxScore = scores[i];
                winner = i+1;
            }
        }
        
        //If all possible squares are claimed.
        if(totalScore == width * height) {
            
            //The player with the highest score wins.
            endGame(winner);
        }
    }
    
    @Override
    protected String getPlayerName(int playerId) {
        return getPlayer(playerId).getName() + " ("+COLOUR_NAMES[playerId-1]+")";
    }
    
    /**
     * Implementation of Player<Dots> for use in inserting a human-controlled player.<br>
     * Each DotsController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class DotsController extends Controller<DotsAndBoxes> {
        
        public DotsController() {}
        
        public DotsController(String name) { super(name); }
        
        @Override
        public void onTileClicked(DotsAndBoxes game, int playerId, int x, int y) {
            
            //Draw a line segment at the clicked position.
            if(x % 2 == 1 && y % 2 == 0) game.drawLine(Orien.HORZ, x/2, y/2);
            if(x % 2 == 0 && y % 2 == 1) game.drawLine(Orien.VERT, x/2, y/2);
        }
    }
}