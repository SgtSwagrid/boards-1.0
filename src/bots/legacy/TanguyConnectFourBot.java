package bots.legacy;

import games.legacy.ConnectFour;
import games.legacy.util.Player;

public class TanguyConnectFourBot implements Player<ConnectFour> {

	@Override
	public void takeTurn(ConnectFour game) {
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		boolean firstTurn = true;
		
		if(firstTurn) {
			for(int i = 0; i < game.WIDTH; i++) {
				if (game.getPiece(i, 0) != null)
					firstTurn = false;
			}
		}
		
		if(firstTurn) {
			game.placePiece(game.WIDTH/2);
		}
		
		
		if(!firstTurn) {
			
			boolean placed = false;
			int pos = 0;
			
			
			for(int x = 0; x < game.WIDTH; x++) {
				int y = 0;
				boolean checkedColumn = false;
				while(y <= game.HEIGHT - 1 && !checkedColumn) {
					if(game.getPiece(x, y) == null) {
						checkedColumn = true;
						Player<?> player = null;
						if(checkFour(x, y, game, player) && !placed) {
							//if(player != this) {
								//pos = x;
							//} else
								game.placePiece(x);
							placed = true; 
					
						}
					}
					y++;	
				}
			}
			
			//if(!placed && pos != 0) {
				//game.placePiece(pos);
				//placed = true;
			// }
			
			if(!placed) {
				int x;
				do {
					x = (int) (Math.random() * game.WIDTH);
				} while(game.getPiece(x, game.HEIGHT - 1) != null);
				
				game.placePiece(x);
			}
		}
	}
	
	private boolean checkFour(int x, int y, ConnectFour game, Player<?> player) {
		
		if (game.getPiece(x, y) == null) {
			Player<?> curPlayer;
			int curX, curY;
			
			curX = x - 1;
			if(curX >= 0) {
				curPlayer = game.getPiece(curX--, y);
				
				
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curX >= 0 && checkLeft) {
						if(game.getPiece(curX--, y) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curX = x + 1;
					boolean checkRight = true;
					while(curX <= game.WIDTH - 1 && checkRight) {
						if(game.getPiece(curX++, y) == curPlayer) {
							count++;
						} else {
							checkRight = false;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}	
			}

			
			curX = x - 1;
			curY = y + 1;
			if(curX >= 0 && curY <= game.HEIGHT - 1) {
				curPlayer = game.getPiece(curX--, curY++);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curX >= 0 && curY <= game.HEIGHT - 1 && checkLeft) {
						if(game.getPiece(curX--, curY++) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curX = x + 1;
					curY = y - 1;
					boolean checkRight = true;
					while(curX <= game.WIDTH - 1 && curY >= 0 && checkRight) {
						if(game.getPiece(curX++, curY--) == curPlayer) {
							count++;
						} else {
							checkRight = false;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
			
			curY = y + 1;
			if(curY <= game.HEIGHT - 1) {
				curPlayer = game.getPiece(x, curY++);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curY <= game.HEIGHT - 1 && checkLeft) {
						if(game.getPiece(x, curY++) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curY = y - 1;
					boolean checkRight = true;
					while(curY >= 0 && checkRight) {
						if(game.getPiece(curX++, curY--) == curPlayer) {
							count++;
						} else {
							checkRight = false;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
			
			curX = x + 1;
			curY = y + 1;
			if(curX <= game.WIDTH - 1 && curY <= game.HEIGHT - 1) {
				curPlayer = game.getPiece(curX++, curY++);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curX <= game.WIDTH - 1 && curY <= game.HEIGHT - 1 && checkLeft) {
						if(game.getPiece(curX++, curY++) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curX = x - 1;
					curY = y - 1;
					boolean checkRight = true;
					while(curX >= 0 && curY >= 0 && checkRight) {
						if(game.getPiece(curX--, curY--) == curPlayer) {
							count++;
						} else {
							checkRight = false;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
			
			curX = x + 1;
			if(curX <= game.WIDTH - 1) {
				curPlayer = game.getPiece(curX++, y);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curX <= game.WIDTH - 1 && checkLeft) {
						if(game.getPiece(curX++, y) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curX = x - 1;
					boolean checkRight = true;
					while(curX >= 0 && checkRight) {
						if(game.getPiece(curX--, y) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
			
			curX = x + 1;
			curY = y - 1;
			if(curX <= game.WIDTH - 1 && curY >= 0) {
				curPlayer = game.getPiece(curX++, curY--);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curX <= game.WIDTH - 1 && curY >= 0 && checkLeft) {
						if(game.getPiece(curX++, curY--) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curX = x - 1;
					curY = y + 1;
					boolean checkRight = true;
					while(curX >= 0 && curY <= game.HEIGHT - 1 && checkRight) {
						if(game.getPiece(curX--, curY++) == curPlayer) {
							count++;
						} else {
							checkRight = true;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
			
			curY = y - 1;
			if(curY >= 0) {
				curPlayer = game.getPiece(x, curY--);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curY >= 0 && checkLeft) {
						if(game.getPiece(x, curY--) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curY = y + 1;
					boolean checkRight = true;
					while(curY <= game.HEIGHT - 1 && checkRight) {
						if(game.getPiece(x, curY++) == curPlayer) {
							count++;
						} else {
							checkRight = false;
						}
					}
					
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
			
			curX = x - 1;
			curY = y + 1;
			if(curX >= 0 && curY <= game.HEIGHT - 1) {
				curPlayer = game.getPiece(curX--, curY++);
			
				if(curPlayer != null) {
					int count = 1;
					boolean checkLeft = true;
					while(curX >= 0 && curY <= game.HEIGHT - 1 && checkLeft) {
						if(game.getPiece(curX--, curY++) == curPlayer) {
							count++;
						} else {
							checkLeft = false;
						}
					}
					
					curX = x + 1;
					curY = y - 1;
					boolean checkRight = true;
					while(curX <= game.WIDTH - 1 && curY >= 0 && checkRight) {
						if(game.getPiece(curX++, curY--) == curPlayer) {
							count++;
						} else {
							checkRight = false;
						}
					}
					
					if(count == game.TARGET - 1) {
						player = curPlayer;
						return true;
					}
					
				}
			}
		}
		
		return false;
		
	}
	
	@Override
	public String toString() { return "Tanguy's Bot"; }
}