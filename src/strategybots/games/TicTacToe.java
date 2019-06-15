package strategybots.games;

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
    
    /** Background tile colours. */
    private static final Colour[] STONE_COLOURS = new Colour[] {
            Colour.rgb(87, 95, 207), Colour.rgb(255, 94, 87)};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "Blue", "Red"};
    
    /** Background tile colours. */
    private static final Colour[] BOARD_COLOURS = new Colour[] {
            Colour.rgb(245, 246, 250), Colour.rgb(220, 221, 225)};
    
    public TicTacToe(int width, int height, int target, Player<TicTacToe> player1, Player<TicTacToe> player2) {
        super(width, height, TITLE, player1, player2);
    }
    
    @Override
    protected void init() {
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOURS);
        new Stone(1, 4, 4);
        new Stone(2, 3, 3);
    }
    
    private class Stone extends Piece {
        
        Stone(int ownerId, int x, int y) {
            super(ownerId, x, y, STONE_TEXTURES[ownerId-1]);
            setColour(STONE_COLOURS[ownerId-1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) { return false; }
    }
}