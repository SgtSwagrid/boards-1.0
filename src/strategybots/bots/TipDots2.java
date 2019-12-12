package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 8 Dec 2019
 * 
 * A MCTS based Othello bot. Currently still in the debug and dev phase, however still plays well against a moderate human.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.Reversi;
import strategybots.games.base.Game.Player;

public class TipDots2 implements Player<DotsAndBoxes>{

	private long time = 2000l;
	private Random rand;
	
	private int width, height;
	private int turn = 0;
	
	private int beamFactor = 10;
	private int masterDepth = 0;
	
	public TipDots2() {
		rand = new Random();
		System.out.println("Tip's Dots and Boxes Bot Loaded");
	}
	
	public TipDots2(long millis) {
		this();
		this.time = millis;
	}
	
	@Override 
	public void init(DotsAndBoxes game, int playerId) {
		this.width = game.getWidth();
		this.height = game.getHeight();
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {

		long start = System.currentTimeMillis();
		int[] move = getMove(game, playerId);
		
		Side side = null;
	        
        if (move[3] == 1) side = Side.TOP; 
        else if (move[3] == 2) side = Side.RIGHT;
        else if (move[3] == 4) side = Side.BOTTOM;
        else if (move[3] == 8) side = Side.LEFT;
		
		game.drawLine(side, move[1], move[2]);
		printStats(move[0], playerId, move[1], move[2], move[3], move[4], start);
		
	}
	
    private int[] getMove(DotsAndBoxes game, int playerId) {
        
        int score = 0, moveX = -1, moveY = -1, moveSide = -1, depth = 1;
        int maxDepth = width * height;
        long start = System.currentTimeMillis();
        
        int[][] board = getBoard(game);
        int[] captures = getScores(game);
        
        System.out.println("Captures start " + captures[0] + " , " + captures[1]);
        
        for(; depth < maxDepth; depth++) {
            
        	masterDepth = depth;
            int[] result = minimax(board, captures, playerId, depth,
                    -Integer.MAX_VALUE, Integer.MAX_VALUE);
            score = result[0];
            moveX = result[1];
            moveY = result[2];
            moveSide = result[3];
            
            if(System.currentTimeMillis()-start > time) break;
        }
        
        return new int[] {score, moveX, moveY, moveSide, depth};
    }
    
    private int[] minimax(int[][] board, int[] captures, int playerId,
            int depth, int a, int b) {
        
        int score = 0, moveX = -1, moveY = -1, moveSide = -1, iters = 0;
        boolean skipTwos = false;
        
        ArrayList<Move> moves = getMoves(board);
        if (moves.size() > 0 && moves.get(0).prio != -1 && masterDepth < 10) skipTwos = true;
        // preorder moves TODO
        
        for (Move move : moves) { 
            
        	if (move.prio == -1 && skipTwos) break;
        	int x = move.x, y = move.y, side = move.orien;
        	// Do move as PLACING
        	boolean placed = applyMove(board, captures, playerId, move, true);
        	int h = heuristic(captures, playerId);
        	
            if(checkWin(board, playerId, h)) {
            	applyMove(board, captures, playerId, move, false);
                return new int[] {h*1000, x, y, move.orien};
            }
            
            int nextPlayer = placed ? playerId : playerId % 2 + 1 ;
            int s = depth<=1 ? h : (placed ? 1 : -1) * minimax(board, captures, nextPlayer, depth-1, -b, -a)[0];
            
            if(s > score || moveX == -1) {
                score = s;
                moveX = x;
                moveY = y;
                moveSide = side;
                a = score > a ? score : a;
            }
            
            // undo move
            applyMove(board, captures, playerId, move, false);
            if(a >= b) break;
            
            iters++;
            if (iters >=  beamFactor) break; 
            
        }
                
        return new int[] {score, moveX, moveY, moveSide};
    }
	
    // returns true if the player PlayerId has won. 
    private boolean checkWin(int[][] board, int playerId, int heur) {
        
    	//int heur = heuristic(captures, playerId);
    	ArrayList<Move> moves = getMoves(board);
    	// TRUE if the margin in PLAYERS favour is positive and there are no more moves.
    	return (heur > 0 && moves.size() == 0);
    }

    
    private int heuristic(int[] captures, int playerId) {
    	return captures[playerId-1] - captures[(3 - playerId) - 1];
    }
    
    
    private boolean applyMove(int[][] board, int[] captures, int playerId, Move move, boolean placing) {
    	
    	int width = board.length, height = board[0].length;
    	boolean captured = false;
    	Move move2 = new Move(move.x, move.y, move.orien, move.prio);
    	
    	// right
    	if (move.orien == 2) {
    		if (move.x + 1 < height) { move2.x++; move2.orien = 8; } else move2 = null;
    	} else if (move.orien == 1) { // top
    		if (move.y + 1 < width) { move2.y++; move2.orien = 4; } else move2 = null;
    	} else if (move.orien == 8) { // left
    		if (move.x - 1 >= 0) { move2.x--; move2.orien = 2; } else move2 = null;
    	} else if (move.orien == 4) { // bottom
    		if (move.y - 1 >= 0) { move2.y--; move2.orien = 1; }else move2 = null;
    	}
    	
    	board[move.x][move.y] += placing ? move.orien : 0 ; 
    	if (board[move.x][move.y] == 15) {
    		captured = true;
    		captures[playerId - 1] += placing ? 1 : -1;
    	}
    	board[move.x][move.y] += placing ? 0 : -move.orien ; 
    	
    	if (move2 != null) {
        	board[move2.x][move2.y] += placing ? move2.orien : 0 ; 
        	if (board[move2.x][move2.y] == 15) {
        		captured = true;
        		captures[playerId - 1] += placing ? 1 : -1;
        	}
        	board[move2.x][move2.y] += placing ? 0 : -move2.orien ; 
        }
    	
    	return captured;
    }
    
    
    private ArrayList<Move> getMoves(int[][] board) {
    	
		ArrayList<Move> valid = new ArrayList<Move>();
        int width = board.length;
        int height = board[0].length;
		
        int[] sides = new int[] {8, 4, 2, 1};
        int sideLimit = 4;
        
    	for (int x = 0 ; x < width; x++) {
			for (int y = 0 ; y < height; y++) {
				int sideSum = 15-board[x][y];				
				for (int ii = 0 ; ii < sideLimit; ii++) {

					if (sideSum - sides[ii] >= 0) {
						//if ((x == width - 1 && sides[ii] == 2) ||
						//		(y == height - 1 && sides[ii] == 1) ||
						//		sides[ii] == 4 || sides[ii] == 8 ) {
							boolean twin = board[x][y] == 3 || board[x][y] == 5 || board[x][y] == 9 || board[x][y] == 6 || board[x][y] == 10 || board[x][y] == 12;
							boolean single = board[x][y] == 1 || board[x][y] == 2 || board[x][y] == 4 || board[x][y] == 8;
							boolean cap = board[x][y] == 14 ||  board[x][y] == 13 ||  board[x][y] == 11 ||  board[x][y] == 7;
							valid.add(new Move(x, y, sides[ii], cap ? 4 : (single ? 1 : twin ? -1 : 0)));
						//}
						sideSum -= sides[ii];
					}
				}
			}
    	}
    	
    	valid.sort(movePriorityComparator);
		return valid;
    }
    
    
	public final Comparator<Move> movePriorityComparator = new Comparator<Move>() {         
		@Override         
		public int compare(Move m1, Move m2) {             
			return (m2.prio < m1.prio ? -1 :                     
				(m2.prio == m1.prio ? 0 : 1));           
		}     
	}; 
    
    
	private int[] getScores(DotsAndBoxes game) {
		return new int[] { game.getScore(1), game.getScore(2) };
	}
	
    private void printStats(int score, int playerId, int moveX, int moveY, int moveSide, int depth, long start) {
        
        System.out.println("=======================");
        System.out.println("Tiptaco's Better DNB Statistics:");
        System.out.println("Player:      " + playerId
                + " ("+(playerId==1?"Yellow":"Red")+")");
        System.out.println("Turn:        " + turn++);
        System.out.println("Expectation: " + score);
        System.out.println("Position:    (" + (moveX+1) + ", " + (moveY+1) + ", " + moveSide + ")");
        System.out.println("Depth:       " + depth);
        System.out.println("Time:        "
                + (System.currentTimeMillis() - start) + "ms");
    }
	
	private int[][] getBoard(DotsAndBoxes game) {

		int[][] board = new int[game.getWidth()][game.getHeight()];
		
		for (int yy = game.getHeight()-1 ; yy >= 0; yy--) {
			for (int xx = 0 ; xx < game.getWidth(); xx++) {
				board[xx][yy] += game.hasLine(Side.TOP, xx, yy) ? 1 : 0;
				board[xx][yy] += game.hasLine(Side.RIGHT, xx, yy) ? 2 : 0;
				board[xx][yy] += game.hasLine(Side.BOTTOM, xx, yy) ? 4 : 0;
				board[xx][yy] += game.hasLine(Side.LEFT, xx, yy) ? 8 : 0;
				System.out.print(board[xx][yy] + ", ");
			}
			System.out.println("; ");
		}
		
		return board;
		
	}
	
    @Override
    public String getName() { return "TipTacos's Dots and Boxes MCTS"; }
	
    class Move {
    	
    	int x = 0, y = 0, orien = 0;
    	int prio = 0;
    	
    	public Move() {}
    	
    	public Move (int x, int y, int orien) {
    		this.x = x;
    		this.y = y;
    		this.orien = orien;
    	}
    	
    	public Move (int x, int y, int orien, int prio) {
    		this(x, y, orien);
    		this.prio = prio;
    	}
    }
}
