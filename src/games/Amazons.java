package games;

import java.util.Optional;

import games.util.Chessboard;
import games.util.Game;
import games.util.IllegalMoveException;
import games.util.Piece;
import games.util.Player;
import swagui.api.Colour;
import swagui.api.Texture;

/**
 * <b>Game of the Amazons implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Game_of_the_Amazons">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Amazons>'.<br>
 * Human players can be made by instantiating 'AmazonsController'.
 * 
 * @author Alec Dorrington
 */
public class Amazons extends Game {
    
    /** Title of the window. */
    private static final String TITLE = "Game of the Amazons";
    
    /** Colour used for selected pieces. */
    public static final Colour HIGHLIGHT_COLOUR1 = Colour.rgb(88, 177, 159);
    /** Colour used for moved pieces. */
    public static final Colour HIGHLIGHT_COLOUR2 = Colour.rgb(249, 127, 81);
    
    /** Chessboard instance, manages the window, and piece layout. */
    private Chessboard board;
    
    /** Grid of pieces indexed by position. */
    private AmazonsPiece[][] pieces;
    /** Queen pieces grouped by owner. */
    private Queen[] player1Pieces, player2Pieces;
    
    /** Players participating in this game. */
    private Player<Amazons> player1, player2;
    /** The winner of the game. */
    private Optional<Player<Amazons>> winner = Optional.empty();
    /** The player whose turn is currently active. */
    private Player<Amazons> currentPlayer;
    /** The ID of the player whose turn is currently active. */
    private volatile int currentPlayerId = 1;
    
    /** The dimensions of the game board. */
    private int width, height;
    
    /** The piece which was moved on this turn. */
    private Optional<Queen> movedPiece = Optional.empty();
    /** Current turn state: 0 = Not moved, 1 = Moved, not fired, 2 = Arrow fired. */
    private volatile int turnPhase;
    
    /**
     * Asynchronously runs a new Game of the Amazons instance.
     * @param width the width of the game board.
     * @param height the height of the game board.
     * @param player1 the first (white) player to participate.
     * @param player2 the second (black) player to participate.
     */
    public Amazons(int width, int height, Player<Amazons> player1, Player<Amazons> player2) {
        
        super(new Player[] {player1, player2});
        
        //Set the board dimensions.
        this.width = width;
        this.height = height;
        
        //Set the participating players.
        this.player1 = player1;
        this.player2 = player2;
        
        //Create a new game board, opening a window and placing initial pieces.
        createBoard();
        
        //Run the game simulation in a new thread.
        new Thread("Game of the Amazons") {
            @Override public void run() {
                Amazons.this.start();
            }
        }.start();
    }
    
    /**
     * Moves your queen at the given position to a new position.<br>
     * Must be called exactly once per turn, before 'shootArrow()' is called.<br>
     * Move must be consistent with the rules of the game, or an exception will be thrown.
     * @param x_from the current x position of the queen.
     * @param y_from the current y position of the queen.
     * @param x_to the new x position of the queen.
     * @param y_to the new y position of the queen.
     * @throws IllegalMoveException
     */
    public void moveQueen(int x_from, int y_from, int x_to, int y_to) {
        
        //Ensure actions are taken in the correct order.
        if(turnPhase != 0)
            throw new IllegalMoveException("Can't move pieces twice.");
        
        //Ensure a piece exists at the given 'from' position.
        if(pieces[x_from][y_from] == null)
            throw new IllegalMoveException("No such piece exists.");
        
        //Move the piece, subject to game constraints.
        pieces[x_from][y_from].movePiece(x_to, y_to);
        
        //Remember the piece which was moved, for the arrow-shooting phase.
        movedPiece = Optional.of((Queen) pieces[x_to][y_to]);
        turnPhase++;
    }
    
    /**
     * Shoots an arrow at a given position.<br>
     * Must be called exactly once per turn, after 'moveQueen()' is called.<br>
     * Arrow will be fired from the most recently moved queen.<br>
     * Move must be consistent with the rules of the game, or an exception will be thrown.
     * @param x the destination x position of the arrow.
     * @param y the destination y position of the arrow.
     * @throws IllegalMoveException
     */
    public void shootArrow(int x, int y) {
        
        //Ensure actions are taken in the correct order.
        if(turnPhase == 0)
            throw new IllegalMoveException("Must move a queen before shooting.");
        
        if(turnPhase == 2)
            throw new IllegalMoveException("Can't shoot multiple arrows.");
        
        //Fire the arrow, subject to game constraints.
        movedPiece.get().shootArrow(x, y);
        
        //Check if this move allowed the current player to win the game. If so, end the game.
        checkWin(currentPlayerId);
        turnPhase++;
    }
    
    /**
     * Checks if the game has concluded, or if it is still in progress.
     * @return whether the game is still running.
     */
    public boolean isRunning() { return !winner.isPresent(); }
    
    /**
     * @return the width of the game board.
     */
    public int getWidth() { return width; }
    
    /**
     * @return the height of the game board.
     */
    public int getHeight() { return height; }
    
    /**
     * Returns the current state of the board.<br>
     * Each array index represents the piece currently at that position.<br>
     * <table border="1">
     * <tr><td>-1</td><td>Empty tile.</td></tr>
     * <tr><td>0</td><td>Arrow.</td></tr>
     * <tr><td>1</td><td>Queen owned by player 1.</td></tr>
     * <tr><td>2</td><td>Queen owned by player 2.</td></tr>
     * </table>
     * @return the game state.
     */
    public int[][] getState() {
        
        int[][] board = new int[width][height];
        
        //For each grid cell.
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                
                //Determine index of piece on this tile.
                board[x][y] = getPiece(x, y);
            }
        }
        return board;
    }
    
    /**
     * Returns the ID of the piece at a position.<br>
     * See 'getState()' for details.
     * @param x the x position of the piece.
     * @param y the y position of the piece.
     * @return the ID of the piece at this position.
     */
    private int getPiece(int x, int y) {
        
        //Empty tile: '-1'.
        if(pieces[x][y] == null)
            return -1;
        
        //Arrow: '0'.
        else if(pieces[x][y] instanceof Arrow)
            return 0;
        
        //Queen: ID of owner.
        else return pieces[x][y].getOwnerId();
    }
    
    /**
     * Runs the game simulation to completion.<br>
     * Assumes the game board has already been created.
     */
    @Override
    protected void start() {
        
        //Call the implementation-specific initialisation code for each player.
        player1.init(this, 1);
        player2.init(this, 2);
        
        //While the game remains incomplete.
        while(!winner.isPresent() && board.getWindow().isOpen()) {
            
            //Determine who is to be the next player.
            currentPlayer = currentPlayerId == 1 ? player1 : player2;
            String colour = currentPlayerId == 1 ? "White" : "Black";
            board.getWindow().setTitle(TITLE + " - " + currentPlayer.getName()
                    + "'s Turn (" + colour + ")");
            
            //Have the current player take their turn.
            turnPhase = 0;
            currentPlayer.takeTurn(this, currentPlayerId);
            
            //Ensure the player completed their turn.
            if(turnPhase != 2 && board.getWindow().isOpen())
                throw new IllegalMoveException("Player did not complete turn.");
            
            //Switch to the next player.
            currentPlayerId = currentPlayerId % 2 + 1;
        }
        
        //Display the winner of the game.
        if(winner.isPresent()) {
            String colour = currentPlayerId == 2 ? "White" : "Black";
            board.getWindow().setTitle(TITLE + " - " + winner.get().getName()
                    + " (" + colour + ") has won!");
            highlightWinner(currentPlayerId % 2 + 1);
        }
    }
    
    /**
     * Create the chessboard and place the initial queens.
     */
    private void createBoard() {
        
        //Create board and window.
        board = new Chessboard(width, height, TITLE);
        pieces = new AmazonsPiece[width][height];
        
        //Determine appropriate spacing for pieces given board size.
        int h_indent = (width - 1) / 3;
        int v_indent = (height - 1) / 3;
        
        //Create white pieces.
        player1Pieces = new Queen[] {
                new Queen(player1, 1, 0, v_indent),
                new Queen(player1, 1, h_indent, 0),
                new Queen(player1, 1, width - 1 - h_indent, 0),
                new Queen(player1, 1, width - 1, v_indent)};
        
        //Create black pieces.
        player2Pieces = new Queen[] {
                new Queen(player2, 2, 0, height - 1 - v_indent),
                new Queen(player2, 2, h_indent, height - 1),
                new Queen(player2, 2, width - 1 - h_indent, height - 1),
                new Queen(player2, 2, width - 1, height - 1 - v_indent)};
    }
    
    /**
     * Check if the player of the given ID has won.<br>
     * Will declare them as the winner if this is so.
     * @param playerId the ID of the player for whom to check for victory.
     */
    private void checkWin(int playerId) {
        
        //Get the pieces of the OTHER player.
        playerId = playerId % 2 + 1;
        Queen[] friendlyPieces = playerId == 1 ? player1Pieces : player2Pieces;
        
        //Check if any of the opponents pieces are free to move.
        for(Queen piece : friendlyPieces) {
            
            //Look at all the surrounding tiles for each opponent piece.
            for(int x = Math.max(piece.getBoardX() - 1, 0);
                    x <= Math.min(piece.getBoardX() + 1, width - 1); x++) {
                
                for(int y = Math.max(piece.getBoardY() - 1, 0);
                        y <= Math.min(piece.getBoardY() + 1, height - 1); y++) {
                    
                    //The player has not yet won if the opponent has a free position to move to.
                    if(pieces[x][y] == null) return;
                }
            }
        }
        //No free positions for the opponent were found.
        winner = Optional.of(currentPlayer);
    }
    
    /**
     * Highlight the tiles of the queens of each player.
     * @param winnerId the player whose queens should receive the winning colour.
     */
    private void highlightWinner(int winnerId) {
        
        //Determine which pieces were winners and losers.
        Queen[] winningPieces = winnerId == 1 ? player1Pieces : player2Pieces;
        Queen[] losingPieces = winnerId == 1 ? player2Pieces : player1Pieces;
        
        //Set the colour for the winning pieces.
        for(Queen p : winningPieces)
            board.setColour(p.getBoardX(), p.getBoardY(), HIGHLIGHT_COLOUR1);
        
        //Set the colour for the losing pieces.
        for(Queen p : losingPieces)
            board.setColour(p.getBoardX(), p.getBoardY(), HIGHLIGHT_COLOUR2);
    }
    
    /**
     * Implementation of Player<Amazons> for use in inserting a human-controlled player.<br>
     * Each AmazonsController will make moves based on mouse input on the game display window.
     * @author Alec Dorrington
     */
    public static final class AmazonsController implements Player<Amazons> {
        
        /** The display name of this player. */
        private String name = "Controller";
        
        /** The piece currently selected by the mouse, if there is any. */
        private Optional<AmazonsPiece> selected = Optional.empty();
        
        /**
         * Constructs a new AmazonsController with the default name of "Controller".
         */
        public AmazonsController() {}
        
        /** 
         * Constructs a new AmazonsController with the given name.
         * @param name the display name of this controller.
         */
        public AmazonsController(String name) { this.name = name; }
        
        @Override
        public void init(Amazons game, int playerId) {
            
            //Add a click listener to each grid cell on the board.
            for(int i = 0; i < game.width; i++) {
                for(int j = 0; j < game.height; j++) {
                    
                    int x = i, y = j;
                    game.board.addListener(x, y, () -> {
                        
                        //Listeners should only be active on your own turn.
                        if(playerId != game.currentPlayerId)
                            return;
                        
                        //Move a queen.
                        if(game.turnPhase == 0) {
                            moveQueen(game, x, y);
                            
                        //Shoot an arrow.
                        } else if(game.turnPhase == 1) {
                            shootArrow(game, x, y);
                        }
                    });
                }
            }
        }
        
        /**
         * Select or move a queen as appropriate.<br>
         * To be called when a tile is clicked prior to any queen movement.
         * @param game the game being played.
         * @param x the x position which was clicked.
         * @param y the y position which was clicked.
         */
        private void moveQueen(Amazons game, int x, int y) {
            
            //Select a queen.
            if(game.pieces[x][y] instanceof Queen &&
                    game.pieces[x][y].getOwnerId() == game.currentPlayerId) {
                
                //Set the selected piece and highlight its tile.
                game.board.resetColours();
                
                selected = Optional.of(game.pieces[x][y]);
                game.board.setColour(x, y, HIGHLIGHT_COLOUR1);
                
            //Move the selected queen.
            } else if(selected.isPresent()) {
                
                try {
                    //Try to move the selected piece to its new tile.
                    game.moveQueen(selected.get().getBoardX(), selected.get().getBoardY(), x, y);
                    game.board.resetColours();
                    game.board.setColour(x, y, HIGHLIGHT_COLOUR2);
                    
                //Invalid moves should be ignored.
                } catch(IllegalMoveException e) {}
            }
        }
        
        /**
         * Fire an arrow from the selected queen to the given position.
         * To be called when a tile is clicked after the queen is moved.
         * @param game the game being played.
         * @param x the x position which was clicked.
         * @param y the y position which was clicked.
         */
        private void shootArrow(Amazons game, int x, int y) {
            
            try {
                //Try to spawn an arrow at this location.
                game.shootArrow(x, y);
                game.board.resetColours();
                selected = Optional.empty();
                
            //Invalid moves should be ignored.
            } catch(IllegalMoveException e) {}
        }
        
        @Override
        public void takeTurn(Amazons game, int playerId) {
            //Wait until the turn is complete before returning control to the game.
            //Actual logic is handled asynchronously by the above button listeners.
            while(game.turnPhase != 2 && game.board.getWindow().isOpen()) {}
        }
        
        @Override
        public String getName() { return name; }
    }
    
    /**
     * Abstract supertype for GotA game pieces (queens and arrows).
     * @author Alec Dorrington
     */
    private abstract class AmazonsPiece extends Piece<Amazons> {
        
        /**
         * Constructs a new piece of a particular owner at a particular position.
         * @param owner the owner of this piece.
         * @param ownerId the ID of the owner of this piece.
         * @param x the x position of this piece.
         * @param y the y position of this piece.
         */
        AmazonsPiece(Player<Amazons> owner, int ownerId, int x, int y) {
            super(board, owner, ownerId, x, y);
            pieces[x][y] = this; //temp
        }
        
        /**
         * Ensures that a move is valid.<br>
         * Throws an exception otherwise.
         * @param x_from the x position from which a piece is moving.
         * @param y_from the y position from which a piece is moving.
         * @param x_to the x position to which a piece is moving.
         * @param y_to the y position to which a piece is moving.
         * @throws IllegalMoveException
         */
        void validateMove(int x_from, int y_from, int x_to, int y_to) {
            
            //Ensure piece is owned by the current player.
            if(getOwnerId() != currentPlayerId)
                throw new IllegalMoveException("Can't move an opponents piece.");
            
            //Ensure piece doesn't move on top of itself.
            if(x_to == x_from && y_to == y_from)
                throw new IllegalMoveException("Must move somewhere else.");
            
            //Ensure piece doesn't move on top of another piece.
            if(pieces[x_to][y_to] != null)
                throw new IllegalMoveException("Can't move onto another piece.");
            
            //Ensure piece moves in a straight line (incl. diagonally).
            if(Math.abs(x_to - x_from) != Math.abs(y_to - y_from)
                    && x_to != x_from && y_to != y_from)
                throw new IllegalMoveException("Must move in a straight line.");
            
            
            int x_sign = (int) Math.signum(x_to - x_from);
            int y_sign = (int) Math.signum(y_to - y_from);
            
            int xx = x_from + x_sign;
            int yy = y_from + y_sign;
            
            //Ensure piece doesn't jump over any other pieces.
            while(xx != x_to || yy != y_to) {
                
                if(pieces[xx][yy] != null)
                    throw new IllegalMoveException("Can't jump over other pieces.");
                
                xx += x_sign;
                yy += y_sign;
            }
        }
        
        /**
         * Called by the game to move a piece to a particular position.<br>
         * Implementations of this method should provide their own move validation.<br>
         * An exception may be thrown if a move is invalid.
         * @param x_to the x position to move to.
         * @param y_to the y position to move to.
         * @throws IllegalMoveException
         */
        abstract void movePiece(int x_to, int y_to);
    }
    
    /**
     * Represents a queen instance on the board.
     * @author Alec Dorrington
     */
    private class Queen extends AmazonsPiece {
        
        Queen(Player<Amazons> owner, int ownerId, int x, int y) {
            
            super(owner, ownerId, x, y);
            
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/chess/white_queen.png" : "res/chess/black_queen.png"));
        }
        
        @Override
        void movePiece(int x_to, int y_to) {
            
            //Ensure a move is valid before making it.
            validateMove(getBoardX(), getBoardY(), x_to, y_to);
            
            //Move this piece in the pieces array.
            pieces[getBoardX()][getBoardY()] = null;
            pieces[x_to][y_to] = this;
            
            //Move this piece graphically.
            board.addPiece(this, x_to, y_to);
            
            setPos(x_to, y_to);
        }
        
        /**
         * Will fire an arrow from this queen to the given position, if such a move is valid.<br>
         * Otherwise, an exception will be thrown.
         * @param x_to the x position to shoot at.
         * @param y_to the y position to shoot at.
         * @throws IllegalMoveException
         */
        void shootArrow(int x_to, int y_to) {
            
            validateMove(getBoardX(), getBoardY(), x_to, y_to);
            new Arrow(getOwner(), getOwnerId(), x_to, y_to);
        }
    }
    
    /**
     * Represents an arrow instance fired onto the board.
     * @author Alec Dorrington
     */
    private class Arrow extends AmazonsPiece {
        
        private Arrow(Player<Amazons> owner, int ownerId, int x, int y) {
            
            super(owner, ownerId, x, y);
            
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/chess/white_pawn.png" : "res/chess/black_pawn.png"));
        }
        
        @Override
        void movePiece(int x_to, int y_to) {
            //Arrows can't be moved.
            throw new IllegalMoveException("Arrows can't be moved.");
        }
    }
}