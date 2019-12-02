package strategybots.bots;

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
            
            int[] result = minimax(board, heights, playerId, depth,
                    -Integer.MAX_VALUE, Integer.MAX_VALUE);
            score = result[0];
            move = result[1];
            
            if(System.currentTimeMillis()-start > time) break;
        }
        return new int[] {score, move, depth};
    }
    
    private int[] minimax(int[][] board, int heights[],
            int playerId, int depth, int a, int b) {
        
        if(depth == 0) return new int[]
                {heuristic(board, playerId), -1};
        
        int score = 0;
        int move = -1;
        
        for(int x = 0; x < width; x++) {
            
            if(heights[x] >= height) continue;
            board[x][heights[x]++] = playerId;
             
            if(checkWin(board, playerId, x, heights[x]-1)) {
                board[x][--heights[x]] = 0;
                return new int[] {depth*1000, x};
            }
            
            int s = -minimax(board, heights, playerId%2+1, depth-1, -b, -a)[0];
            
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
    
    private int heuristic(int[][] board, int playerId) {
        return h(board, playerId) - h(board, playerId%2+1);
    }
    
    private int h(int[][] board, int playerId) {
        
        int score = 0;
        
        for(int[] dir : dirs) {
            
            int x0 = dir[0]>=0 ? 0 : (target-1);
            int x1 = width - (dir[0]<=0 ? 0 : (target-1));
            
            int y0 = dir[1]>=0 ? 0 : (target-1);
            int y1 = height - (dir[1]<=0 ? 0 : (target-1));
            
            for(int x = x0; x < x1; x++) {
                y: for(int y = y0; y < y1; y++) {
                    
                    int n = 0;
                    for(int i = 0; i < target; i++) {
                        int piece = board[x+i*dir[0]][y+i*dir[1]];
                        if(piece == playerId) n++;
                        else if(piece == playerId%2+1) break y;
                    }
                    score += n * n;
                }
            }
        }
        return score;
    }
    
    private int[][] getBoard() {
        
        int[][] board = new int[width][height];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                board[x][y] = game.getStone(x, y);
            }
        }
        return board;
    }
    
    private int[] getHeights() {
        
        int[] heights = new int[width];
        x: for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if(game.getStone(x, y) == 0) {
                    heights[x] = y;
                    continue x;
                }
            }
            heights[x] = height;
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
