package strategybots.bots.legacy;

public abstract class GameState {
    
    private final GameState PARENT;
    
    //private int a, b;
    
    private int score = -Integer.MAX_VALUE;
    private GameState move;
    
    protected GameState(GameState parent) {
        PARENT = parent;
    }
    
    protected GameState() { PARENT = null; }
    
    public final int getScore() { return score; }
    
    public final GameState getBestMove() { return move; }
    
    public final void evaluate(long millis) {
        
        long timeout = System.currentTimeMillis() + millis;
        
        for(int i = 0; System.currentTimeMillis() < timeout; i++) {
            evaluate(i, timeout);
        }
    }
    
    private void evaluate(int depth, long timeout) {
        
        if(System.currentTimeMillis() > timeout) return;
        
        if(depth == 0 || isTerminal()) {
            score = heuristic();
        
        } else {
            
            while(hasSuccessor()) {
                
                GameState successor = nextSuccessor();
                successor.evaluate(depth - 1, timeout);
                
                if(move == null || -successor.getScore() > move.getScore()) {
                    move = successor;
                    score = move.getScore();
                }
                reset();
            }
        }
    }
    
    /*private synchronized void setA(int a) {
        
        if(a > this.a) {
            this.a = a;
            
            if(PARENT != null) {
                PARENT.setA(a);
            }
        }
    }
    
    private synchronized void setB(int b) {
        
        if(b < this.b) {
            this.b = b;
            
            if(PARENT != null) {
                PARENT.setB(b);
            }
        }
    }*/
    
    protected abstract boolean isTerminal();
    
    protected abstract int heuristic();
    
    protected abstract boolean hasSuccessor();
    
    protected abstract GameState nextSuccessor();
    
    protected abstract void reset();
    
    protected abstract GameState fork();
}