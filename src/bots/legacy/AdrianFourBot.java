package bots.legacy;

import games.legacy.ConnectFour;
import games.legacy.util.Player;

public class AdrianFourBot implements Player<ConnectFour> {

	private int width, height;
	private Player gameState[][], tempState[][];
	private int priority[];
	
	@Override
	public void takeTurn(ConnectFour game) {
	
		//Scan the board and save the states
		reset();
		scanBoard(game);
		
		findOnes();
		findTwos();
		findThrees();
		findFours();
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//simulatePlaceAndCheck();
		
		
		game.placePiece(placeValidSlot());
		
	}
	
	private void simulatePlaceAndCheck()
	{
		int oldState[] = priority;
		tempState = gameState;
		
		int slot = findMaxSlot();
		
		for (int ii = 0 ; ii < height; ii++)
		{
			if (gameState[ii][slot] == null)
			{
				gameState[ii][slot] = this;
				break;
			}
		}
		
		reset();
		boolean found = findFours();
		
		if (found)
		{
			priority[slot] = 0;
			//priority = oldState;
			
			gameState = tempState;
			System.out.println("eneny four avoided");
			
			simulatePlaceAndCheck();
		}
		
		priority = oldState;
		gameState = tempState;
	}
	
	private boolean findFours()
	{
		boolean foundAFour = false;
		
		for (int xx = 0; xx < width; xx++)
		{
			for (int yy = 0 ; yy < height ; yy++)
			{
				
				int vertM = 0, horM = 0, dia1M = 0, dia2M = 0;
				int vertO = 0, horO = 0, dia1O = 0, dia2O = 0;
				
				for (int kk = 0; kk < 4; kk++)
				{
					if (isA(xx + kk, yy))
					{
						if (isMe(xx + kk, yy))
						{
							horM++;
						}
						else if (isOpp(xx + kk, yy))
						{
							horO++;
						}
					}
					
					if (isA(xx + kk, yy + kk))
					{
						if (isMe(xx + kk, yy + kk))
						{
							dia1M++;
						}
						else if (isOpp(xx + kk, yy + kk))
						{
							dia1O++;
						}
					}
					
					if (isA(xx - kk, yy + kk))
					{
						if (isMe(xx - kk, yy + kk))
						{
							dia2M++;
						}
						else if (isOpp(xx - kk, yy + kk))
						{
							dia2O++;
						}
					}
					
					if (isA(xx, yy + kk))
					{
						if (isMe(xx, yy + kk))
						{
							vertM++;
						}
						else if (isOpp(xx, yy + kk))
						{
							vertO++;
						}
					}
				}
				
				for (int kk = 0; kk < 4; kk++)
				{
				
					if (vertM == 3 || vertO == 3)
					{
						if (vertO == 3)
						{
							foundAFour = true;
						}
						safeBlock(xx, yy + kk, vertM == 3 ? 80 : 40);
					}
					
					if (dia1M == 3 || dia1O == 3)
					{
						if (dia1O == 3)
						{
							foundAFour = true;
						}
						safeBlock(xx + kk, yy + kk, dia1M == 3 ? 80 : 40);
					}
					
					if (dia2M == 3 || dia2O == 3)
					{
						if (dia2O == 3)
						{
							foundAFour = true;
						}
						safeBlock(xx - kk, yy + kk, dia2M == 3 ? 80 : 40);
					}
					
					if (horM == 3 || horO == 3)
					{
						if (horO == 3)
						{
							foundAFour = true;
						}
						safeBlock(xx + kk, yy, horM == 3 ? 80 : 40);
					}
				}
				
			}
		}
		
		return foundAFour;
	}
	
	private int placeValidSlot()
	{
		int slot = findMaxSlot();
		if (gameState[height-1][slot] != null)
		{
			// remove max
			priority[slot] = 0;
			
			boolean cont = false;
			for (int ii = 0 ; ii < width; ii++)
			{
				if (priority[ii] != 0)
				{
					cont = true;
				}
			}
			
			if (cont)
			{
				slot = placeValidSlot();
			}
			else
			{
				slot = findEmptySlot();
			}
					
		}
		
		return slot;
	}
	
	private int findEmptySlot()
	{
		int found = 0;
		
		for (int ii = 0 ; ii < width; ii++)
		{
			if (gameState[height -1][ii] == null)
			{
				found = ii;
				break;
			}
		}
		
		return found;
	}
	
	// module to capture the game board
	private void scanBoard(ConnectFour game)
	{
		tempState = gameState;
		
		for (int ii = 0 ; ii < game.HEIGHT ; ii++)
		{
			for (int jj = 0 ; jj < game.WIDTH; jj++)
			{
				gameState[ii][jj] = game.getPiece(jj, ii);
				if (gameState[ii][jj] != null && gameState[ii][jj].equals(this))
				{
					//System.out.println("Adrian has dot at " + jj);
				}
			}
		}
	}
	
	private void reset()
	{
		for (int ii = 0 ; ii < width; ii++)
		{
			priority[ii] = 0;
		}
	}
	
	private void findOnes()
	{
		if (countPieces() == 1)
		{
			for (int ii = 0 ; ii < width ; ii++)
			{
				if (gameState[0][ii] != null)
				{
					safeInc(ii+1, 1);
					safeInc(ii-1, 1);
				}
			}
		}
		else if (isFirstTurn())
		{
			priority[width / 2]++;
		}
		else if (countPieces() > 1)
		{
			for (int ii = 0 ; ii < width ; ii++)
			{
				if (gameState[0][ii] != null)
				{
					safeInc(ii+1, 1);
					safeInc(ii-1, 1);
					safeInc(ii, 1, 1);
				}
			}
		}
	}
	
	// to find and block opponents
	private void findTwos()
	{
		for (int xx = 0; xx < width; xx++)
		{
			for (int yy = 0 ; yy < height ; yy++)
			{
				// kernel left right
				if (isA(xx,yy) && isA(xx+1, yy))
				{
					if (isOpp(xx, yy) && isOpp(xx + 1, yy))
					{
						// BOGEY double
						safeBlock(xx - 1, yy, 5);
						safeBlock(xx + 2, yy, 5);
					}
					else if (isMe(xx, yy) && isMe(xx + 1, yy))
					{
						// PERSON DOUBLE
						safeBlock(xx - 1, yy, 5);
						safeBlock(xx + 2, yy, 5);
					}
				}
				
				//diag
				if (isA(xx,yy) && isA(xx+1, yy+1))
				{
					if (isOpp(xx, yy) && isOpp(xx + 1, yy + 1))
					{
						// BOGEY double
						safeBlock(xx - 1, yy - 1, 5);
						safeBlock(xx + 2, yy + 2, 5);
					}
					else if (isMe(xx, yy) && isMe(xx + 1, yy+1))
					{
						// PERSON DOUBLE
						safeBlock(xx - 1, yy - 1, 5);
						safeBlock(xx + 2, yy + 2, 5);
					}
				}
				
				//other dia
				if (isA(xx,yy) && isA(xx - 1, yy + 1))
				{
					if (isOpp(xx, yy) && isOpp(xx - 1, yy + 1))
					{
						// BOGEY double
						safeBlock(xx + 1, yy - 1, 5);
						safeBlock(xx - 2, yy + 2, 5);
					}
					else if (isMe(xx, yy) && isMe(xx - 1, yy+1))
					{
						// PERSON DOUBLE
						safeBlock(xx + 1, yy - 1, 5);
						safeBlock(xx - 2, yy + 2, 5);
					}
				}
				
				
				//vert
				if (isA(xx,yy) && isA(xx, yy+1))
				{
					if (isOpp(xx, yy) && isOpp(xx, yy + 1))
					{
						// BOGEY double
						safeBlock(xx, yy - 1, 5);
						safeBlock(xx, yy + 2, 5);
					}
					else if (isMe(xx, yy) && isMe(xx, yy+1))
					{
						// PERSON DOUBLE
						safeBlock(xx, yy - 1, 5);
						safeBlock(xx, yy + 2, 5);
					}
				}
			}
		}
	}
	
	private int countPieces()
	{
		int number = 0;
		
		for (int ii = 0; ii < height; ii++)
		{
			for (int jj = 0 ; jj < width; jj++)
			{
				if (gameState[ii][jj] != null)
				{
					number++;
				}
			}
		}
		
		return number;
	}
	
	private boolean isFirstTurn()
	{
		boolean isFirst = true;
		
		for (int ii = 0 ; ii < width; ii++)
		{
			if (gameState[0][ii] != null)
			{
				isFirst = false;
			}
		}
		
		return isFirst;
	}
	
	private boolean isA(int xx, int yy)
	{
		return (safeGet(xx, yy) != null);
	}
	
	private boolean isOpp(int xx, int yy)
	{
		return !safeGet(xx, yy).equals(this);
	}
	
	private boolean isMe(int xx, int yy)
	{
		return safeGet(xx, yy).equals(this);
	}
	
	private void safeBlock(int xx, int yy, int num)
	{
		if (canBlock(xx, yy))
		{
			safeInc(xx, num);
		}
	}
	
	private void findThrees()
	{
		for (int xx = 0; xx < width; xx++)
		{
			for (int yy = 0 ; yy < height ; yy++)
			{
				// hozir
				if (isA(xx, yy) && isA(xx+1, yy) && isA(xx+2, yy))
				{
					if (isOpp(xx, yy) && isOpp(xx+1, yy) && isOpp(xx+2, yy))
					{
						safeBlock(xx-1, yy, 10);
						safeBlock(xx+3, yy, 10);
					}
					else if (isMe(xx, yy) && isMe(xx+1, yy) && isMe(xx+2, yy))
					{
						safeBlock(xx-1, yy, 10);
						safeBlock(xx+3, yy, 10);
					}
				}
				
				//diag
				if (isA(xx, yy) && isA(xx+1, yy+1) && isA(xx+2, yy+2))
				{
					if (isOpp(xx, yy) && isOpp(xx+1, yy+1) && isOpp(xx+2, yy+2))
					{
						safeBlock(xx-1, yy-1, 10);
						safeBlock(xx+3, yy+3, 10);
					}
					else if (isMe(xx, yy) && isMe(xx+1, yy+1) && isMe(xx+2, yy+2))
					{
						safeBlock(xx-1, yy-1, 10);
						safeBlock(xx+3, yy+3, 10);
					}
				}
				
				//diag
				if (isA(xx, yy) && isA(xx-1, yy+1) && isA(xx-2, yy+2))
				{
					if (isOpp(xx, yy) && isOpp(xx-1, yy+1) && isOpp(xx-2, yy+2))
					{
						safeBlock(xx-3, yy+3, 10);
						safeBlock(xx+1, yy-1, 10);
					}
					else if (isMe(xx, yy) && isMe(xx-1, yy+1) && isMe(xx-2, yy+2))
					{
						safeBlock(xx-3, yy+3, 10);
						safeBlock(xx+1, yy-1, 10);
					}
				}
				
				//vert
				if (isA(xx, yy) && isA(xx, yy+1) && isA(xx, yy+2))
				{
					if (isOpp(xx, yy) && isOpp(xx, yy+1) && isOpp(xx, yy+2))
					{
						safeBlock(xx, yy-1, 10);
						safeBlock(xx, yy+3, 10);
					}
					else if (isMe(xx, yy) && isMe(xx, yy+1) && isMe(xx, yy+2))
					{
						safeBlock(xx, yy-1, 10);
						safeBlock(xx, yy+3, 10);
					}
				}
			}
		}
	}
	
	private boolean canBlock(int xx, int yy)
	{
		boolean canBlock = false;
		
		if (safeGet(xx, yy) == null)
		{
			if (yy != 0)
			{
				if (safeGet(xx, yy-1) != null)
				{
					canBlock = true;
				}
			}
			else
			{
				canBlock = true;
			}
		}
					
		return canBlock;
	}
	
	private void safeInc(int x, int num)
	{
		safeInc(x, 0, num);
	}
	
	private void safeInc(int x, int y, int num)
	{
		if (x >= 0 && x < width && y >= 0 && y < height)
		{
			priority[x] += num;
		}
	}
	
	private Player safeGet(int x, int y)
	{
		Player ret = null;
		
		if (x >= 0 && x < width && y >= 0 && y < height)
		{
			ret = gameState[y][x];
		}
		
		return ret;
	}
	
	private int findMaxSlot()
	{
		int max = 0;
		int maxIdx = 0;
		
		for (int ii = 0; ii < priority.length; ii++)
		{
			if (priority[ii] > max)
			{
				max = priority[ii];
				maxIdx = ii;
			}
		}
		
		return maxIdx;
	}
	
	private int findMax()
	{
		int max = 0;
		
		for (int ii = 0; ii < priority.length; ii++)
		{
			if (priority[ii] > max)
			{
				max = priority[ii];
			}
		}
		
		return max;
	}
	
	@Override 
	public void init(ConnectFour game)
	{
		gameState = new Player[game.HEIGHT][game.WIDTH];
		tempState = new Player[game.HEIGHT][game.WIDTH];
		priority = new int[game.WIDTH];
		height = game.HEIGHT;
		width = game.WIDTH;
	}
	
	@Override
	public String toString() { return "Adrian's Fourth Bot"; }
}