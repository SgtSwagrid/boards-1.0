package bots;

import games.HyperMNK;
import games.HyperMNK.HyperMNKPlayer;

public class SwagmaxC4 implements HyperMNKPlayer {
    
    private HyperMNK game;
    
    private byte width, height, target;
    
    private int time;
    
    public SwagmaxC4(int time) {
        this.time = time;
    }
    
    @Override
    public void init(HyperMNK game, int playerId) {
        
        this.game = game;
        width = (byte) game.getDimensions()[0];
        height = (byte) game.getDimensions()[1];
        target = (byte) game.getTarget();
    }
    
    @Override
    public void takeTurn(HyperMNK game, int playerId) {
        
        byte[][] board = getBoard();
        byte[] heights = getHeights();
        
        boolean firstTurn = true;
        for(int x = 0; x < width; x++) firstTurn &= board[x][0] == 0;
        if(firstTurn) game.placePiece(width / 2);
        
        else game.placePiece(minimax(board, heights, playerId, time));
    }
    
    private int minimax(byte[][] board, byte[] heights, int playerId, int time) {
        
        int move = 0, depth = 1;
        
        long startTime = System.currentTimeMillis();
        
        while(System.currentTimeMillis() - startTime < time) {
            
            move = minimax(board, heights, playerId, depth, -100, 100)[1];
            depth++;
        }
        System.out.println("Minimax Depth: " + depth);
        return move;
    }
    
    private int[] minimax(byte[][] board, byte[] heights, int playerId, int depth, int a, int b) {
        
        int score = depth == 1 ? 0 : -100;
        int move = -1;
        
        for(int x = 0; x < width; x++) {
            
            if(heights[x] == height) continue;
            board[x][heights[x]++] = (byte) playerId;
            
            if(detectWin(board, playerId, x, heights[x] - 1)) {
                board[x][--heights[x]] = 0;
                return new int[] {depth, x};
                
            } else if(depth > 1) {
                
                int s = -minimax(board, heights, playerId % 2 + 1, depth - 1, -b, -a)[0];
                
                if(s > score) {
                    score = s;
                    move = x;
                    a = s > a ? s : a;
                }
            }
            board[x][--heights[x]] = 0;
            if(a >= b) break;
        }
        return new int[] {score, move};
    }
    
    private boolean detectWin(byte[][] board, int playerId, int x, int y) {
        
        for(int i = 0; i < 4; i++) {
            
            int x_dir = i < 2 ? 1 : i == 2 ? 0 : -1;
            int y_dir = i == 0 ? 0 : 1;
            
            int streak = 1;
            
            for(int sign = -1; sign <= 1; sign += 2) {
                
                int xx = x + x_dir * sign;
                int yy = y + y_dir * sign;
                
                while(xx >= 0 && xx < width && yy >= 0 && yy < height
                        && board[xx][yy] == playerId) {
                    
                    streak++;
                    
                    if(streak == target) return true;
                    
                    xx += x_dir * sign;
                    yy += y_dir * sign;
                }
            }
        }
        return false;
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
    
    private byte[] getHeights() {
        
        byte[] heights = new byte[width];
        
        for(int x = 0; x < width; x++) {
            for(int y = 0; y <= height; y++) {
                
                if(y == height || game.getPiece(x, y) == 0) {
                    heights[x] = (byte) y;
                    break;
                }
            }
        }
        return heights;
    }
    
    @Override
    public String getName() { return "Swagmax"; }
}