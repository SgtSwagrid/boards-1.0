package strategybots.bots;

import static java.lang.Math.*;

import strategybots.games.ConnectFour;
import strategybots.games.base.Game.Player;

public class SwagC4 implements Player<ConnectFour> {
    
    private ConnectFour game;
    private int playerId;
    private int width, height, target;
    
    private int turn = 1;
    private long time = 2000;
    
    public SwagC4() {}
    
    public SwagC4(long time) { this.time = time; }
    
    @Override
    public void init(ConnectFour game, int playerId) {
        
        this.game = game;
        this.playerId = playerId;
        width = game.getWidth();
        height = game.getHeight();
        target = game.getTarget();
    }

    @Override
    public void takeTurn(ConnectFour game, int playerId) {
        
        long start = System.currentTimeMillis();
        int[] move = getMove();
        game.placeStone(move[1]);
        printStats(move[0], move[1], move[2], start);
    }
    
    private int[] getMove() {
        
        int score = 0, move = -1, depth = 1;
        int maxDepth = width * height;
        long start = System.currentTimeMillis();
        
        int[][] board = getBoard();
        int[] heights = getHeights();
        
        for(; depth < maxDepth; depth++) {
            
            int[] result = minimax(board, heights, playerId, depth, 0,
                    -Integer.MAX_VALUE, Integer.MAX_VALUE);
            score = result[0];
            move = result[1];
            
            if(System.currentTimeMillis()-start > time) break;
        }
        return new int[] {score, move, depth};
    }
    
    private int[] minimax(int[][] board, int heights[], int playerId,
            int depth, int heuristic, int a, int b) {
        
        int score = 0, move = -1;
        
        for(int x = 0; x < width; x++) {
            
            if(heights[x] >= height) continue;
            board[x][heights[x]++] = playerId;
             
            if(checkWin(board, playerId, x, heights[x]-1)) {
                board[x][--heights[x]] = 0;
                return new int[] {depth*1000, x};
            }
            
            int h = heuristic + heuristic(board, playerId, x, heights[x]-1);
            int s = depth<=1 ? h :
                -minimax(board, heights, playerId%2+1, depth-1, -h, -b, -a)[0];
            
            if(s > score || move == -1) {
                score = s;
                move = x;
                a = score>a ? score:a;
            }
            board[x][--heights[x]] = 0;
            if(a >= b) break;
        }
        return new int[] {score, move};
    }
    
    private final int[][] dirs = new int[][] {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};
    private final int[] signs = new int[] {-1, 1};
    
    private boolean checkWin(int[][] board, int playerId, int x, int y) {
        
        for(int[] dir : dirs) {
            
            int streak = 1;
            
            for(int sign : signs) {
                
                for(int i = 1; i < target; i++) {
                    
                    int xx = x+i*dir[0]*sign;
                    int yy = y+i*dir[1]*sign;
                    
                    if(xx < 0 || xx >= width) break;
                    if(yy < 0 || yy >= height) break;
                    
                    if(board[xx][yy] != playerId) break;
                    
                    streak++;
                }
            }
            if(streak >= target) return true;
        }
        return false;
    }
    
    private int heuristic(int[][] board, int playerId, int x, int y) {
        
        int score = 0;
        
        for(int[] dir : dirs) {
            
            int[] v0 = raycast(width, height, x, y, -dir[0], -dir[1], target-1);
            int[] v1 = raycast(width, height, x, y, dir[0], dir[1], target-1);
            int len = max(abs(v1[0]-v0[0]), abs(v1[1]-v0[1])) + 1;
            
            int myPieces = 0, otherPieces = 0;
            
            for(int i = 0; i < len; i++) {
                
                int xx = v0[0] + dir[0]*i;
                int yy = v0[1] + dir[1]*i;
                
                if(board[xx][yy] == playerId) myPieces++;
                else if(board[xx][yy] != 0) otherPieces++;
                
                if (i >= target) {
                    
                    int xx_end = xx - dir[0]*target;
                    int yy_end = yy - dir[1]*target;
                    
                    if(board[xx_end][yy_end] == playerId) myPieces--;
                    else if(board[xx_end][yy_end] != 0) otherPieces--;
                }
                
                if(i >= target-1) {
                    
                    if(myPieces == 1)
                        score += otherPieces*otherPieces;
                    if(otherPieces == 0)
                        score += 2*myPieces-1;
                }
            }
        }
        return score;
    }
    
    private static int[] raycast(int w, int h, int x, int y, int dx, int dy, int l) {
        
        int xLen = dx > 0 ? (w-x-1) : (
                   dx < 0 ? x : (
                   w>h?w:h));
        
        int yLen = dy > 0 ? (h-y-1) : (
                   dy < 0 ? y : (
                   w>h?w:h));
        
        int len = xLen < yLen ? xLen : yLen;
        len = l < len ? l : len;
         
        return new int[] {
            x + dx*len,
            y + dy*len
        };
    }
    
    private int[][] getBoard() {
        
        int[][] board = new int[width][height];
        game.forEachPosition((x, y) -> board[x][y] = game.getStone(x, y));
        return board;
    }
    
    private int[] getHeights() {
        
        int[] heights = new int[width];
        for(int x = 0; x < width; x++) {
            heights[x] = game.getStackHeight(x);
        }
        return heights;
    }
    
    private void printStats(int score, int move, int depth, long start) {
        
        System.out.println("=======================");
        System.out.println("SwagC4 Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        System.out.println("Turn:        " + turn++);
        System.out.println("Expectation: " + score);
        System.out.println("Column:      " + (move+1));
        System.out.println("Depth:       " + depth);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
    }
    
    @Override
    public String getName() { return "SwagC4"; }
}
