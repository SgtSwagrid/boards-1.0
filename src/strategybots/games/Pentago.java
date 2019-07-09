package strategybots.games;

import strategybots.event.Event;
import strategybots.games.util.Board.Pattern;
import strategybots.graphics.Button;
import strategybots.graphics.Colour;
import strategybots.graphics.Texture;
import strategybots.graphics.Window.WindowResizeEvent;

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
    
    /** Texture used for rotation arrows. */
    private static final String ARROW_TEXTURE = "res/misc/rotation_arrows.png";
    /** Texture used for rotation arrows with clockwise highlighted. */
    private static final String CLOCKWISE_ARROW_TEXTURE = "res/misc/rotation_arrows_clockwise.png";
    /** Texture used for rotation arrows with anticlockwise highlighted. */
    private static final String ANTICLOCKWISE_ARROW_TEXTURE = "res/misc/rotation_arrows_anticlockwise.png";
    
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
                pieces[clockwise?yy:QUADRANT_SIZE-yy-1][clockwise?QUADRANT_SIZE-xx-1:xx] = ownerId;
                
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
                    new Stone(pieces[xx][yy],
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
            if(!game.piecePlaced && game.placeStone(x, y) && game.isRunning()) {
                //Show rotation panel.
                new RotationPanel(game);
            }
        }
    }
    
    private static class RotationPanel {
        
        Pentago game;
        
        Button[] buttons = new RotationButton[4];
        
        RotationPanel(Pentago game) {
            
            this.game = game;
            
            for(int i=0; i<4; i++) {
                buttons[i] = new RotationButton(i%2, i/2);
            }
        }
        
        void delete() {
            for(Button button:buttons) {
                button.delete();
            }
        }
        
        private class RotationButton extends Button {
            
            int x, y;
            
            RotationButton(int x, int y) {
                
                super(game.getBoard().getWindow());
                game.getBoard().setInputEnabled(false);
                
                this.x=x;
                this.y=y;
                
                setSize();
                Event.addHandler(WindowResizeEvent.class, e -> setSize());
                
                setAngle(x==0 ? (y==0?180:270) : (y==0?90:0));
                setDepth(1.0F);
                
                setColour(Colour.WHITE.withAlpha(180));
                setTexture(Texture.getTexture(ARROW_TEXTURE));
            }
            
            @Override
            protected void onLeftClick(int rx, int ry) {
                
                if(game.rotateQuadrant(x, y, isClockwise(rx, ry))) {
                    RotationPanel.this.delete();
                    game.getBoard().setInputEnabled(true);
                }
            }
            
            @Override
            protected void onMouseOver(int rx, int ry) {
                setTexture(Texture.getTexture(isClockwise(rx, ry) ?
                        CLOCKWISE_ARROW_TEXTURE : ANTICLOCKWISE_ARROW_TEXTURE));
            }
            
            @Override
            protected void onMouseLeave(int rx, int ry) {
                setTexture(Texture.getTexture(ARROW_TEXTURE));
            }
            
            private boolean isClockwise(int rx, int ry) {
                
                if(rx==0) return false;
                
                return x==0 ?
                    (y==0 ?
                        ry/rx > getHeight()/getWidth() :
                        (float)ry/getHeight() + (float)rx/getWidth() > 1.0F) :
                    (y==0 ?
                        (float)ry/getHeight() + (float)rx/getWidth() < 1.0F :
                        ry/rx < getHeight()/getWidth());
            }
            
            private void setSize() {
                
                int size = 2*game.getBoard().getTileSize();
                setSize(size, size);
                setPosition((x*2-1)*game.getBoard().getBoardWidth()/4,
                        (y*2-1)*game.getBoard().getBoardHeight()/4);
            }
        }
    }
}