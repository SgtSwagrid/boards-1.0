package strategybots.bots;

/* Author: Adrian Shedley with help from Alec Dorrington
 * Date: 8 Dec 2019
 * 
 * A MCTS based Othello bot. Currently still in the debug and dev phase, however still plays well against a moderate human.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Side;
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
		// Use minimax to get the  
		Triple result = getMove(game, playerId);
		
		// Convert from side orientation number to SIDE enum
		ArrayList<Move> moveStack = new ArrayList<Move>(); 
		if (result.move.isCompound()) {
			moveStack.addAll(result.move.moves);
		} else {
			moveStack.add(result.move);
		}
		Side side = null;
		
		for (Move move : moveStack) {
	        if (move.orien == 1) side = Side.TOP; 
	        else if (move.orien == 2) side = Side.RIGHT;
	        else if (move.orien == 4) side = Side.BOTTOM;
	        else if (move.orien == 8) side = Side.LEFT;
			
			game.drawLine(side, move.x, move.y);
			printStats(result.score, playerId, move.x, move.y, move.orien, result.depth, start);
		}
	}
	
	
	/** 
	 * Use Minimax to determine the best move from the current boardstate and playerId
	 * @param game the DotsAndBoxes game instance
	 * @param playerId the player to maximise score for
	 * @return An array of values {SCORE, X, Y, ORIENTATION, DEPTH}
	 */
    private Triple getMove(DotsAndBoxes game, int playerId) {
        
        int score = 0, depth = 1;
        Move move = null;
        int maxDepth = 2 * width * height + width + height;
        long start = System.currentTimeMillis();
        
        int[][] board = getBoard(game);
        int[] captures = getScores(game);
        
        System.out.println("Captures start " + captures[0] + " , " + captures[1]);
        
        // Iteratively deepen the minimax tree between depth 1 and depth height*width
        for(; depth < maxDepth; depth++) {
        	masterDepth = depth;
            Triple result = minimax(board, captures, playerId, depth,
                    -Integer.MAX_VALUE, Integer.MAX_VALUE);
            score = result.score;
            move = result.move;
            
            if(System.currentTimeMillis()-start > time) break;
        }
        
        return new Triple(score, move, depth);
    }
    
    /**
     * The recursive minimax function will select evaluate all successor states and maximise for the highest score.
     * BeamFactor will search only a limited branching factor to save on branching factor
     * @param board A 2D array representing the sides present in each box
     * @param captures An array containing the number of captures for player1 and player2
     * @param playerId The player to maximise for
     * @param depth The maximum number of layers to traverse from here
     * @param a Alpha limit value
     * @param b Beta limit value
     * @return An output array for {SCORE, X, Y, ORIENTATON}
     */
    private Triple minimax(int[][] board, int[] captures, int playerId,
            int depth, int a, int b) {
        
        int score = 0, iters = 0; 
        Move bestMove = null;
        ArrayList<Move> moves = getMoves(board);
        
        for (Move move : moves) { 
            
        	/*String dept = "";
        			
        	for (int i = 0 ; i < 8-depth; i++) {
        		dept += "  ";
        	}*/
        	
        	//System.out.println(dept + move + " prio " + move.prio);
        	
        	// Do move as PLACING
        	boolean hasCaptured = applyMove(board, captures, playerId, move, true);
        	
        	int h = heuristic(captures, playerId);
        	
            if(moves.size() == 1) { //checkWin(board, playerId, h)) {
            	
            	applyMove(board, captures, playerId, move, false);
            	//System.out.println("Terminal move " + move);
                return new Triple(h, move, depth);
            }
            
            //int usedDepth = move.isCompound() ? move.moves.size() : 1;
            int nextPlayer = hasCaptured ? playerId : (3-playerId) ;
            int s = (depth<1) ? h : (hasCaptured ? 
            		minimax(board, captures, nextPlayer, depth-1, a, b).score : 
            		-minimax(board, captures, nextPlayer, depth-1, -b, -a).score);
            
            if (s == 0) {
            	//System.out.println("Score is 0 at depth " + depth);
            }
            
        	/*if (depth < 10) {
            	String dept = "";
        		for (int i = 0 ; i < 9-depth; i++) {
            		dept += "    ";
            	}
        		System.out.println(dept + s + " MOVE: " + move + " : " + move.prio + " [" + captures[0] + ", " + captures[1] + "]");
        	}*/
  
            
            if(s > score || bestMove == null ) {
                score = s;
                bestMove = move;
                a = score > a ? score : a;
            }
            
            // undo move
            applyMove(board, captures, playerId, move, false);
            
            /*if (depth > 1 && moves.size() == 1) {
            	System.out.println("Uh oh stinky at depth " + depth);
            	printBoard(board);
            }*/
            
            if(a >= b) break;
            
            iters++;
            if (iters >= beamFactor*2) break; 
        }
                
        return new Triple(score, bestMove, depth);
    }
	
    /**
     * Check if the player specified has won on the board state shown
     * @param board
     * @param playerId
     * @param heur
     * @return True if the player passed in has won, draws and losses return false
     */
    private boolean checkWin(int[][] board, int playerId, int heur) {
        
    	//int heur = heuristic(captures, playerId);
    	ArrayList<Move> moves = getMoves(board);
    	// TRUE if the margin in PLAYERS favour is positive and there are no more moves.
    	return (heur >= 0 && moves.size() == 0);
    }

    /**
     * Heuristic that returns the margin between the input player and the opponent.
     * @param captures
     * @param playerId
     * @return A positive number is a winning position for the specified player.
     */
    private int heuristic(int[] captures, int playerId) {
    	return captures[playerId-1] - captures[(3 - playerId) - 1];
    }
    
    /**
     * Applies the move to the board and captures arrays specified for the player and move. The placing boolean
     * controls if the move is being done or undone. 
     * @param board The board denoting each cells state
     * @param captures The counts of cells captures for each player
     * @param playerId The placing player's Id
     * @param move The move to be played out or undone
     * @param placing TRUE when placing and capturing, FALSE when undoing a move and retracting a capture.
     * @return TRUE if at least one square was captured or released. 
     */
    private boolean applyMove(int[][] board, int[] captures, int playerId, Move move, boolean placing) {
    	
    	int width = board.length, height = board[0].length;
    	boolean captured = false, lastMove = true;
    	int iters = 0;
    	
    	//ArrayList<Move> toExecute = new ArrayList<Move>();
    	
    	//if (moveInput.isCompound()) {
    	//	toExecute.addAll(moveInput.moves);
    	//	if (placing == false) Collections.reverse(toExecute);
    	//} else {
    		//toExecute.add(moveInput);
    	//}
    	
    	//for (Move move : toExecute) {
	    	Move move2 = new Move(move.x, move.y, move.orien, move.prio);
	
	    	if (move.orien == 2) { // right
	    		if (move.x + 1 < width) { move2.x++; move2.orien = 8; } else move2 = null;
	    	} else if (move.orien == 1) { // top
	    		if (move.y + 1 < height) { move2.y++; move2.orien = 4; } else move2 = null;
	    	} else if (move.orien == 8) { // left
	    		if (move.x - 1 >= 0) { move2.x--; move2.orien = 2; } else move2 = null;
	    	} else if (move.orien == 4) { // bottom
	    		if (move.y - 1 >= 0) { move2.y--; move2.orien = 1; } else move2 = null;
	    	}
	    	
	    	board[move.x][move.y] += placing ? move.orien : 0 ; 
	    	if (board[move.x][move.y] == 15) {
	    		captured = true && lastMove;
	    		captures[playerId - 1] += placing ? 1 : -1;
	    	}
	    	board[move.x][move.y] += placing ? 0 : -move.orien ; 
	    	
	    	if (move2 != null) {
	        	board[move2.x][move2.y] += placing ? move2.orien : 0 ; 
	        	if (board[move2.x][move2.y] == 15) {
	        		captured = true && lastMove;
	        		captures[playerId - 1] += placing ? 1 : -1;
	        	}
	        	board[move2.x][move2.y] += placing ? 0 : -move2.orien ; 
	        }
	    	
	    	//iters++;
	    	//if (iters == toExecute.size() - 1) lastMove = true;
    	//}
    	
    	return captured;
    }
    
    private ArrayList<Move> getCompoundMoves(int[][] board, Move seedMove) {

    	ArrayList<Move> outputMoves = new ArrayList<Move>();
    	Move compoundMove = new Move(seedMove);
    	compoundMove.addMove(seedMove);
    	int cX = seedMove.x, cY = seedMove.y, cSide = seedMove.orien;
    	
    	do {
	    	if (cSide == 2) { // right
	    		if (cX + 1 < width) { cX++; } else break;
	    	} else if (cSide == 1) { // top
	    		if (cY + 1 < height) { cY++; } else break;
	    	} else if (cSide == 8) { // left
	    		if (cX - 1 >= 0) { cX--;} else break;
	    	} else if (cSide == 4) { // bottom
	    		if (cY - 1 >= 0) { cY--; } else break;
	    	}
	    	
	    	if (cX < 0 || cY < 0) System.out.println(cX + " ,  " + cY);
	    	if (getSides(board[cX][cY]) == 2) {
	    		int cSideD = cSide >= 4 ? (cSide / 4) : (cSide * 4);
	    		cSide = 15 - board[cX][cY] - cSideD;
	    		compoundMove.addMove(new Move(cX, cY, cSide, seedMove.prio));
	    	}
	    	
    	} while (getSides(board[cX][cY]) == 2);

    	// once ended, if length >= 2, clone and remove second last move. 
    	if (compoundMove.isCompound() && compoundMove.moves.size() >= 2) {
    		Move compoundMove2 = new Move();
    		compoundMove2.addMove(compoundMove);
    		compoundMove2.moves.remove(compoundMove.moves.size() - 2);
    		
    		outputMoves.add(compoundMove);
    		outputMoves.add(compoundMove2);
    	} else {
    		outputMoves.add(seedMove);
    	}
    	
    	return outputMoves;
    }

    
    /**
     * Gets a list of all valid moves for the current board state. Zero moves means that the game is finished.
     * Each type of move is ranked and the order is dependent on how many edges the current cell has. For example:
     * A cell with TWO existing sides is ranked with less precedence than one with THREE already filled sides
     * @param board The board containing each cell's state
     * @return An ArrayList of all single valid moves
     */
    private ArrayList<Move> getMoves(int[][] board) {
    	
		ArrayList<Move> valid = new ArrayList<Move>();
		ArrayList<Move> caps = new ArrayList<Move>();
        int width = board.length;
        int height = board[0].length;
		
        int[] sides = new int[] {8, 4, 2, 1};
        int sideLimit = 4;
        
    	for (int x = 0 ; x < width; x++) {
			for (int y = 0 ; y < height; y++) {
				int sideSum = 15-board[x][y];	
				
				int numSides = getSides(board[x][y]);
				boolean twin = numSides == 2;
				boolean single = numSides == 1;
				boolean cap = numSides == 3;
								
				for (int ii = 0 ; ii < sideLimit; ii++) {
						
					if (sideSum - sides[ii] >= 0) {
						

						Move move = new Move(x, y, sides[ii], cap ? 4 : (single ? 1 : (twin ? -1 : 0)));
						if (cap) {
							caps.add(move);
						} else {
							valid.add(move);
						}

						sideSum -= sides[ii];
					}
				}
			}
    	}
    	
    	// Compound move shenanigans 
    	
    	// If there are at least one 3 move, remove all 2 moves, 
    	if (caps.size() > 0) {
    		//valid.removeIf(n -> n.prio == -1);
    		
    		//for (Move capture : caps) {
    		//	valid.addAll(getCompoundMoves(board, capture));
    		//}
    	}

    	valid.addAll(caps);
    	
    	valid.sort(movePriorityComparator);
		return valid;
    }
    
    private int getSides(int cell) {
    	if (cell == 0) return 0;
		boolean single = cell == 1 || cell == 2 || cell == 4 || cell == 8;
    	boolean twin = cell == 3 || cell == 5 || cell == 9 || cell == 6 || cell == 10 || cell == 12;
		boolean triple = cell == 14 || cell == 13 || cell == 11 || cell == 7;
		boolean captured = cell == 15;
		
		return single ? 1 : (twin ? 2 : (triple ? 3 : (captured ? 4 : 0)));
    }
    
    private void printBoard(int[][] board) {
    	int width = board.length, height = board[0].length;
    	
    	for (int y = height - 1; y >= 0 ; y--) {
    		for (int x = 0 ; x < width; x++) {
    			System.out.print(board[x][y] + ", ");
    		}
    		System.out.println("; ");
    	}
    }
    
    /**
     * Comparator Function to order moves in descending order of priority
     */
	public final Comparator<Move> movePriorityComparator = new Comparator<Move>() {         
		@Override         
		public int compare(Move m1, Move m2) {             
			return (m2.prio < m1.prio ? -1 : (m2.prio == m1.prio ? 0 : 1));           
		}     
	}; 
    
	private int[] getScores(DotsAndBoxes game) {
		return new int[] { game.getScore(1), game.getScore(2) };
	}
	
	/**
	 * Prints a neat summary after each completed bot move.
	 * @param score
	 * @param playerId
	 * @param moveX
	 * @param moveY
	 * @param moveSide
	 * @param depth
	 * @param start
	 */
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
				//System.out.print(board[xx][yy] + ", ");
			}
			//System.out.println("; ");
		}
		
		return board;
	}
	
    @Override
    public String getName() { return "TipTacos's Dots and Boxes MCTS"; }
	
    class Triple {
    	Move move;
    	int score;
    	int depth;
    	
    	public Triple(int score, Move move) {
    		this(score, move, -1);
    	}
    	
    	public Triple(int score, Move move, int depth) {
    		this.score = score;
    		this.move = move;
    		this.depth = depth;
    	}
    }
    
    class Move {
    	
    	ArrayList<Move> moves = new ArrayList<Move>();
    	int x = -1, y = -1, orien = -1, prio = 0;
    	
    	public Move() {}
    	
    	public Move(Move move) {
    		moves.addAll(move.moves);
    		this.prio = 10;
    	}
    	
    	public Move (int x, int y, int orien) {
    		this.x = x;
    		this.y = y;
    		this.orien = orien;
    	}
    	
    	public Move (int x, int y, int orien, int prio) {
    		this(x, y, orien);
    		this.prio = prio;
    	}
    	
    	public void addMove(Move move) {
    		if (move.moves.size() == 0) {
    			moves.add(move);
    		} else {
    			moves.addAll(move.moves);
    		}
    	}
    	
    	public boolean isCompound() { return moves.size() > 0; }
    	
    	public String toString() { return "Move (" + x + ", " + y + ", " + orien + ")" ; }
    }
}
