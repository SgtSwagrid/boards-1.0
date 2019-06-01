package strategybots.bots.legacy;

import java.util.ArrayList;

import strategybots.games.legacy.ConnectFour;
import strategybots.games.legacy.MNKGame;
import strategybots.games.legacy.util.Player;

public class AdrianMNKBot implements Player<MNKGame> {

	private MNKGame game;
	private int board[][];
	public int prio[][];
	
	private int width, height, target;
	
	public static final int EMPTY = 0;
	public static final int ME = 1;
	public static final int OP = 2;
	
	public static final int VER = 0, HOR = 1, D1 = 2, D2 = 3;
	
	// Strength factors
	// DEFAULT: depth = 8, breadth = 4 or 5
	public static int DEPTH_F = 8, BREADTH_F = 5;
	public static int DEPTH = 0, BREADTH = 0;

	
	public AdrianMNKBot() {
		// TODO Auto-generated constructor stub
	}
	
	public AdrianMNKBot(int depth, int breadth) {
		// TODO Auto-generated constructor stub
		DEPTH_F = depth;
		BREADTH_F = breadth;
	}
	
	@Override
	public void takeTurn(MNKGame game) {
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		reset();
		scanBoard();
		
		scanRuns(board, prio, 2, target);
		scanOpens(board, prio, target);
		
		//game.prio = prio;
		
		//System.out.println(sumBoard());
		
		System.out.println("is empty "+ isEmpty() + " sum board is " + sumBoard());
		
		if (isEmpty())
		{
			playFirst();
		}
		else if (sumBoard() == 2) // turn 2
		{
			System.out.println("Me, the bot, has Second turn");
			scanSecond(board, prio);
			play();
		}
		else
		{			
			updatePrefs();
			safePlace(board, findBestMove(board, prio, ME));
			//play();
		}
		
		printPrio();
		
	}
	
	private void updatePrefs()
	{
		int gameScore = sumBoard();
		
		if (gameScore < 10)
		{
			DEPTH = DEPTH_F - 2;
			BREADTH = BREADTH_F + 2;
		}
		else
		{
			DEPTH = DEPTH_F;
			BREADTH = BREADTH_F;
		}
	}
	
	private Pos findBestMove(int[][] board, int[][] prio, int owner)
	{
		ArrayList<Pos> moves = rankMoves(prio, BREADTH);
		
		ArrayList<Outcome> outcomes = new ArrayList<>();
		
		Outcome bestOutcome = null;
		
		int counter = 0;
		
		for (Pos move : moves)
		{
			System.out.println("Counter: " + (counter++));
			
			int nBoard[][] = deepCopy(board);
			int nPrio[][] = deepCopy(prio);
			
			// do move
			boolean placed = simulatePlace(nBoard, move, owner);
			Outcome winner;
			
			if (placed)
			{
				winner = findBestMoveRec(nBoard, nPrio, DEPTH, owner);
			
				Outcome o = new Outcome(move);
				o.state = winner.state;
				o.levels = winner.levels;
				
				outcomes.add(o);
			}

		}
		
		bestOutcome = findHighestOutcome(outcomes, ME);
		
		Outcome opBest = findHighestOutcome(outcomes, OP);
		if (opBest != null)
			System.out.println("Best opponent outcome at " + opBest.move.x + "," + opBest.move.y + " with state " + opBest.state + " and level " + opBest.levels);
		
		if (bestOutcome == null)
		{
			System.out.println("best outcome is null");
			
			resetArray(prio);
			
			scanRuns(board, prio, 2, target);
			scanOpens(board, prio, target);
			
			Pos max = findBestMove(prio);
			//safePlace(board, max.x, max.y);
			
			bestOutcome = new Outcome(max);
			
		}
		else
		{
			System.out.println("Best outcome at " + bestOutcome.move.x + "," + bestOutcome.move.y + " with state " + bestOutcome.state + " and level " + bestOutcome.levels);
		}
		
		return bestOutcome.move;
	}
	
	private Outcome findHighestOutcome(ArrayList<Outcome> outcomes, int owner)
	{
		Outcome best = null;
		int maxLevels = DEPTH + 1;
		
		for (Outcome o : outcomes)
		{
			if (o.state == owner)
			{
				if (o.levels < maxLevels)
				{
					best = o;
					maxLevels = o.levels;
				}
			}
		}
		
		return best;
	}
	
	private Outcome findBestMoveRec(int[][] board, int[][] prio, int level, int player)
	{
		// get the opposing player
		int nPlayer = getNextPlayer(player);
		
		Outcome outcome = new Outcome(0);
		
		int winState = getWinner(board, prio);
		
		if (winState == ME || winState == OP)
		{	// end because someone has won, the level above will decide what to do
			// do nothing 
			outcome.state = winState;
			outcome.levels = 0;
		}
		else
		{ 	// continue
			if (level > 0)
			{
				// clear the incoming array
				resetArray(prio);
			
				// repopulate the prio array
				scanOpens(board, prio, target);
				scanRuns(board, prio, 2, target);
			
				// list and the execute the moves
				ArrayList<Pos> moves = rankMoves(prio, BREADTH);
				int counter = 0;
				
				ArrayList<Outcome> outcomes = new ArrayList<>();
				
				for (Pos move : moves)
				{
					int nBoard[][] = deepCopy(board);

					//System.out.println(indent(DEPTH - level) + " Move " + counter++);
					// do move
					boolean placed = simulatePlace(nBoard, move, nPlayer);
					Outcome winner;
					
					if (placed)
					{	
						winner = findBestMoveRec(nBoard, prio, level - 1, nPlayer);
						
						if (winner.state == ME || winner.state == OP) 
						{
							if (winner.state == ME && winner.levels == 1)
							{
								//System.out.println(indent(DEPTH - level) + "DETECTED: I win at level " + level);
							}
							else if ( winner.levels == 1)
							{
								//System.out.println(indent(DEPTH - level) + "DETECTED: OP wins at level " + level);
							}
						}
						
						/*if (winner.state != 0)
						{
							winner.levels++;
						}*/
						
						outcomes.add(winner);
					} 
				}
				
				// return closest win
				int minLevels = DEPTH + 1;
				
				for (Outcome o : outcomes)
				{
					if (o.state != 0)
					{
						if (o.levels < minLevels)
						{
							outcome = o;
							minLevels = o.levels;
						}
					}
				}
				
			}
		}

		outcome.levels++;
		
		return outcome;
	}
	
	private String indent(int n)
	{
		String str = "";	
		for (int ii = 0 ; ii < n ; ii++)
		{
			str += "  ";
		}
		
		return str;
	}
	
	private int getNextPlayer(int player)
	{
		int nPlayer = EMPTY;
		
		if (player == ME) nPlayer = OP;
		if (player == OP) nPlayer = ME;
		
		return nPlayer;
	}
	
	private int[][] deepCopy(int[][] A)
	{
		int[][] newArray = new int[height][width];
		
		for (int ii = 0 ; ii < height; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				newArray[ii][jj] = A[ii][jj];
			}
		}
		
		return newArray;
	}
	
	private void resetArray(int[][] A)
	{
		for (int ii = 0 ; ii < height; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				A[ii][jj] = 0;
			}
		}
	}
	
	private boolean simulatePlace(int[][] board, Pos pos, int owner)
	{
		boolean placed = false;
		
		if (board[pos.y][pos.x] == EMPTY)
		{
			board[pos.y][pos.x] = owner;
			placed = true;
		}
		else
		{
			//System.out.println("Simuplace failed At " + pos.x + " " + pos.y);
		}
		
		return placed;
	}
	
	private int getWinner(int[][] board, int[][] prio)
	{
		int winner = 0;
		
		if (hasWon(board, prio, ME)) winner = ME;
		if (hasWon(board, prio, OP)) winner = OP;
		
		return winner;
	}
	
	private ArrayList<Pos> rankMoves(int[][] prio, int n)
	{
		ArrayList<Pos> moveOrder = new ArrayList<>();
		
		//System.out.println("best move finder with n as " + n);
		
		Pos bestMove = null;
		
		do
		{
			bestMove = findBestMove(prio);
			
			//System.out.println("looped one");
			
			if (bestMove != null)
			{
				//System.out.println(" > move acceptyed ");
				
				moveOrder.add(bestMove);
				// remove the value from the matrix
				prio[bestMove.y][bestMove.x] = 0;
			}
			
		} while (moveOrder.size() != n && bestMove != null && sumArray(prio) != 0);
		
		return moveOrder;		
	}
	
	private Pos findBestMove(int[][] A)
	{
		int value = -1;
		Pos max = null;
		
		for (int ii = 0; ii < A.length; ii++)
		{
			for (int jj = 0 ; jj < A[0].length; jj++)
			{
				if (A[ii][jj] > value)
				{
					value = A[ii][jj];
					max = new Pos(jj, ii);
				}
			}
		}
		
		return max;
	}
	
	private boolean hasWon(int[][] board, int[][] prio, int owner)
	{
		return (findRuns(board, prio, target, owner) >= 1);
	}
	
	private void scanSecond(int[][] board, int[][] prio)
	{
		findRuns(board, prio, 1, OP);
	}
	
	private void scanRuns(int[][] board, int[][] prio, int min, int n)
	{
		for (int ii = 1 ; ii < n; ii++)
		{
			findRuns(board, prio, ii, ME);
			findRuns(board, prio, ii, OP);
		}
	}
	
	private void scanOpens(int[][] board, int[][] prio, int n)
	{
		for (int ii = 3 ; ii <= n; ii++)
		{
			findOpens(board, prio, ii, ME);
			findOpens(board, prio, ii, OP);
		}
	}
	
	private void findOpens(int[][] board, int[][] prio, int n, int owner)
	{
		//ArrayList<ArrayList<Pos>> opens = new ArrayList<>();
		
		for (int ii = 0 ; ii < height; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				for (int dd = 0 ; dd < 4; dd++)
				{
					if (isOpen(board, jj, ii, n, dd, owner))
					{
						//opens.add(markOpen(jj, ii, n, dd, n * 10 + 1, owner));
						
						//markRunEnd(jj, ii, n, dd, (n-2) * 10);
						markOpen(board, prio, jj, ii, n, dd, n * 10 + 1, owner);
					}
				}	
			}
		}
		
		//markOpens(opens);
	}
	
	private boolean isOpen(int[][] board, int x, int y, int n, int dir, int owner)
	{
		int count = 0;
		
		for (int kk = 0 ; kk < n; kk++)
		{
			switch (dir)
			{
			case VER: // y + k
				
				if (safeGet(board, x, y + kk) == owner)
				{
					count++;
				}
				
				break;
			case HOR: // x + k
				
				if (safeGet(board, x + kk, y) == owner)
				{
					count++;
				}
				
				break;
			case D1: // x + k, y + k
				
				if (safeGet(board, x + kk, y + kk) == owner)
				{
					count++;
				}
				
				break;
			case D2: // x - k, y + k
				
				if (safeGet(board, x - kk, y + kk) == owner)
				{
					count++;
				}
				
				break;
			}
		}
		
		return (count == (n - 1));
	}
	
	private void markOpen(int[][] board, int[][] prio, int x, int y, int n, int dir, int num, int owner)
	{
		
		
		for (int kk = 0; kk < n; kk++)
		{
			
			switch (dir)
			{
			case VER: // y + k
				
				safeIncEmpty(board, prio, x, y + kk, num);
				
				break;
			case HOR: // x + k
				
				safeIncEmpty(board, prio, x + kk , y, num);
				
				break;
			case D1: // x + k, y + k
				
				safeIncEmpty(board, prio, x + kk, y + kk, num);
				
				break;
			case D2: // x - k, y + k
	
				safeIncEmpty(board, prio, x - kk, y + kk, num);
	
				break;
			}
		}
	}
	
	private void play()
	{
		Pos max = findMaxSlot();
		safePlace(board, max.x, max.y);
	}
	
	private void safePlace(int[][] board, Pos pos)
	{
		safePlace(board, pos.x, pos.y);
	}
	
	private void safePlace(int[][] board, int x, int y)
	{
		boolean placed = false;
		
		while (!placed)
		{
			if (board[y][x] == EMPTY)
			{
				game.placePiece(x, y);
				placed = true;
			}
			else
			{
				prio[y][x] = 0;
				Pos max = findBestMove(board);
				x = max.x;
				y = max.y;
			}
			
			if (sumArray(prio) == 0 && !placed)
			{
				while (!placed)
				{
					x = (int)(Math.random() * width);
					y = (int)(Math.random() * height);
					
					if (board[y][x] == EMPTY)
					{
						game.placePiece(x, y);
						placed = true;
					}
				}
			}
		}
	}
	
	private void playFirst()
	{
		if (board[width/2][height/2] == EMPTY)
		{
			game.placePiece(width/2, height/2);
		}
		else
		{
			game.placePiece(width/2+1, height/2);
		}
	}
	
	private int findRuns(int[][] board, int[][] prio, int n, int owner)
	{
		int runsSum = 0;
		
		for (int ii = 0 ; ii < height; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				for (int dd = 0 ; dd < 4; dd++)
				{
					if (isRun(board, jj, ii, n, dd, owner))
					{
						markRunEnd(board, prio, jj, ii, n, dd, n * 10);
						runsSum++;
					}
				}
			}
		}
		
		return runsSum;
	}
	
	private void printPrio()
	{
		String str = "";
		
		for (int ii = height - 1 ; ii >= 0; ii--)
		{
			str += "[ ";
			for (int jj = 0 ; jj < width; jj++)
			{
				str += String.format("%4d", prio[ii][jj]);
			}
			str += " ]\n";
		}
		
		System.out.println(str);
	}
	
	private boolean isRun(int[][] board, int x, int y, int n, int dir, int owner)
	{
		boolean run = true;
		
		for (int kk = 0 ; kk < n; kk++)
		{
			switch (dir)
			{
			case VER: // y + k
				
				if (safeGet(board, x, y + kk) != owner)
				{
					run = false;
				}
				
				break;
			case HOR: // x + k
				
				if (safeGet(board, x + kk, y) != owner)
				{
					run = false;
				}
				
				break;
			case D1: // x + k, y + k
				
				if (safeGet(board, x + kk, y + kk) != owner)
				{
					run = false;
				}
				
				break;
			case D2: // x - k, y + k
				
				if (safeGet(board, x - kk, y + kk) != owner)
				{
					run = false;
				}
				
				break;
			}
		}
		return run;
	}
	
	private void safeIncEmpty(int[][] board, int[][] prio, int x, int y, int num)
	{
		if (safeGet(board, x, y) == EMPTY)
		{
			safeInc(prio, x, y, num);
		}
	}
	
	private void markRunEnd(int[][] board, int[][] prio, int x, int y, int n, int dir, int num)
	{
		switch (dir)
		{
		case VER: // y + k
			
			safeIncEmpty(board, prio, x, y - 1, num);
			safeIncEmpty(board, prio, x, y + n, num);
			
			break;
		case HOR: // x + k
			
			safeIncEmpty(board, prio, x - 1 , y, num);
			safeIncEmpty(board, prio, x + n, y, num);
			
			break;
		case D1: // x + k, y + k
			
			safeIncEmpty(board, prio, x - 1, y - 1, num);
			safeIncEmpty(board, prio, x + n, y + n, num);
			
			break;
		case D2: // x - k, y + k

			safeIncEmpty(board, prio, x + 1, y - 1, num);
			safeIncEmpty(board, prio, x - n, y + n, num);

			break;
		}
	}
	
	private int sumBoard()
	{
		return sumArray(board);
	}
	
	private int sumArray(int[][] A)
	{
		int sum = 0;
		
		for (int ii = 0 ; ii < height; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				sum += A[ii][jj];
			}
		}
		
		return sum;
	}
	
	private boolean isEmpty()
	{
		return (sumBoard() == 0);
	}
	
	private void safeInc(int[][] prio, int x, int y, int num)
	{
		if (x >= 0 && x < width && y >= 0 && y < height)
		{
			prio[y][x] += num;
		}
	}
	
	private int safeGet(int[][] board, int x, int y)
	{
		int ret = EMPTY;
		
		if (x >= 0 && x < width && y >= 0 && y < height)
		{
			ret = board[y][x];
		}
		
		return ret;
	}
	
	private Pos findMaxSlot()
	{
		return findBestMove(prio);
	}
	
	// module to capture the game board
	private void scanBoard()
	{
		for (int ii = 0 ; ii < height ; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				int state = EMPTY;
				
				if (game.isFriendly(jj, ii)) state = ME;
				if (game.isOpponent(jj, ii)) state = OP;
				
				board[ii][jj] = state;
			}
		}
	}
	
	private void reset()
	{
		for (int ii = 0 ; ii < height ; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				prio[ii][jj] = 0;
			}
		}
	}
	
	@Override
	public void init(MNKGame game)
	{
		this.game = game;
		width = game.getWidth();
		height = game.getHeight();
		
		board = new int[height][width];
		prio = new int[height][width];
		
		target = game.getTarget();
	}
	
	@Override
	public String toString() { return "Adrians MNK Bot"; }
}

class Pos
{
	public int x, y;
	public int state;
	
	public Pos(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Pos(int x, int y, int state)
	{
		this.x = x;
		this.y = y;
		this.state = state;
	}
	
	public String toString()
	{
		String str = "";
		str += x + " " + y;
		return str;
	}
}

class Outcome
{
	public Pos move;
	public int state = 0; // 0 = no definite, 1 = win, 2 = loss
	public int levels = 0;
	
	public Outcome(int state)
	{
		this.state = state;
		this.levels = 0;
	}
	
	public Outcome(Pos move)
	{
		this.move = move;
	}
}