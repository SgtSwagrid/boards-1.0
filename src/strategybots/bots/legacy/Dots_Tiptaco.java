package strategybots.bots.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import strategybots.bots.legacy.C4_Tiptaco.Board;
import strategybots.bots.legacy.C4_Tiptaco.Minimax;
import strategybots.games.ConnectFour;
import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.base.Game.Player;
import strategybots.games.graphics.Colour;

public class Dots_Tiptaco implements Player<DotsAndBoxes>{

	// Threads is number of threads in the pool, Depth is minimax depth, breadth is minimax depth
	public static final int THREADS = 4, DEPTH = 5, BREADTH = 10;
	
	public static final int VERT = 0, HORZ = 1;
	
	private ExecutorService ex;
	private State state;
	private int width, height;
	
	@Override 
	public void init(DotsAndBoxes game, int playerId) {
		width = game.getWidth();
		height = game.getHeight();
		
		// create the threadpool for minimax to use later
		ex = Executors.newFixedThreadPool(THREADS);
		
		state = new State(game, playerId);
	}
	
	@Override
	public void takeTurn(DotsAndBoxes game, int playerId) {
	
		// Do the minimax in the threadpool, saving the best move if there was one.
		//Move theMove = bestMove(game, playerId);

		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Take Turn Rewrite as pseudocoded from my workbook
		
		// 0) Update the board state
		state = updateBoard(game, playerId);
		Random rand = new Random();
		Orien o = rand.nextInt(2) == 0 ? Orien.VERT : Orien.HORZ;
		
		// 1) Loop until all the 3's are taken
		ArrayList<Move> move3 = state.validMoves(state, 3);
		while (move3.size() > 0) {
			
			Move m = move3.get(0);
			game.drawLine(m.o, m.x, m.y);
			state.placeSide(m.o, m.x, m.y);
			move3 = state.validMoves(state, 3);
		}
		
		// 2) Try to place where a 3 wont be made
		State successor = new State(state);
		int tries = 0; 
		int dx = 0, dy = 0;
		do {
			
			do {
				dx = rand.nextInt(width);
				dy = rand.nextInt(height);
			} while ((state.getNumSides()[dx][dy] == 4));
			
			successor = new State(state);
			successor.placeSide(o, dx, dy);
			tries++;
		} while (successor.validMoves(successor, 3).size() != 0 && tries != 100);
		
		if (successor.validMoves(successor, 3).size() == 0)
		{
			game.drawLine(o, dx, dy);
			return;
		}
		
		// 3) Group Chains 
		
		state = updateBoard(game, playerId);
		
		Mapper mapper = new Mapper(state);
		mapper.map();
		Group largest = mapper.getLargestGroup();
		
		// 4) PLace inside the smallest group
		
		Group smallest = mapper.getSmallestGroup();
	
		if (smallest != null) {
			System.out.println("Min Group size " + smallest.group.size());
			for (Point p : mapper.getSmallestGroup().group) System.out.print("(" + p.x + "," + p.y +") ");
		}
		
		Point picked = smallest.group.get(0);
		Move pick = state.emptyMove(state, picked.x, picked.y);
		game.drawLine(pick.o, pick.x, pick.y);
		
		
		
		
		
		
	}
	
	/**
	 * Makes an arraylist of moves to be considered for minimax, All moves are considered for now
	 * @param game
	 * @param state
	 * @param playerId
	 * @return ArrayList of possible Moves
	 */
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
	
	/**
	 * Selects the best move out of all possible moves and their resulting minimaxs
	 * @param game
	 * @param playerId
	 * @return
	 */
	private Move bestMove(DotsAndBoxes game, int playerId)
	{
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		List<Move> moves = new ArrayList<Move>();
		
		moves = possibleMoves(game, state, playerId);
		moves = moves.subList(0, Math.min(BREADTH,  moves.size()));
				
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

	/**
	 * The State class will hold all information about a current state in the game 
	 * @author Adrian Shedley
	 */
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
			
			playerScore[0] = 0;
			playerScore[1] = 0;
			// todo add completed boxes
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
	
		public Move emptyMove(State state, int xx, int yy)
		{
			if (!state.getBoard()[0][xx][yy]) return new Move(Orien.VERT, xx, yy);
			else if (!state.getBoard()[1][xx][yy]) return new Move(Orien.HORZ, xx, yy);
			else if (!state.getBoard()[0][xx+1][yy]) return new Move(Orien.VERT, xx + 1, yy);
			else if (!state.getBoard()[1][xx][yy+1]) return new Move(Orien.HORZ, xx, yy + 1);
			
			return null;
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
	
	class Mapper
	{
		State state;
		
		ArrayList<Group> groups;
		boolean[][] visited;

		public Mapper(State state)
		{
			this.state = state;	
			groups = new ArrayList<Group>();
			visited = new boolean[state.getWidth()][state.getHeight()];
		}
		
		public ArrayList<Group> map()
		{			
			for (int xx = 0 ; xx < state.getWidth() ; xx++)
			{
				for (int yy = 0;  yy < state.getHeight() ; yy++)
				{
					if (!visited[xx][yy]) groups.add(map(xx,yy));
				}
			}
			
			return groups;
		}
		
		public Group map(int x , int y)
		{
			Group gp = new Group(state, this);
			gp.map(new Point(x, y));
			return gp;
		}
		
		public ArrayList<Group> getGroups()
		{
			return groups;
			
		}
		
		public Group getLargestGroup()
		{
			int maxSize = 0;
			Group max = null;
			
			for (Group gg: groups)
			{
				if (gg.group.size() > maxSize)
				{
					maxSize = gg.group.size();
					max = gg;
				}
			}
			
			return max;
		}
		
		public Group getSmallestGroup()
		{
			int minSize = Integer.MAX_VALUE;
			Group min = null;
			
			for (Group gg: groups)
			{
				if (gg.group.size() < minSize && gg.group.size() != 0)
				{
					minSize = gg.group.size();
					min = gg;
				}
			}
			
			return min;
		}
	}
	
	class Group
	{
		State state;
		Mapper parent;
		
		ArrayList<Point> group;
		int[][] adj;
		
		public Group(State state, Mapper parent) {
			this.state = state;
			this.parent = parent;
			
			group = new ArrayList<Point>();
			adj = new int[state.getWidth()][state.getHeight()];
		}
		
		public boolean map(Point p) {
						
			if (!parent.visited[p.x][p.y] && (state.getNumSides()[p.x][p.y] == 2 || (state.getNumSides()[p.x][p.y] + adj[p.x][p.y] == 3)) )
			{
				group.add(p);
				adj[p.x][p.y]--; 
				
				parent.visited[p.x][p.y] = true; 
				
				if (p.x - 1 >= 0) {
					Point p2 = new Point(p.x - 1, p.y);
					if (openAdjacent(p, p2)) {
						adj[p2.x][p2.y]++; 
						map(p2);
					}
					
				}
				
				if (p.x + 1 < state.getWidth())  {
					Point p2 = new Point(p.x + 1, p.y);
					if (openAdjacent(p, p2)) {
						adj[p2.x][p2.y]++; 
						map(p2);
					}
				}
				
				if (p.y - 1 >= 0)  {
					Point p2 = new Point(p.x, p.y - 1);
					if (openAdjacent(p, p2)) {
						adj[p2.x][p2.y]++; 
						map(p2);
					}
				}
				
				if (p.y + 1 < state.getHeight())  {
					Point p2 = new Point(p.x, p.y + 1);
					if (openAdjacent(p, p2))  {
						adj[p2.x][p2.y]++; 
						map(p2);
					}
				}
			}
			
			return group.size() != 0;
			
		}
		
		// Method for if the two points are adjacent and open
		private boolean openAdjacent(Point p1, Point p2)
		{
			boolean openAdj = false;
			
			if (p1.x == p2.x && Math.abs(p1.y - p2.y) == 1) {
				// X same, so dealing with the horizontals
				int dy = Math.max(p1.y, p2.y);
				
				openAdj = !state.getBoard()[1][p1.x][dy];
				
			} else if (p1.y == p2.y && Math.abs(p1.x - p2.x) == 1){
				// Y same so dealing with verticals
				int dx = Math.max(p1.x, p2.x);
				
				openAdj = !state.getBoard()[0][dx][p1.y];
			} // else not adj because not touching
			
			return openAdj;
		}
		
	}
	
	class Point { int x, y; public Point(int x, int y) { this.x = x; this.y = y; } }
	
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
			if (true)//state.isTerminal())
			{
				int score = 0;
				
				if (state.getPlayer() == myId) {
					score += state.getPlayerScore()[myId - 1];
					//score -= state.getPlayerScore()[myId == 1 ? 0 : 1];
				} else {
					score -= state.getPlayerScore()[myId - 1];
					//score += state.getPlayerScore()[myId == 1 ? 0 : 1];
				}
				
				return score;
			}
			return 0;
		}
		
		private int minimax(State state, int depth, boolean maximizingPlayer) {
			if ( depth == 0 || state.isTerminal()) {
				int score = evalWinner(state);
				return score;
			}
			
			if (maximizingPlayer) {
				int value = Integer.MIN_VALUE;
				
				 List<Move> validMoves = possibleMoves(state.getGame(), state, state.currentPlayer);
				 validMoves = validMoves.subList(0, Math.min(BREADTH,  validMoves.size()));
				
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
			} else {
				int value = Integer.MAX_VALUE;
				
				 List<Move> validMoves = possibleMoves(state.getGame(), state, state.currentPlayer);
				 validMoves = validMoves.subList(0, Math.min(BREADTH,  validMoves.size()));
				 
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
	
	/**
	 * The move data type. Holds Orientation o and Position x,y
	 * @author Adrian
	 *
	 */
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
}