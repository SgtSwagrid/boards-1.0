package strategybots.bots.legacy;

import strategybots.games.legacy.DotsAndBoxes;
import strategybots.games.legacy.DotsAndBoxes.Orientation;
import strategybots.games.legacy.DotsAndBoxes.Side;
import strategybots.games.legacy.util.Player;

@SuppressWarnings("unused")
public class AdrianDotBot implements Player<DotsAndBoxes> {

	private int width, height;
	private DotsAndBoxes game;
	
	
	@Override
	public void takeTurn(DotsAndBoxes game) {
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		boolean more = true;
		
		if (isEmpty())
		{
			safeDraw(width/2, height /2);
		}
		else
		{
		
			while (more)
			{
				for (int ii = 0 ; ii < width; ii++)
				{
					for (int jj = 0 ; jj < width; jj++)
					{
						
						if (threeSides(ii, jj) && more)
						{
							more = safeDraw(ii, jj);
						}
						else if (oneSide(ii, jj) && more)
						{
							more = safeDraw(ii, jj);
						}
						else if (twoSides(ii, jj) && more)
						{
							more = safeDraw(ii, jj);
						}
					}
				}
			}
		}
	}
	
	private boolean safeDraw(int x, int y)
	{
		boolean another = false;
		
		if (!game.hasLine(x, y, Side.BOTTOM))
		{
			another = game.drawLine(x, y, Side.BOTTOM);
		}
		else if (!game.hasLine(x, y, Side.TOP))
		{
			another = game.drawLine(x, y, Side.TOP);
		}
		else if (!game.hasLine(x, y, Side.LEFT))
		{
			another = game.drawLine(x, y, Side.LEFT);
		}
		else if (!game.hasLine(x, y, Side.RIGHT))
		{
			another = game.drawLine(x, y, Side.RIGHT);
		}
		
		return another;
	}
	
	private boolean isEmpty()
	{
		boolean empty = true;
		
		for (int ii = 0 ; ii < width; ii++)
		{
			for (int jj = 0 ; jj < height ; jj++)
			{
				if (countSides(ii, jj) != 0)
				{
					empty = false;
				}
			}
		}
		
		return empty;
	}
	
	private boolean oneSide(int x, int y)
	{
		boolean has1 = false;
		
		if (countSides(x,y) == 1)
		{
			has1 = true;
		}
		
		return has1;
	}
	
	private boolean twoSides(int x, int y)
	{
		boolean has2 = false;
		
		if (countSides(x,y) == 2)
		{
			has2 = true;
		}
		
		return has2;
	}
	
	private boolean threeSides(int x, int y)
	{
		boolean has3 = false;
		
		if (countSides(x,y) == 3)
		{
			has3 = true;
		}
		
		return has3;
	}
	
	private int countSides(int x, int y)
	{
		int sides = 0;
		
		if (game.hasLine(x, y, Side.BOTTOM)) sides++;
		if (game.hasLine(x, y, Side.TOP)) sides++;
		if (game.hasLine(x, y, Side.LEFT)) sides++;
		if (game.hasLine(x, y, Side.RIGHT)) sides++;
		
		return sides;
	}
	
	@Override
	public void init(DotsAndBoxes game)
	{
		width = game.WIDTH;
		height = game.HEIGHT;
		this.game = game;
	}
	
	@Override
	public String toString() { return "Adrians Dot Bot"; }
}