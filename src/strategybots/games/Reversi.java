package strategybots.games;

import java.util.HashSet;
import java.util.Set;

import strategybots.games.base.TileGame;
import strategybots.games.base.Board.Pattern;
import strategybots.graphics.Colour;

/**
 * <b>Reversi/Othello implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Reversi">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Reversi>'.<br>
 * Human players can be made by instantiating 'ReversiController'.
 * 
 * @author Alec Dorrington
 */
public class Reversi extends TileGame {
    
    /** Title of the window. */
    private static final String TITLE = "Reversi";
    
    /** Default board dimensions. */
    private static final int WIDTH = 8, HEIGHT = 8;
    
    /** Textures used for game pieces. */
    private static final String[] DISC_TEXTURES = new String[] {
            "res/misc/white_dot.png", "res/misc/black_dot.png"};
    
    /** The display name of the colour of each player. */
    private static final String[] COLOUR_NAMES = new String[] {
            "White", "Black"};
    
    /** Background tile colours. */
    private static final Colour BOARD_COLOUR1 = Colour.rgb(123, 237, 159);
    private static final Colour BOARD_COLOUR2 = Colour.rgb(46, 213, 115);
    
    /**
     * Asynchronously runs a new Reversi instance.
     * @param width the width of the game board.
     * @param height the height of the game board.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Reversi(int width, int height, Player<Reversi> player1, Player<Reversi> player2) {
        super(width, height, TITLE, player1, player2);
    }
    
    /**
     * Asynchronously runs a new Reversi instance,
     * using a default board size of 10x10.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Reversi(Player<Reversi> player1, Player<Reversi> player2) {
        this(WIDTH, HEIGHT, player1, player2);
    }
    
    /**
     * Places a new disc at the given position.<br>
     * Must be called exactly once per turn.<br>
     * Move must be consistent with the rules of the game.
     * @param x the x position to place the piece at. 
     * @param y the y position to place the piece at.
     * @return whether the move was valid and successful.
     */
    public synchronized boolean placeDisc(int x, int y) {
        
        //Ensure game is running and turn hasn't already been taken.
        if(!isRunning() || turnDone()) return false;
        
        //Ensure position is in bounds.
        if(!inBounds(x, y)) return false;
        
        //Ensure pieces are placed onto empty tiles.
        if(getPieceInst(x, y).isPresent()) return false;
        
        //Determine which enemy pieces this move would flip.
        Set<Disc> flipped = getFlipped(getCurrentPlayerId(), x, y);
        
        //Ensure each move flips at least one enemy piece.
        if(flipped.size() == 0) return false;
        
        //Create a new piece at the chosen location.
        new Disc(getCurrentPlayerId(), x, y);
        
        //Replace all the flipped enemy pieces with friendly pieces.
        for(Disc disc : flipped) {
            disc.delete();
            new Disc(getCurrentPlayerId(), disc.getCol(), disc.getRow());
        }
        
        endTurn();
        return true;
    }
    
    /**
     * Returns the disc currently at the given position.<br>
     * <table border="1">
     * <tr><td>0</td><td>Empty tile.</td></tr>
     * <tr><td>1</td><td>Piece owned by player 1.</td></tr>
     * <tr><td>2</td><td>Piece owned by player 2.</td></tr>
     * </table>
     * @param x the x position at which to check for a piece.
     * @param y the y position at which to check for a piece.
     * @return the piece at (x, y) on the board.
     */
    public int getDisc(int x, int y) {
        return getPieceInst(x, y).isPresent() ? getPieceInst(x, y).get().getOwnerId() : 0;
    }
    
    /**
     * Determines which discs are to be captured if a disc
     * is placed at the given position by the given player.
     * @param playerId the ID of the player making the move.
     * @param x the x position the disc is placed at.
     * @param y the y position the disc is placed at.
     * @return a set of all the opponent discs which are captured, if any.
     */
    private Set<Disc> getFlipped(int playerId, int x, int y) {
        
        Set<Disc> flipped = new HashSet<>(), pending = new HashSet<>();
        
        //For each direction in which to check for captures.
        for(int i = -1; i <= 1; i++) {
            j_loop: for(int j = -1; j <= 1; j++) {
                
                if(i == 0 && j == 0) continue;
                
                pending.clear();
                
                //Continue searching in this direction.
                for(int dist = 1;; dist++) {
                    
                    //Determine the current position to check for a disc.
                    //Position = Placement + Direction * Distance.
                    int xx = x + i * dist;
                    int yy = y + j * dist;
                    
                    //If this tile is in bounds and not empty.
                    if(xx >= 0 && xx < getWidth() && yy >= 0 && yy < getHeight()
                            && getPieceInst(xx, yy).isPresent()) {
                        
                        //If this tile is occupied by the opponent player.
                        if(getPieceInst(xx, yy).get().getOwnerId() != playerId) {
                            //Add the disc to the set of discs which MIGHT be added.
                            pending.add((Disc) getPieceInst(xx, yy).get());
                            
                        //If this tile is occupied by the current player.
                        } else {
                            //An enclosed chain has been found.
                            flipped.addAll(pending);
                            continue j_loop;
                        }
                    //Empty tile/edge of board has been reached before a friendly piece.
                    } else continue j_loop;
                }
            }
        }
        return flipped;
    }
    
    @Override
    protected void init() {
        
        //Set the board colours.
        getBoard().setBackground(Pattern.CHECKER, BOARD_COLOUR1, BOARD_COLOUR2);
        
        //Place the initial pieces on the board.
        new Disc(1, getWidth()/2-1, getHeight()/2);
        new Disc(1, getWidth()/2, getHeight()/2-1);
        new Disc(2, getWidth()/2-1, getHeight()/2-1);
        new Disc(2, getWidth()/2, getHeight()/2);
    }
    
    @Override
    protected void checkEnd() {
        
        for(int i = 0; i < getNumPlayers(); i++) {
            
            //For each empty tile on the board.
            for(int x = 0; x < getWidth(); x++) {
                for(int y = 0; y < getHeight(); y++) {
                    
                    if(!getPieceInst(x, y).isPresent()) {
                        
                        //There exists a legal move. Don't skip the turn.
                        if(getFlipped(getCurrentPlayerId() % getNumPlayers()
                                + 1, x, y).size() > 0) return;
                    }
                }
            }
            //This player has no legal moves. Skip turn.
            skipTurn();
        }
        //No players have any legal moves. End game.
        endGame(findWinner());
    }
    
    /**
     * @return the ID of the player with the highest score, or -1 in the case of a draw.
     */
    private int findWinner() {
        
        int winnerId = -1;
        int winnerScore = 0;
        
        for(int i = 1; i <= getNumPlayers(); i++) {
            
            //If this player beat the current highest score, they are the new winner.
            if(getPieces(i).size() > winnerScore) {
                winnerId = i;
                winnerScore = getPieces(i).size();
            
            //If this player matched the current highest score, there may be a draw.
            } else if(getPieces(i).size() == winnerScore) {
                winnerId = -1;
            }
        }
        return winnerId;
    }
    
    @Override
    protected String getPlayerName(int playerId) {
        return getPlayer(playerId).getName() + " (" + COLOUR_NAMES[playerId - 1] + ")";
    }
    
    /**
     * Implementation of Player<Reversi> for use in inserting a human-controlled player.<br>
     * Each ReversiController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class ReversiController extends Controller<Reversi> {
        
        public ReversiController() {}
        
        public ReversiController(String name) { super(name); }
        
        @Override
        public void onTileClicked(Reversi game, int playerId, int x, int y) {
            //Place a piece.
            game.placeDisc(x, y);
        }
    }
    
    /**
     * Represents a Reversi game piece.
     * @author Alec Dorrington
     */
    private class Disc extends Piece {
        
        Disc(int ownerId, int x, int y) {
            super(ownerId, x, y, DISC_TEXTURES[ownerId - 1]);
        }

        @Override
        public boolean movePiece(int x_to, int y_to) {
            //Pieces can't be moved.
            return false;
        }
    }
}