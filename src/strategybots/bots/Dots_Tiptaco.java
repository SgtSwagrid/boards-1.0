package strategybots.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import strategybots.bots.C4_Tiptaco.Board;
import strategybots.bots.C4_Tiptaco.Minimax;
import strategybots.games.ConnectFour;
import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.util.Game.Player;
import strategybots.graphics.Colour;

public class Dots_Tiptaco implements Player<DotsAndBoxes>{

	public static final int THREADS = 4, DEPTH = 5, BREADTH = 10;
	public static final int TIME = 1000;
	public static final int VERT = 0, HORZ = 1;
	
	private ExecutorService ex;
	private State state;
	private int width, height;
	
	@Override 
	public void init(DotsAndBoxes game, int playerId) {
		width = game.getWidth();
		height = game.getHeight();
		
		ex = Executors.newFixedThreadPool(THREADS);
		
		state = new State(game, playerId);
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {
		// TODO Auto-generated method stub
		
		state = updateBoard(game, playerId);
		
		/*
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Move> moves = new ArrayList<Move>();
		moves = mm.validMoves(state, 3);
				
		if (moves.size() == 0)
		{
			moves = mm.validMoves(state, 0);
		}
		
		if (moves.size() == 0)
		{
			moves = mm.validMoves(state, 1);
		}*/
	
		Random rand = new Random();
		Orien o = rand.nextInt(2) == 0 ? Orien.VERT : Orien.HORZ;
		Move theMove = bestMove(game, playerId);
		
		System.out.println("Help my move was null");
		
		if (theMove == null) {
			int dx = 0, dy = 0;
			
			do
			{
				dx = rand.nextInt(width);
				dy = rand.nextInt(height);
			} while ((state.getNumSides()[dx][dy] == 4));
			
			System.out.println("Placing " + dx + " "  + dy);
			
			game.drawLine(o, dx, dy);
		} else {
			game.drawLine(theMove.o, theMove.x, theMove.y);
		}
	}
	
	private ArrayList<Move> possibleMoves(DotsAndBoxes game, State state, int playerId)
	{
		ArrayList<Move> moves = new ArrayList<Move>();
		Minimax mm = new Minimax(state, playerId, playerId, false);
		
		moves.addAll(mm.validMoves(state, 3));
		moves.addAll(mm.validMoves(state, 1));
		
		for (int kk = 0 ; kk < 2; kk++)
		{
			for (int xx = 0 ; xx < state.lines[kk].length; xx++)
			{
				for (int yy = 0; yy < state.lines[kk][xx].length; yy++)
				{
					Orien o = kk == VERT ? Orien.VERT : Orien.HORZ;
					
					if (state.lines[kk][xx][yy] == false)
					{
						moves.add(new Move(o, xx, yy));
					}
				}
			}
		}
		
		return moves;
	}
	
	private Move bestMove(DotsAndBoxes game, int playerId)
	{
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		List<Move> moves = new ArrayList<Move>();
		
		moves = possibleMoves(game, state, playerId).subList(0, BREADTH);
		
		//if (moves.size() == 0) System.out.println("Help moves size is 0");
		
		for (Move m : moves)
		{
			State successor = new State(state);
			int completedBoxes = successor.placeSide(m.o, m.x, m.y);
			
			futures.add(ex.submit(new Minimax(successor, playerId, DEPTH, completedBoxes != 0)));
		}
		
		int bestScore = Integer.MIN_VALUE;
		Move bestMove = null;
		
		for (int ii = 0 ; ii < futures.size(); ii++)
		{
			try {	
				System.out.println("Future:" + ii + " has value " + futures.get(ii).get());
				if (futures.get(ii).get() > bestScore)
				{
					bestScore = futures.get(ii).get();
					bestMove = moves.get(ii);
					System.out.println(bestMove.toString());
				}
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return bestMove;
	}
	
	private State updateBoard(DotsAndBoxes game, int playerId)
	{
		return new State(game, playerId);
	}
	
	@Override
	public String getName()
	{
		return "Adrian's Bot";
	}

	
	class State {
		
		private int currentPlayer;
		
		private boolean lines[][][];		
		private boolean complete[][];
		
		private DotsAndBoxes game;
		
		boolean terminal = false;
		
		private int numSides[][];
		private int playerScore[];
		
		public State(DotsAndBoxes game, int myId)
		{
			this.game = game;
			this.currentPlayer = myId;
			
			width = game.getWidth();
			height = game.getHeight();
			
			lines = new boolean[2][][];
			complete = new boolean[width][height];
			
			lines[VERT] = new boolean[width + 1][height];
			lines[HORZ] = new boolean[width][height + 1];
			
			numSides = new int[width][height];
			playerScore = new int[2];
			
			populateBoard(game);
		}

		public State(State state)
		{
			this.game = state.getGame();
			this.currentPlayer = state.getPlayer();
			
			width = state.getWidth();
			height = state.getHeight();
			
			this.lines = deepCopy(state.getLines());
			this.complete = deepCopy(state.getComplete());
			
			this.numSides = deepCopy(state.getNumSides());
			this.playerScore = new int[2];
			this.playerScore[0] = state.getPlayerScore()[0];
			this.playerScore[1] = state.getPlayerScore()[1];
		}

		// Load the game from the game object into some data structure of state
		private void populateBoard(DotsAndBoxes game)
		{
			for (int kk = 0 ; kk < 2; kk++)
			{
				for (int xx = 0 ; xx < lines[kk].length; xx++)
				{
					for (int yy = 0; yy < lines[kk][xx].length; yy++)
					{
						Orien o = kk == VERT ? Orien.VERT : Orien.HORZ;
						lines[kk][xx][yy] = game.hasLine(o, xx, yy);
						
						if (lines[kk][xx][yy])
						{
							int sc = captureSquares(o , xx, yy);
							//if (sc != 0) System.out.println(sc);
						}
					}
				}
			}
			
			// todo add completed boxes
		}
		
	    public int[][] getNumSides()
	    {
	    	return numSides;
	    }
	    
	    public int[] getPlayerScore()
	    {
	    	return playerScore;
	    }
	    
	    public int getPlayer()
	    {
	    	return currentPlayer;
	    }
	    
		public boolean[][][] getBoard()
		{
			return lines;
		}
		
		public int placeSide(Orien o, int x, int y)
		{
			// todo
			int kk = o == Orien.VERT ? 0 : 1;
			
			lines[kk][x][y] = true;
			
			int filledSquares = 0;
			
			for (int xx = 0; xx < width ; xx++)
			{
				for (int yy = 0  ; yy < height ; yy++)
				{
					if (numSides[xx][yy] == 4) filledSquares++;
				}
			}
			
			int caps = captureSquares(o, x, y);
			
			if (caps == 0)
			{
				currentPlayer = currentPlayer % 2 + 1;
			}
			
			if (filledSquares == width * height)
			{
				terminal = true;
				//System.out.println("Win");
			}
			
			return caps;
			//setPiece(x, y);
			//terminal = checkWin(x, y);
		}
		
	    private int captureSquares(Orien orien, int x, int y) {
	           
	        //Increment the number of sides for each adjacent square.
	        //Increment score for each square which reaches 4 sides.
	        
	        int score = 0;
	        
	        int xx = orien == Orien.VERT?x-1:x;
	        int yy = orien == Orien.HORZ?y-1:y;
	        
	        //Check the square on the negative side of the line.
	        if(xx>=0 && yy>=0 && ++numSides[xx][yy] == 4) {
	            score++;
	        }
	        
	        //Check the square on the positive side of the line.
	        if(x<width && y<height && ++numSides[x][y] == 4) {
	            score++;
	        }
	        
	        playerScore[currentPlayer-1] += score;
	        return score;
	    }
		
		public boolean[][][] getLines()
		{
			return lines;
		}
		
		public boolean[][] getComplete()
		{
			return complete;
		}
		
		public boolean isTerminal()
		{
			return terminal;
		}
		
		public DotsAndBoxes getGame()
		{
			return game;
		}
		
		private boolean[][][] deepCopy(boolean[][][] A)
		{
			boolean copy[][][] = new boolean[A.length][][];
			
			for (int ii = 0 ; ii < A.length ; ii++)
			{
				copy[ii] = deepCopy(A[ii]);
			}
			
			return copy;
		}
		
		private boolean[][] deepCopy(boolean[][] A)
		{
			boolean[][] copy = new boolean[A.length][A[0].length];
			
			for (int ii = 0 ; ii < A.length ; ii++)
			{
				for (int jj = 0; jj < A[0].length; jj++)
				{
					copy[ii][jj] = A[ii][jj];
				}
			}
			
			return copy;
		}
		
		private int[][] deepCopy(int[][] A)
		{
			int[][] copy = new int[A.length][A[0].length];
			
			for (int ii = 0 ; ii < A.length ; ii++)
			{
				for (int jj = 0; jj < A[0].length; jj++)
				{
					copy[ii][jj] = A[ii][jj];
				}
			}
			
			return copy;
		}
		
		public int getWidth() { return width; }
		public int getHeight() { return height; }
		
	}
	
	class Minimax implements Callable<Integer> {

		private State st;
		private int myId;
		private int depth;
		boolean max = false;
		
		public Minimax(State st, int myId, int depth, boolean maximize)
		{
			this.st = st;
			this.myId = myId;
			this.depth = depth;
			max = maximize;
		}
		
		private int evalWinner(State state)
		{
			if (state.isTerminal())
			{
				if (state.getPlayer() == myId) return state.getPlayerScore()[myId - 1];
				else return state.getPlayerScore()[myId == 1 ? 0 : 1];
			}
			else
			{
				if (state.getPlayer() == myId) return state.getPlayerScore()[myId - 1];
				else return state.getPlayerScore()[myId == 1 ? 0 : 1];
				
				//return 0;
			}
		}
		
		private int minimax(State state, int depth, boolean maximizingPlayer) {
			if ( depth == 0 || state.isTerminal())
			{
				int score = evalWinner(state);
				//System.out.println(state.isTerminal() + " " + score);
				
				return score;
			}
			
			if (maximizingPlayer)
			{
				int value = Integer.MIN_VALUE;
				
				 List<Move> validMoves = possibleMoves(state.getGame(), state, state.currentPlayer).subList(0, BREADTH);
				
				 for (Move move : validMoves)
				 {
					 State successor = new State(state);
					 int completedBoxes = successor.placeSide(move.o, move.x, move.y);
					// successor.placePiece(move, getStackSize(state, move));
					 if (completedBoxes == 0) //swap
						 value = Math.max(value,  minimax(successor, depth - 1, false));
					 else
						 value = Math.max(value,  minimax(successor, depth - 1, true));

				 }
				 
				 return value;
			}
			else
			{
				int value = Integer.MAX_VALUE;
				
				 List<Move> validMoves = possibleMoves(state.getGame(), state, state.currentPlayer).subList(0, BREADTH);
				
				 for (Move move : validMoves)
				 {
					 State successor = new State(state);
					 int completedBoxes = successor.placeSide(move.o, move.x, move.y);
					// successor.placePiece(move, getStackSize(state, move));
					 if (completedBoxes == 0) //swap
						 value = Math.min(value,  minimax(successor, depth - 1, true));
					 else
						 value = Math.min(value,  minimax(successor, depth - 1, false));
				 }
				 
				 return value;
			}
		}
		
		private Move emptyMove(State state, int xx, int yy)
		{
			if (!state.getBoard()[0][xx][yy]) return new Move(Orien.VERT, xx, yy);
			else if (!state.getBoard()[1][xx][yy]) return new Move(Orien.HORZ, xx, yy);
			else if (!state.getBoard()[0][xx+1][yy]) return new Move(Orien.VERT, xx + 1, yy);
			else if (!state.getBoard()[1][xx][yy+1]) return new Move(Orien.HORZ, xx, yy + 1);
			
			return null;
		}
		
		public ArrayList<Move> validMoves(State state, int numSides) {
			ArrayList<Move> moves = new ArrayList<Move>();
			
			for (int xx = 0; xx < width ;xx++)
			{
				for (int yy = 0; yy < height ; yy++)
				{
					if (state.getNumSides()[xx][yy] == numSides)
					{
						moves.add(emptyMove(state, xx, yy));
					}
				}
			}
			
			return moves;
		}
		
		@Override
		public Integer call() throws Exception {
			return minimax(st, depth, max);
		}
		
	}
	
	class Move {
		Orien o;
		int x, y;
		
		public Move(Orien o, int x, int y)
		{
			this.o = o;
			this.x = x;
			this.y = y;
		}
	}
	
	class MCTS {
		
		DotsAndBoxes game;
		Node root;
		State state;
		int myId;
		
		public MCTS(DotsAndBoxes game, State state, int playerId)
		{
			this.game = game;
			this.state = state;
			myId = playerId;
			
			root = new Node(state, null);
		}
		
		public Node mcts(Node root)
		{
			long startTime = System.currentTimeMillis();
			
			Node leaf;
			
			while (System.currentTimeMillis() - startTime > TIME)
			{
				// do mcts
				//leaf = rollout(root);
				
				
			}
			
			
			return root;
		}
		
		public void traverse()
		{
			
		}
		
		public void rollout() {
			
		}
		
		public void backprop() {
			
		}
		
		public Node bestChild()
		{
			return null;
		}
		
	}
	
	class Node {
		
		public State state;
		public int visits, score;
		
		public Node parent;
		public ArrayList<Node> children;
		
		public Node(State state, Node parent)
		{
			this.parent = parent;
			children = new ArrayList<Node>();
			
			this.state = state;
			this.visits = 0;
			this.score = 0;
		}

		public ArrayList<Node> getUnvisited()
		{
			ArrayList<Node> unVisited = new ArrayList<Node>();
			
			for (Node n : children)
			{
				if (n.visits == 0) unVisited.add(n);
			}
			
			return unVisited;
		}
		
		public boolean fullyExpanded()
		{
			if (getUnvisited().size() == 0 || state.isTerminal())
			{
				return true;
			}
			
			return false;
		}
		
	}
	
	
}


