package strategybots.bots;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import strategybots.games.legacy.HyperMNK;
import strategybots.games.legacy.HyperMNK.HyperMNKPlayer;

public class Swagmax implements HyperMNKPlayer {
    
    private HyperMNK game;
    
    private byte playerId;
    
    private int width, height, target;
    
    @Override
    public void init(HyperMNK game, int playerId) {
        this.game = game;
        this.playerId = (byte) playerId;
        width = game.getDimensions()[0];
        height = game.getDimensions()[1];
        target = game.getTarget();
    }
    
    @Override
    public void takeTurn(HyperMNK game, int playerId) {
        
        byte[][] board = getBoard();
        
        int col = evalState(board, playerId, 8, Integer.MIN_VALUE, Integer.MAX_VALUE)[1];
        game.placePiece(col);
    }
    
    private int[] evalState(byte[][] board, int playerId, int depth, int a, int b) {
        
        if(depth == 0) return new int[] {0, 0};
        
        int max = Integer.MIN_VALUE;
        int move = 3;
        
        Iterator<Integer> successors = getSuccessors(board);
        
        while(successors.hasNext()) {
            
            if(detectWin(board, playerId)) return new int[] {depth, successors.next()};
            
            int[] r = evalState(board, playerId % 2 + 1, depth - 1, -b, -a);
            
            int score = -r[0];
            
            if(score > max) {
                max = score;
                move = successors.next();
            }
            if(score > a) a = score;
            
            if(a > b) break;
        }
        return new int[] {max, move};
    }
    
    private boolean detectWin(byte[][] board, int playerId) {
        
        x_loop: for(int x = 0; x <= width - target; x++) {
            for(int y = 0; y <= height - target; y++) {
                
                if(board[x][y] == 0) {
                    continue x_loop;
                } else if(board[x][y] == playerId) {
                    dir_loop: for(int[] dir : new int[][] {{1, 0}, {0, 1}, {1, 1}}) {
                        
                        for(int i = 1; i < target; i++) {
                            
                            if(board[x + dir[0] * i][y + dir[1] * i] != playerId) {
                                continue dir_loop;
                            }
                            if(i == target - 1) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private Iterator<Integer> getSuccessors(byte[][] board) {
        
        return new Iterator<Integer>() {
            
            int i = 0, height;
            
            @Override
            public boolean hasNext() {
                
                while(i < width) {
                    
                    if(i > 0) board[i - 1][height] = 0;
                    
                    height = getHeight(i);
                    i++;
                    
                    if(height < Swagmax.this.height) {
                        board[i - 1][height] = playerId;
                        return true;
                    }
                }
                return false;
            }
            
            @Override
            public Integer next() { return i; }
            
            int getHeight(int col) {
                
                for(int i = height - 1; i > 0; i++) {
                    if(board[col][i - 1] > 0) {
                        return i;
                    }
                }
                return 0;
            }
        };
    }
    
    private byte[][] getBoard() {
        byte[][] board = new byte[width][height];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                board[x][y] = (byte) game.getPiece(x, y);
            }
        }
        return board;
    }
    
    @Override
    public String getName() { return "Swagmax"; }
}
