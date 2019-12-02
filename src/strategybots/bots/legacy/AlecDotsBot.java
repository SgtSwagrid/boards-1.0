package strategybots.bots;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.util.Game.Player;
import strategybots.graphics.Tile;


public class AlecDotsBot implements Player<DotsAndBoxes> {
    
    Random rand = new Random();
    
    @Override
    public void init(DotsAndBoxes game, int playerId) {}

    @Override
    public void takeTurn(DotsAndBoxes game, int playerId) {
        
        this.game = game;
        
        boolean[][][] state = getState(game);
        
        List<int[]> tiles = new LinkedList<>();
        Set<int[]> remainingTiles = new HashSet<>();
        
        Set<Set<int[]>> chains = new HashSet<>();
        Set<Set<int[]>> primedChains = new HashSet<>();
        
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                tiles.add(new int[] {x, y});
            }
        }
        
        while(!tiles.isEmpty()) {
            
            
            
            int[] tile = tiles.remove(0);
            
            Set<int[]> chain = new HashSet<>();
            Deque<int[]> queue = new ArrayDeque<>();
            
            boolean primed = false;
            
            queue.add(tile);
            while(!queue.isEmpty()) {
                
                int[] t = queue.poll();
                
                //System.out.println("hello world");
                
                if(numSides(state, t[0], t[1]) == 2 || numSides(state, t[0], t[1]) == 3) {
                    
                    if(chain.stream().anyMatch(tt -> tt[0]== t[0] && tt[1]==t[1])) {
                        System.out.println("hi");
                        System.out.println(queue.size());
                        continue;
                    }
                    
                    if(numSides(state, t[0], t[1]) == 3) primed = true;
                    
                    
                    
                    
                    chain.add(tile);
                    
                    if(tile[1]>0 && !state[0][tile[0]][tile[1]]) queue.add(new int[] {t[0], t[1]-1});
                    if(tile[0]>0 && !state[1][tile[0]][tile[1]]) queue.add(new int[] {t[0]-1, t[1]});
                    if(tile[1]<game.getHeight()-1 && !state[0][tile[0]][tile[1]+1]) queue.add(new int[] {t[0], t[1]+1});
                    if(tile[0]<game.getWidth()-1 && !state[0][tile[0]+1][tile[1]]) queue.add(new int[] {t[0]+1, t[1]});
                }
            }
            
            if(!chain.isEmpty()) {
                if(primed) primedChains.add(chain);
                else chains.add(chain);
            } else {
                
                
                remainingTiles.add(tile);
                
            }
        }
        
        if(!primedChains.isEmpty()) {
            
            int smallestSize = 10000;
            Set<int[]> smallestChain = null;
            
            for(Set<int[]> chain : primedChains) {
                if(chain.size() < smallestSize) {
                    smallestSize = chain.size();
                    smallestChain = chain;
                }
            }
            
            for(int[] tile : smallestChain) {
                
                if(numSides(state, tile[0], tile[1]) == 3) {
                    
                    placeRemaining(game, tile[0], tile[1]);
                    return;
                }
            }
        } else if(remainingTiles.isEmpty()) {
            
            Set<int[]> s = null;
            for(Set<int[]> chain : chains) {
                if(chain.size() < s.size()) s = chain;
            }
            
            for(int[] tile : s) {
                placeRemaining(game, tile[0], tile[1]);
                return;
            }
            
        } else {
            for(int[] t:remainingTiles) {
                int x = t[0], y = t[1];
                //if() {
                    if(!game.hasLine(Orien.VERT, x, y)) game.drawLine(Orien.VERT, x, y);
                    else if(!game.hasLine(Orien.VERT, x+1, y)) game.drawLine(Orien.VERT, x+1, y);
                    else if(!game.hasLine(Orien.HORZ, x, y)) game.drawLine(Orien.HORZ, x, y);
                    else if(!game.hasLine(Orien.HORZ, x, y+1)) game.drawLine(Orien.HORZ, x, y+1);
                    return;
                //}
            }
        }
        
        game.drawLine(rand.nextInt(2)==1 ? Orien.VERT : Orien.HORZ,
                rand.nextInt(game.getWidth()), rand.nextInt(game.getHeight()));
    }
    
    
    private int[] minimax(DotsAndBoxes game, boolean[][][] state, int depth, int a, int b) {
        
        for(Orien orien : Orien.values()) {
            
            int width = game.getWidth() + (orien==Orien.VERT?1:0);
            int height = game.getHeight() + (orien==Orien.HORZ?1:0);
            
            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    
                    
                }
            }
        }
        
        return new int[] {0, 0};
    }
    
    DotsAndBoxes game;
    
    private int numSides(boolean[][][] state, int x, int y) {
        
        if(x<0 || y<0 || x>=game.getWidth() || y>=game.getHeight()) return 0;
        
        return (state[0][x][y]?1:0) + (state[0][x][y+1]?1:0) +
                (state[1][x][y]?1:0) + (state[1][x+1][y]?1:0);
    }
    
    private void placeRemaining(DotsAndBoxes game, int x, int y) {
        if(!game.hasLine(Orien.VERT, x, y)) game.drawLine(Orien.VERT, x, y);
        else if(!game.hasLine(Orien.VERT, x+1, y)) game.drawLine(Orien.VERT, x+1, y);
        else if(!game.hasLine(Orien.HORZ, x, y)) game.drawLine(Orien.HORZ, x, y);
        else if(!game.hasLine(Orien.HORZ, x, y+1)) game.drawLine(Orien.HORZ, x, y+1);
    }
    
    private boolean[][][] getState(DotsAndBoxes game) {
        
        boolean[][][] state = new boolean[2][][];
        
        state[0] = new boolean[game.getWidth()][game.getHeight()+1];
        state[1] = new boolean[game.getWidth()+1][game.getHeight()];
        
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight()+1; y++) {
                state[0][x][y] = game.hasLine(Orien.HORZ, x, y);
            }
        }
        
        for(int x = 0; x < game.getWidth()+1; x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                state[1][x][y] = game.hasLine(Orien.VERT, x, y);
            }
        }
        return state;
    }
    
    private Square[][] getSquares(DotsAndBoxes game) {
        
        Square[][] squares = new Square[game.getWidth()][game.getHeight()];
        
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                squares[x][y] = new Square(x, y);
            }
        }
        
        //for(int x = 0)
    }
    
    @Override
    public String getName() { return "Alec Bot"; }
    
    private class Square {
        
        int x, y;
        
        Edge right, above, left, below;
        
        int sides;
        
        Square(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private class Edge {
        
        Square square1, square2;
        
        boolean line = false;
        
        Edge(Square s1, Square s2) {
            square1 = s1;
            square2 = s2;
        }
        
        Square getOther(Square square) {
            return square==square1 ? square2:square1;
        }
        
        void setLine(boolean l) {
            
            if(!line && l) {
                square1.sides++;
                square2.sides++;
                
            } else if(line && !l) {
                square1.sides--;
                square2.sides--;
            }
            line = l;
        }
    }
}
