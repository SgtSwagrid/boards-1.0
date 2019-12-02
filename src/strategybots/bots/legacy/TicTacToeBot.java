package strategybots.bots.legacy;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import strategybots.games.TicTacToe;
import strategybots.games.base.Game.Player;

public class TicTacToeBot implements Player<TicTacToe> {
    
    private TicTacToe game;
    public TicTacToe getGame() { return game; }
    
    private int width = game.getWidth();
    private int height = game.getHeight();
    private int target = game.getTarget();
    
    private int playerId;
    public int getPlayerId() { return playerId; }
    
    @Override
    public void init(TicTacToe game, int playerId) {
        this.game = game;
        this.playerId = playerId;
    }
    
    @Override
    public void takeTurn(TicTacToe game, int playerId) {
        new TicTacToeState()
            .evaluate(2000L)
            .getBestMove()
            .makeMove();
    }
    
    private class TicTacToeState extends GameTree {
        
        private byte[][] board;
        private int x, y;
        private int heuristic = Integer.MIN_VALUE;
        
        private TicTacToeState() {
            board = new byte[width][height];
            game.forEachPosition((x, y) ->
                board[x][y] = (byte)game.getStone(x, y));
        }

        private TicTacToeState(GameTree parent, byte[][] board, int x, int y) {
            super(parent);
            this.board = board;
            this.x = x;
            this.y = y;
        }

        @Override
        public Iterator<GameTree> successors() {
            return new Iterator<GameTree>() {
                
                private int x = 0, y = 0;
                private boolean incremented = true, hasNext = true;
                
                private boolean increment() {
                    while(game.getStone(x, y) == 0 && y < height) {
                        if(++x >= width) { x = 0; y++; }
                    }
                    return y < height;
                }

                @Override
                public boolean hasNext() {
                    if(!incremented) {
                        hasNext = increment();
                        incremented = true;
                    }
                    return hasNext;
                }

                @Override
                public GameTree next() {
                    if(!hasNext()) throw new NoSuchElementException();
                    byte[][] newBoard = Arrays.stream(board)
                            .map(byte[]::clone).toArray(byte[][]::new);
                    return new TicTacToeState(TicTacToeState.this, newBoard, x, y);
                }
            };
        }
        
        @Override
        public int heuristic() {
            
            if(heuristic != Integer.MIN_VALUE) return heuristic;
            
            for(int i = 0; i < 4; i++) {
                
                int dx = i < 2 ? 1 : (i < 3 ? 0 : -1);
                int dy = i < 1 ? 0 : 1;
                
                int xn = dx > 0 ? x : (dx < 0 ? width-x-1 : target-1);
                xn = xn < target-1 ? xn : target-1;
                int xp = xn != 0 ? width-xn-1 : target-1;
                
                int yn = dy != 0 && y < target-1 ? y : target-1;
                int yp = dy != 0 ? height-yn-1 : target-1;
                
                int n = xn < yn ? xn : yp;
                int p = xp < yp ? xp : yp;
                
                int cf = 0, ce = 0;
                
                for(int j = -n; p-j+1 <= target; j++) {
                    
                     
                }
            }
            
            return 0;
        }

        @Override
        public boolean isTerminal() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void makeMove() {
            game.placeStone(x, y);
        }
    }
}
