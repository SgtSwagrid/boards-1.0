package games;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import games.util.ChessBoard;
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
    private ChessBoard board;
    
    /** Grid of pieces indexed by position. */
    private Piece<Amazons>[][] boardPieces;
    /** Groups of pieces indexed by owner. */
    @SuppressWarnings("unchecked")
    private Set<Queen>[] playerPieces = new HashSet[] {new HashSet<>(), new HashSet<>()};
    
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
        
        //Start the game.
        start();
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
        
        //Ensure source location is in bounds.
        if(x_from < 0 || x_from >= width || y_from < 0 || y_from >= height)
            throw new IllegalMoveException("Source location out of bounds.");
        
        //Ensure a piece exists at the given 'from' position.
        if(boardPieces[x_from][y_from] == null)
            throw new IllegalMoveException("No such piece exists.");
        
        //Move the piece, subject to game constraints.
        boardPieces[x_from][y_from].movePiece(x_to, y_to);
        
        //Remember the piece which was moved, for the arrow-shooting phase.
        movedPiece = Optional.of((Queen) boardPieces[x_to][y_to]);
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
        
        //Check if this move allowed the a player to win the game. If so, end the game.
        checkWin();
        turnPhase++;
    }
    
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
                //Empty tile: '-1'.
                if(boardPieces[x][y] == null)
                    board[x][y] = -1;
                
                //Arrow: '0'.
                else if(boardPieces[x][y] instanceof Arrow)
                    board[x][y] = 0;
                
                //Queen: ID of owner.
                else board[x][y] = boardPieces[x][y].getOwnerId();
            }
        }
        return board;
    }
    
    /**
     * @return the width of the game board.
     */
    public int getWidth() { return width; }
    
    /**
     * @return the height of the game board.
     */
    public int getHeight() { return height; }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void init() {
        
        //Create board and window.
        board = new ChessBoard(width, height, TITLE);
        boardPieces = new Piece[width][height];
        
        //Determine appropriate spacing for pieces given board size.
        int h_indent = (width - 1) / 3;
        int v_indent = (height - 1) / 3;
        
        //Place the queens in their starting positions.
        new Queen(players[0], 1, 0, v_indent);
        new Queen(players[0], 1, h_indent, 0);
        new Queen(players[0], 1, width - 1 - h_indent, 0);
        new Queen(players[0], 1, width - 1, v_indent);
        
        new Queen(players[1], 2, 0, height - 1 - v_indent);
        new Queen(players[1], 2, h_indent, height - 1);
        new Queen(players[1], 2, width - 1 - h_indent, height - 1);
        new Queen(players[1], 2, width - 1, height - 1 - v_indent);
    }
    
    @Override
    protected void setupTurn() {
        
        turnPhase = 0;
        
        //Set the title to indicate the players turn.
        String colour = currentPlayerId == 1 ? "White" : "Black";
        board.getWindow().setTitle(TITLE + " - " + currentPlayer.getName()
                + "'s Turn (" + colour + ")");
    }
    
    @Override
    protected void verifyTurn() {
        
        //Ensure the player completed their turn.
        if(turnPhase != 2 && board.getWindow().isOpen())
            throw new IllegalMoveException("Player did not complete turn.");
    }
    
    @Override
    protected boolean isRunning() {
        return winnerId == -1 && board.getWindow().isOpen();
    }
    
    @Override
    protected void onFinish() {
        
        //Display the winner of the game.
        if(winnerId != -1) {
            String colour = currentPlayerId == 2 ? "White" : "Black";
            board.getWindow().setTitle(TITLE + " - " + players[winnerId - 1].getName()
                    + " (" + colour + ") has won!");
            highlightWinner();
        }
    }
    
    /**
     * Check if the player of the given ID has won.<br>
     * Will declare them as the winner if this is so.
     * @param playerId the ID of the player for whom to check for victory.
     */
    private void checkWin() {
        
        //For each player.
        outer: for(int i = 0; i < players.length; i++) {
            
            //Check if any of this players pieces are free to move.
            for(Queen piece : playerPieces[i]) {
                
                //Look at all the surrounding tiles for each opponent piece.
                for(int x = Math.max(piece.getCol() - 1, 0);
                        x <= Math.min(piece.getCol() + 1, width - 1); x++) {
                    
                    for(int y = Math.max(piece.getRow() - 1, 0);
                            y <= Math.min(piece.getRow() + 1, height - 1); y++) {
                        
                        //Player hasn't won if the opponent has somewhere to move.
                        if(boardPieces[x][y] == null)
                            continue outer;
                    }
                }
            }
            //This player has nowhere to move, the other player wins.
            winnerId = (i + 1) % players.length + 1;
        }
    }
    
    /**
     * Highlight the tiles of the queens of each player.
     * @param winnerId the player whose queens should receive the winning colour.
     */
    private void highlightWinner() {
        
        //Set the colour for the winning pieces.
        for(Queen p : playerPieces[winnerId - 1])
            board.setColour(p.getCol(), p.getRow(), HIGHLIGHT_COLOUR1);
        
        //Set the colour for the losing pieces.
        for(Queen p : playerPieces[winnerId % 2])
            board.setColour(p.getCol(), p.getRow(), HIGHLIGHT_COLOUR2);
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
        private Optional<Piece<Amazons>> selected = Optional.empty();
        
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
            game.board.addListenerToAll((x, y) -> {
                
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
        
        /**
         * Select or move a queen as appropriate.<br>
         * To be called when a tile is clicked prior to any queen movement.
         * @param game the game being played.
         * @param x the x position which was clicked.
         * @param y the y position which was clicked.
         */
        private void moveQueen(Amazons game, int x, int y) {
            
            //Select a queen.
            if(game.boardPieces[x][y] instanceof Queen &&
                    game.boardPieces[x][y].getOwnerId() == game.currentPlayerId) {
                
                //Set the selected piece and highlight its tile.
                game.board.resetColours();
                
                selected = Optional.of(game.boardPieces[x][y]);
                game.board.setColour(x, y, HIGHLIGHT_COLOUR1);
                
            //Move the selected queen.
            } else if(selected.isPresent()) {
                
                try {
                    //Try to move the selected piece to its new tile.
                    game.moveQueen(selected.get().getCol(), selected.get().getRow(), x, y);
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
     * Represents a queen instance on the board.
     * @author Alec Dorrington
     */
    private class Queen extends Piece<Amazons> {
        
        Queen(Player<Amazons> owner, int ownerId, int x, int y) {
            
            super(board, owner, ownerId, x, y);
            
            boardPieces[x][y] = this;
            playerPieces[ownerId - 1].add(this);
            
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/chess/white_queen.png" : "res/chess/black_queen.png"));
        }
        
        @Override
        public void movePiece(int x_to, int y_to) {
            
            //Ensure a move is valid before making it.
            validateMove(getCol(), getRow(), x_to, y_to);
            
            //Move this piece in the pieces array.
            boardPieces[getCol()][getRow()] = null;
            boardPieces[x_to][y_to] = this;
            
            //Move this piece graphically.
            board.setPosition(this, x_to, y_to);
            
            //Update the position.
            x = x_to;
            y = y_to;
        }
        
        /**
         * Will fire an arrow from this queen to the given position, if such a move is valid.<br>
         * Otherwise, an exception will be thrown.
         * @param x_to the x position to shoot at.
         * @param y_to the y position to shoot at.
         * @throws IllegalMoveException
         */
        void shootArrow(int x_to, int y_to) {
            
            validateMove(getCol(), getRow(), x_to, y_to);
            new Arrow(getOwner(), getOwnerId(), x_to, y_to);
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
            
            //Ensure target location is in bounds.
            if(x_to < 0 || x_to >= width || y_to < 0 || y_to >= height)
                throw new IllegalMoveException("Target location out of bounds.");
            
            //Ensure piece doesn't move on top of itself.
            if(x_to == x_from && y_to == y_from)
                throw new IllegalMoveException("Must move somewhere else.");
            
            //Ensure piece doesn't move on top of another piece.
            if(boardPieces[x_to][y_to] != null)
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
                
                if(boardPieces[xx][yy] != null)
                    throw new IllegalMoveException("Can't jump over other pieces.");
                
                xx += x_sign;
                yy += y_sign;
            }
        }
    }
    
    /**
     * Represents an arrow instance fired onto the board.
     * @author Alec Dorrington
     */
    private class Arrow extends Piece<Amazons> {
        
        private Arrow(Player<Amazons> owner, int ownerId, int x, int y) {
            
            super(board, owner, ownerId, x, y);
            
            boardPieces[x][y] = this;
            
            //Select the appropriate texture depending on the owner.
            setTexture(Texture.getTexture(ownerId == 1 ?
                    "res/chess/white_pawn.png" : "res/chess/black_pawn.png"));
        }
        
        @Override
        public void movePiece(int x_to, int y_to) {
            //Arrows can't be moved.
            throw new IllegalMoveException("Arrows can't be moved.");
        }
    }
}