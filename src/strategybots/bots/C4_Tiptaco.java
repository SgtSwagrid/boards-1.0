package strategybots.bots;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import strategybots.games.ConnectFour;
import strategybots.games.util.Game.Player;

public class C4_Tiptaco implements Player<ConnectFour> {

	/**
	 * A MonteCarlo Search Tree implementation of the Connect Four game type.
	 * 
	 * @author Adrian Shedley
	 */
	
	public static final int NUM_THREADS = 1;
	public static final int TREE_DEPTH= 5;
	
	private ConnectFour g = null;
	private int myId = 0;
	
	private ExecutorService ex = null;
	private Board board;
	
	private int width, height, target;
	
	@Override
	public void init(ConnectFour game, int playerId) {
		g = game;
		myId = playerId;
		
		width = game.getWidth();
		height = game.getHeight();
		target = 4;
		
		board = new Board(game, playerId);
		
		ex = Executors.newFixedThreadPool(NUM_THREADS);
	}
	
	@Override
	public void takeTurn(ConnectFour game, int playerId) {
		// TODO Auto-generated method stub
		updateBoard(game, playerId);
		
		//ex = Executors.newFixedThreadPool(NUM_THREADS);
		
		System.out.println("Que");
		int best = bestPosition(game);
		
		//ex.shutdownNow();
		
		/*try {
			ex.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		game.placeStone(best);
		
		System.out.println("Placed");
		
	}
	
	private void updateBoard(ConnectFour game, int playerId) {
		board = new Board(game, playerId);
	}
	
	
	private int bestPosition(ConnectFour game)
	{
		//List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		List<Integer> moves = new ArrayList<Integer>();
		List<Integer> futures = new ArrayList<Integer>();
		
		for (int ii = 0 ; ii < width ; ii++) {
			
			if (game.validatePlacement(ii))
			{
				Board successor = new Board(board);
				successor.placePiece(ii, game.getStackSize(ii));
				
				//futures.add(ex.submit(new Minimax(successor, myId, TREE_DEPTH)));
				
				Minimax mm = new Minimax(successor, myId, TREE_DEPTH);
				
				try {
					futures.add(mm.call());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				moves.add(ii);
			}

		}
		
		int bestScore = Integer.MIN_VALUE;
		int bestSlot = 0;
		
		for (int ii = 0 ; ii < futures.size(); ii++)
		{
			//try {
				if (futures.get(ii)/*.get()*/ > bestScore)
				{
					bestScore = futures.get(ii)/*.get()*/;
					bestSlot = moves.get(ii);
				}
			/*} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		return bestSlot;
	}
	
	class Minimax implements Callable<Integer> {

		private Board b;
		private int myId;
		private int depth;
		
		public Minimax(Board b, int myId, int depth)
		{
			this.b = b;
			this.myId = myId;
			this.depth = depth;
		}
		
		private int evalWinner(Board board)
		{
			if (board.isTerminal())
			{
				if (board.getPlayer() == myId) return 1;
				else return -1;
			}
			else
			{
				return 0;
			}
		}
		
		private int minimax(Board board, int depth, boolean maximizingPlayer) {
			if ( depth == 0 || board.terminal)
			{
				return evalWinner(board) * (depth + 1);
			}
			
			if (maximizingPlayer)
			{
				int value = Integer.MIN_VALUE;
				
				 List<Integer> validMoves = validMoves(board);
				
				 for (Integer move : validMoves)
				 {
					 Board successor = new Board(board);
					 successor.placePiece(move, getStackSize(board, move));
					 
					 value = Math.max(value,  minimax(successor, depth - 1, false));
				 }
				 
				 return value;
			}
			else
			{
				int value = Integer.MAX_VALUE;
				
				 List<Integer> validMoves = validMoves(board);
				
				 for (Integer move : validMoves)
				 {
					 Board successor = new Board(board);
					 successor.placePiece(move, getStackSize(board, move));
					 
					 value = Math.min(value, minimax(successor, depth - 1, true));
				 }
				 
				 return value;
			}
		}
		
		private List<Integer> validMoves(Board b) {
			List<Integer> moves = new ArrayList<Integer>();
			
			for (int ii = 0 ; ii < width ; ii++)
			{
				if (b.getBoard()[ii][height - 1] == 0)
				{
					moves.add(ii);
				}
			}
			
			return moves;
		}
		
		private int getStackSize(Board board, int x) {
	        
	        for(int y = 0; y < height; y++) {
	            
	            if(board.getBoard()[x][y] == 0) return y;
	        }
	        return height;
	    }
		
		@Override
		public Integer call() throws Exception {
			return minimax(b, depth, false);
		}
		
	}
	
	
	public class Board {

		private int currentPlayer = 0;
		private int board[][];
		
		private ConnectFour game;
		
		boolean terminal = false;
		
		public Board(ConnectFour game, int myId)
		{
			this.game = game;
			this.currentPlayer = myId;
			
			width = game.getWidth();
			height = game.getHeight();
			
			board = new int[width][height];
			
			populateBoard(game);
		}

		public Board(Board board)
		{
			this.game = board.getGame();
			
			width = board.getWidth();
			height = board.getHeight();
			
			this.currentPlayer = board.currentPlayer;
			this.board = deepCopy(board.getBoard());
		}
		
		// Load the game from the game object into some data structure of state
		private void populateBoard(ConnectFour game)
		{
			for (int xx = 0 ; xx < width; xx++)
			{
				for (int yy = 0; yy < height; yy++)
				{
					board[xx][yy] = (int)game.getStone(xx, yy);
				}
			}
		}

		public boolean checkWin(int x, int y)
		{
			boolean win = false;
			
			for (int nn = -1 ; nn < 2 ; nn++)
			{
				for (int mm = 1 ; mm < 2 ; mm++)
				{
					if (!(nn == 0 && mm == 0) && !win) win = isStreak(x, y, nn, mm, target);
				}
			}
			
			return win;
		}
		
		public boolean isStreak(int x, int y, int inX, int inY, int length)
		{
			boolean streak = true;
			
			for (int ii = 1 ; ii < length ; ii++)
			{
				int nX = x + ii*inX;
				int nY = y + inY*ii;
				
				if ( nX >= 0 && nY >= 0 && nX < width && nY < height)
				{
					if (board[nX][nY] != currentPlayer)
					{
						streak = false;
					}
				}
				else
				{
					streak = false;
				}
			}
			
			return streak;
		}
		
		public int getPlayer() {
			return currentPlayer;
		}
		
		public void swapPlayer() {
			currentPlayer = currentPlayer == 1 ? 2 : 1;
		}
		
		public int[][] getBoard()
		{
			return board;
		}
		
		public void placePiece(int x, int y)
		{
			setPiece(x, y);
			terminal = checkWin(x, y);
			
			if (!terminal) swapPlayer();
		}
		
		public boolean isTerminal()
		{
			return terminal;
		}
		
		public void setPiece(int x, int y)
		{
			board[x][y] = currentPlayer;
		}
		
		public ConnectFour getGame()
		{
			return game;
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
	
}
