package games;
import java.util.Optional;

import swagui.api.Button;
import swagui.api.Colour;
import swagui.api.Texture;
import swagui.api.Tile;
import swagui.api.Window;
import util.Chessboard;

public class Amazons {
    
    private static final String TITLE = "Game of the Amazons";
    
    private Chessboard board;
    
    private Piece[][] pieces;
    
    private AmazonsPlayer[] players;
    
    private int width, height;
    
    private AmazonsPlayer currentPlayer;
    
    private Optional<Queen> selected = Optional.empty();
    
    public Amazons(int width, int height, AmazonsPlayer... players) {
        
        this.width = width;
        this.height = height;
        
        board = new Chessboard(width, height, TITLE);
        pieces = new Piece[width][height];
        this.players = players.clone();
        
        new Queen(board.getWindow(), players[0], 4, 4);
        new Queen(board.getWindow(), players[0], 5, 5);
        new Queen(board.getWindow(), players[0], 6, 6);
        
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                
                int x = i, y = j;
                board.addListener(x, y, () -> {
                    
                    if(selected.isPresent())
                        selected.get().move(x, y);
                });
            }
        }
        
        currentPlayer = players[0];
        
        
    }
    
    public int getNumPlayers() { return players.length; }
    
    public static class AmazonsPlayer {
        
        
    }
    
    private class Piece extends Button {
        public Piece(Window window) { super(window); }
    }
    
    private class Queen extends Piece {
        
        AmazonsPlayer owner;
        int x, y;

        Queen(Window window, AmazonsPlayer owner, int x, int y) {
            
            super(window);
            this.owner = owner;
            move(x, y);
            
            setSize(Chessboard.TILE_SIZE, Chessboard.TILE_SIZE);
            setColour(Colour.WHITE);
            setTexture(Texture.getTexture("res/chess/white_knight.png"));
        }
        
        void move(int x, int y) {
            
            board.unhighlightTile(this.x, this.y);
            pieces[this.x][this.y] = null;
            
            this.x = x;
            this.y = y;
            
            board.movePiece(this, x, y);
            pieces[x][y] = this;
            
            selected = Optional.empty();
        }
        
        @Override protected void onLeftClick() {
            
            if(currentPlayer == owner) {
                
                if(selected.isPresent())
                    board.unhighlightTile(selected.get().x, selected.get().y);
                
                board.highlightTile(x, y);
                selected = Optional.of(this);
            }
        }
    }
    
    private class Arrow extends Piece {
        
        private AmazonsPlayer owner;
        
        private Arrow(Window window, AmazonsPlayer owner) {
            super(window);
            this.owner = owner;
        }
    }
}