package strategybots.bots.legacy;

import strategybots.games.legacy.DotsAndBoxes;
import strategybots.games.legacy.DotsAndBoxes.Orientation;
import strategybots.games.legacy.DotsAndBoxes.Side;
import strategybots.games.legacy.util.Player;

@SuppressWarnings("unused")
public class TanguyDotsAndBoxesBot implements Player<DotsAndBoxes> {

	@Override
	public void takeTurn(DotsAndBoxes game) {
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		boolean hasTurn = true;
		
		while(hasTurn) {
			hasTurn = false;
		
			for(int i = 0; i < game.WIDTH; i++) {
				for(int j = 0; j < game.HEIGHT; j++) {
					
					if(game.isOccupied(i, j)) {
						continue;
					}
					
					int count = 0;
					Side empty = null;
					if (game.hasLine(i, j, Side.TOP)) {
						count++;
					}
					else {
						empty = Side.TOP;
					}
					if (game.hasLine(i, j, Side.RIGHT)) {
						count++;
					}
					else {
						empty = Side.RIGHT;
					}
					if (game.hasLine(i, j, Side.BOTTOM)) {
						count++;
					}
					else {
						empty = Side.BOTTOM;
					}
					if (game.hasLine(i, j, Side.LEFT)) {
						count++;
					}
					else {
						empty = Side.LEFT;
					}
					
					if(count == 3) {
						if(game.drawLine(i, j, empty))
							hasTurn = true;
					}
				}
			}
			
			if(hasTurn)
				continue;
			
			boolean hasOnlyTwos = true;
			
			for(int i = 0; i < game.WIDTH; i++) {
				for(int j = 0; j < game.HEIGHT; j++) {
					
					if(game.isOccupied(i, j)) {
						continue;
					}
					
					int count = 0;
					if (game.hasLine(i, j, Side.TOP)) {
						count++;
					}
					if (game.hasLine(i, j, Side.RIGHT)) {
						count++;
					}
					if (game.hasLine(i, j, Side.BOTTOM)) {
						count++;
					}
					if (game.hasLine(i, j, Side.LEFT)) {
						count++;
					}
					
					if(count < 2) {
						
						int countXL = 0, countXR = 0, countYD = 0, countYU = 0;
						
						if(i - 1 >= 0)
							countXL = countLines(i - 1, j, game);
						
						if(i + 1 <= game.WIDTH - 1)
							countXR = countLines(i + 1, j, game);
						
						if(j - 1 >= 0)
							countYD = countLines(i, j - 1, game);
						
						if(j + 1 <= game.HEIGHT - 1)
							countYU = countLines(i, j + 1, game);
						
						if(countXL < 2 && countXR < 2 && countYD < 2 && countYU < 2) {
							hasOnlyTwos = false;
						}
					}
				}
			}
			
			int x, y;
			Side side;
			
			if(hasOnlyTwos) {
				do {
					x = (int) (Math.random() * game.WIDTH);
					y = (int) (Math.random() * game.HEIGHT);
					side = Side.values()[(int) (Math.random() * 4)];
				} while(game.hasLine(x,y,side));
				
				if(game.drawLine(x, y, side))
					hasTurn = true;
				
			} else {
				
				boolean valid;
				
				do {
					
					valid = false;
					x = (int) (Math.random() * game.WIDTH);
					y = (int) (Math.random() * game.HEIGHT);
					side = Side.values()[(int) (Math.random() * 4)];
					
					int count = countLines(x, y, game);
					
					if(count < 2) {
						
						switch (side) {
						
						case TOP: 
							if(y + 1 <= game.HEIGHT - 1) {
								if(countLines(x, y + 1, game) < 2)
									valid = true;
							} else {
								valid = true;
							}
							break;
						case RIGHT: 
							if(x + 1 <= game.WIDTH - 1) {
								if(countLines(x + 1, y, game) < 2)
									valid = true;
							} else {
								valid = true;
							}
							break;
						case BOTTOM: 
							if(y - 1 >= 0) {
								if(countLines(x, y - 1, game) < 2)
									valid = true;
							} else {
								valid = true;
							}
							break;
						case LEFT: 
							if(x - 1 >= 0) {
								if(countLines(x - 1, y, game) < 2)
									valid = true;
							} else {
								valid = true;
							}
							break;
						default: 
							break;
						
						}
					}
					
					
				} while(game.hasLine(x, y, side) || !valid);

				
				if(game.drawLine(x, y, side))
					hasTurn = true;
			}
			
		}
	}
	
	private static int countLines(int x, int y, DotsAndBoxes game) {
		
		int count = 0;
		
		if (game.hasLine(x, y, Side.TOP)) {
			count++;
		}
		if (game.hasLine(x, y, Side.RIGHT)) {
			count++;
		}
		if (game.hasLine(x, y, Side.BOTTOM)) {
			count++;
		}
		if (game.hasLine(x, y, Side.LEFT)) {
			count++;
		}
		
		return count;
	}
	
	
	@Override
	public String toString() { return "Tanguy's Bot"; }
}