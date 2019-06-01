package strategybots.bots;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;

import strategybots.games.HyperMNK;
import strategybots.games.HyperMNK.HyperMNKPlayer;

public class SwagmaxC4 implements HyperMNKPlayer {
    
    private final int MULTI_THREADING_DEPTH = 2;
    
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
        
        int depth = MULTI_THREADING_DEPTH + 1;
        
        long startTime = System.currentTimeMillis();
        
        Branch root = null;
        
        while(System.currentTimeMillis() - startTime < time && depth < 100) {
            
            root = new Branch(Optional.empty(), board, heights, playerId,
                    depth, MULTI_THREADING_DEPTH, -1000, 1000);
            System.out.println("hey");
            root.awaitResponse();
            System.out.println("howdy");
            depth++;
        }
        System.out.println("\n======== Minimax Statistics =========");
        System.out.println("Move selected: " + root.getMove());
        System.out.println("Evaluation metric: " + root.getScore());
        System.out.println("Maximum allowed depth: " + depth);
        System.out.println("=====================================\n");
        
        return root.getMove();
    }
    
    private int minimaxRecurse(Branch branch, boolean root, byte[][] board,
            byte[] heights, int playerId, int depth, int a, int b) {
        
        int score = depth == 1 ? 0 : -1000;
        int move = -1;
        
        for(int x = 0; x < width; x++) {
            
            int lower = branch.getLower(playerId);
            int upper = branch.getUpper(playerId);
            a = lower > a ? lower : a;
            b = upper < b ? upper : b;
            
            if(heights[x] == height) continue;
            board[x][heights[x]++] = (byte) playerId;
            
            if(detectWin(board, playerId, x, heights[x] - 1)) {
                
                board[x][--heights[x]] = 0;
                return depth * 10;
                
            } else if(depth > 1) {
                
                int s = -minimaxRecurse(branch, false, board, heights,
                        playerId % 2 + 1, depth - 1, -b, -a);
                
                if(s > score || (s == score && Math.abs(width / 2 - x)
                        < Math.abs(width / 2 - move))) {
                    
                    score = s;
                    move = x;
                    a = s > a ? s : a;
                }
            }
            board[x][--heights[x]] = 0;
            if(a >= b) break;
        }
        if(root) {
            branch.proposeMove(move, score);
        }
        return score;
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
    
    private byte[][] copy(byte[][] a) {
        byte[][] b = new byte[a.length][a[0].length];
        for(int i = 0; i < a.length; i++) {
            for(int j = 0; j < a[0].length; j++) {
                b[i][j] = a[i][j];
            }
        }
        return b;
    }
    
    @Override
    public String getName() { return "Swagmax"; }
    
    private class Branch {
        
        Set<Branch> children = new HashSet<>();
        Optional<Branch> parent = Optional.empty();
        int pendingChildren = 0;
        
        byte[][] board;
        byte[] heights;
        
        int playerId;
        
        int depth, t_depth;
        
        int a, b;
        
        int score = -100, move = -1;
        
        Semaphore lock = new Semaphore(1);
        Semaphore wait = new Semaphore(1);
        
        Branch(Optional<Branch> parent, byte[][] board, byte[] heights,
                int playerId, int depth, int t_depth, int a, int b) {
            
            this.parent = parent;
            this.board = board;
            this.heights = heights;
            this.playerId = playerId;
            this.depth = depth;
            this.t_depth = depth;
            this.a = a;
            this.b = b;
            
            awaitResponse();
            createChildren();
        }
        
        void createChildren() {
            
            if(t_depth == 0) {
                
                pendingChildren++;
                
                new Thread() { @Override public void run() {
                    minimaxRecurse(Branch.this, true, board, heights, playerId, depth, a, b);
                }}.start();
            
            } else {
                
                for(int x = 0; x < width; x++) {
                    
                    if(heights[x] == height) continue;
                    
                    byte[][] board = copy(this.board);
                    byte[] heights = this.heights.clone();
                    
                    board[x][heights[x]++] = (byte) playerId;
                    
                    pendingChildren++;
                    
                    if(detectWin(board, playerId, x, heights[x] - 1)) {
                        pendingChildren = 0;
                        proposeMove(x, depth * 10);
                        
                    } else {
                        children.add(new Branch(Optional.of(this), board, heights,
                                playerId % 2 + 1, depth - 1, t_depth - 1, -b, -a));
                    }
                }
            }
        }
        
        void proposeMove(int move, int score) {
            
            if(score > this.score) {
                
                this.move = move;
                this.score = score;
                updateLower(a);
            }
            
            if(--pendingChildren <= 0) {
                
                if(parent.isPresent()) {
                    delete();
                    parent.get().proposeMove(this.move, -this.score);
                    
                } else {
                    System.out.println("sup");
                    wait.release();
                }
            }
        }
        
        int getLower(int playerId) {
            
            acquireLock();
            int a = playerId == this.playerId ? this.a : -this.b;
            lock.release();
            return a;
        }
        
        int getUpper(int playerId) {
            
            acquireLock();
            int b = playerId == this.playerId ? this.b : -this.a;
            lock.release();
            return b;
        }
        
        void updateLower(int a) {
            
            acquireLock();
            
            if(this.a > a) {
                
                this.a = a;
                if(a >= b) delete();
                children.forEach(c -> c.updateUpper(-a));
            }
            lock.release();
        }
        
        void updateUpper(int b) {
            
            acquireLock();
            
            if(this.b < b) {
                
                this.b = b;
                if(a >= b) delete();
                children.forEach(c -> c.updateLower(-b));
            }
            lock.release();
        }
        
        void acquireLock() {
            
            try {
                lock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        void awaitResponse() {
            
            try {
                wait.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        int getMove() { return move; }
        
        int getScore() { return score; }
        
        void delete() {
            
            if(parent.isPresent()) {
                parent.get().acquireLock();
                parent.get().children.remove(this);
                parent.get().lock.release();
            }
            
            acquireLock();
            children.forEach(Branch::delete);
            children.clear();
            lock.release();
        }
    }
}