package strategybots.bots.legacy;

import java.util.Iterator;
import java.util.Optional;

public abstract class GameTree {
    
    private int score = -Integer.MIN_VALUE;
    public int getScore() { return score; }
    
    private GameTree move;
    public GameTree getBestMove() { return move; }
    
    private GameTree parent;
    public Optional<GameTree> getParent() {
        return Optional.ofNullable(parent);
    }
    
    protected GameTree() {}
    
    protected GameTree(GameTree parent) { this.parent = parent; }
    
    public final GameTree evaluate(long timeMs) {
        
        long timeout = System.currentTimeMillis() + timeMs;
        
        for(int i = 0; System.currentTimeMillis() < timeout; i++) {
            evaluate(i, timeout);
        }
        return this;
    }
    
    private void evaluate(int depth, long timeout) {
        
        if(System.currentTimeMillis() > timeout) return;
        
        if(depth == 0 || isTerminal()) {
            score = heuristic();
        
        } else {
            
            Iterator<GameTree> successors = successors();
            while(successors.hasNext()) {
                
                GameTree successor = successors.next();
                successor.evaluate(depth-1, timeout);
                
                if(move == null || -successor.getScore() > move.getScore()) {
                    move = successor;
                    score = move.getScore();
                }
            }
        }
    }
    
    public abstract Iterator<GameTree> successors();
    
    public abstract int heuristic();
    
    public abstract boolean isTerminal();
    
    public abstract void makeMove();
}