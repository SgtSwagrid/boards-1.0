package strategybots.bots;

import strategybots.games.ConnectFour;
import strategybots.games.base.Game.Player;

public class SwagC4 implements Player<ConnectFour> {
    
    private ConnectFour game;
    private int turn = 1;
    private long time = 2000;
    
    public SwagC4() {}
    
    public SwagC4(long time) { this.time = time; }
    
    @Override
    public void init(ConnectFour game, int playerId) {
        this.game = game;
    }

    @Override
    public void takeTurn(ConnectFour game, int playerId) {
        
        long start = System.currentTimeMillis();
        
        int[] move = bestMove(getBoard(), playerId);
        game.placeStone(move[1]);
        
        System.out.println("================");
        System.out.println("SwagC4 statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        System.out.println("Turn:        " + turn++);
        System.out.println("Expectation: " + move[0]);
        System.out.println("Column:      " + move[1]+1);
        System.out.println("Depth:       " + move[2]);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
    }
    
    private int[] bestMove(int[][] board, int playerId) {
        
        int score = 0, move = -1, depth;
        long start = System.currentTimeMillis();
        int maxDepth = game.getWidth() * game.getHeight();
        
        for(depth = 0; depth < maxDepth; depth++) {
            
            int[] result = evaluate(board, playerId, depth,
                    -Integer.MAX_VALUE, Integer.MAX_VALUE);
            score = result[0];
            move = result[1];
            
            if(System.currentTimeMillis()-start > time) break;
        }
        return new int[] {score, move, depth};
    }
    
    private int[] evaluate(int[][] board, int playerId, int depth, int a, int b) {
        
        if(depth == 0) return new int[]
                {heuristic(board, playerId), -1};
        
        int score = -Integer.MAX_VALUE;
        int move = -1;
        
        for(int x = 0; x < game.getWidth(); x++) {
            
            int stackSize = getStackSize(board, x);
            
            if(stackSize >= game.getHeight()) continue;
            
            board[x][stackSize] = playerId;
            
            if(isWin(board, playerId, x, stackSize)) {
                board[x][stackSize] = 0;
                return new int[] {depth*10000, x};
            }
            
            int moveScore = -evaluate(board, playerId%2+1, depth-1, -b, -a)[0];
            
            board[x][stackSize] = 0;
            
            if(moveScore > score) {
                score = moveScore;
                move = x;
                a = score;
            }
            if(a > b) break;
        }
        return new int[] {score, move};
    }
    
    private boolean isWin(int[][] board, int playerId, int x, int y) {
        
        int[][] dirs = new int[][] {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        int[] signs = new int[] {-1, 1};
        
        for(int[] dir : dirs) {
            
            int streak = 1;
            
            for(int sign : signs) {
                
                for(int i = 1; i < game.getTarget(); i++) {
                    
                    int xx = x+i*dir[0]*sign;
                    int yy = y+i*dir[1]*sign;
                    
                    if(xx < 0 || xx >= game.getWidth()) break;
                    if(yy < 0 || yy >= game.getHeight()) break;
                    
                    if(board[xx][yy] != playerId) break;
                    
                    streak++;
                }
            }
            if(streak >= game.getTarget()) return true;
        }
        return false;
    }
    
    private int heuristic(int[][] board, int playerId) {
        return h(board, playerId) - h(board, playerId%2+1);
    }
    
    private int h(int[][] board, int playerId) {
        
        int[][] dirs = new int[][] {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        
        int score = 0;
        
        for(int[] dir : dirs) {
            
            int x0 = dir[0]>=0 ? 0 : (game.getTarget()-1);
            int x1 = game.getWidth() - (dir[0]<=0 ? 0 : (game.getTarget()-1));
            
            int y0 = dir[1]>=0 ? 0 : (game.getTarget()-1);
            int y1 = game.getHeight() - (dir[1]<=0 ? 0 : (game.getTarget()-1));
            
            for(int x = x0; x < x1; x++) {
                y: for(int y = y0; y < y1; y++) {
                    
                    int streak = 0;
                    
                    for(int i = 0; i < game.getTarget(); i++) {
                        int piece = board[x+i*dir[0]][y+i*dir[1]];
                        if(piece == playerId) streak++;
                        else if(piece == playerId%2+1) break y;
                    }
                    score += streak * streak;
                }
            }
        }
        return score;
    }
    
    private int getStackSize(int[][] board, int x) {
        
        for(int y = 0; y < game.getHeight(); y++) {
            if(board[x][y] == 0) return y;
        }
        return game.getHeight();
    }
    
    private int[][] getBoard() {
        
        int[][] board = new int[game.getWidth()][game.getHeight()];
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                board[x][y] = game.getStone(x, y);
            }
        }
        return board;
    }
    
    @Override
    public String getName() { return "SwagC4"; }
}
