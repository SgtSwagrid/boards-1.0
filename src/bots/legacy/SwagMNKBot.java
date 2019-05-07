package bots.legacy;

import java.util.ArrayList;
import java.util.List;

import games.legacy.MNKGame;
import games.legacy.util.Player;

public class SwagMNKBot implements Player<MNKGame> {
	
	public int FORESIGHT = 4;
	
	private MNKGame game;
	private int[][] board;
	
	@Override
	public void init(MNKGame game) {
		this.game = game;
		if(game.getWidth() * game.getHeight() > 25) FORESIGHT = 3;
		else FORESIGHT = 4;
		board = new int[game.getWidth()][game.getHeight()];
	}
	
	private int turn = 0;

	@Override
	public void takeTurn(MNKGame game) {
		loadBoard();
		if(turn == 0) {
			all: for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					if(board[i - 1 + (game.getWidth() / 2)][j - 1 + (game.getHeight() / 2)] == 0) {
						game.placePiece(i - 1 + (game.getWidth() / 2), j - 1 + (game.getHeight() / 2));
						break all;
					}
				}
			}
		}
		else if(turn == 1) {
			takeTurnRecurse(board, 0, FORESIGHT);
		} else
			takeTurnRecurse(board, 0, FORESIGHT);
		
		turn++;
	}
	
	private int takeTurnRecurse(int[][] board, int depth, int maxDepth) {
		
		int score = Integer.MIN_VALUE;
		List<Position> moves = new ArrayList<>();
		
		for(int x = 0; x < game.getWidth(); x++) {
			for(int y = 0; y < game.getHeight(); y++) {
				
				if(board[x][y] != 0) continue;
				
				board[x][y] = (depth % 2) + 1;
				
				//Win.
				if(detectWin(board) != 0) {
					
					score = (maxDepth - depth) * 2000;
					moves.clear();
					moves.add(new Position(x, y));
					board[x][y] = 0;
					break;
					
				//Go deeper.
				} else if(depth < maxDepth) {
					
					int result = -takeTurnRecurse(board, depth + 1, maxDepth);
					
					if(result >= score) {
						if(result > score) {
							score = result;
							moves.clear();
						}
						moves.add(new Position(x, y));
					}
					
					board[x][y] = 0;
					
				//Max depth - use heuristic instead.
				} else {
					
					score = (depth % 2 == 0 ? 1 : -1) * heuristicScore(board);
					board[x][y] = 0;
					break;
					
				}
			}
		}
		
		if(depth == 0) {
			if(!moves.isEmpty()) {
				int move = (int) (Math.random() * moves.size());
				game.placePiece(moves.get(move).x, moves.get(move).y);
			}
		}
		
		return score;
	}
	
	private int heuristicScore(int[][] board) {
		
		int score = 0;
		
		for(int y = 0; y < game.getHeight(); y++) {
			
			int streak = 0, prev = 0;
			boolean open = false;
			
			for(int x = 0; x < game.getWidth(); x++) {
				
				if(x == game.getWidth() - 1) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					
				} else if(board[x][y] == 0) {
					score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak + (open ? 1 : 0) - 4);
					streak = 0;
					prev = 0;
					open = true;
					
				} else if(board[x][y] != prev) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					streak = 1;
					prev = board[x][y];
					open = false;
					
				} else if(board[x][y] == prev) {
					streak++;
				}
			}
		}
		
		for(int x = 0; x < game.getWidth(); x++) {
			
			int streak = 0, prev = 0;
			boolean open = false;
			
			for(int y = 0; y < game.getHeight(); y++) {
				
				if(y == game.getHeight() - 1) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					
				} else if(board[x][y] == 0) {
					score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak + (open ? 1 : 0) - 4);
					streak = 0;
					prev = 0;
					open = true;
					
				} else if(board[x][y] != prev) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					streak = 1;
					prev = board[x][y];
					open = false;
					
				} else if(board[x][y] == prev) {
					streak++;
				}
			}
		}
		
		int size = Math.max(game.getWidth(), game.getHeight());
		
		for(int i = 0; i < size; i++) {
			
			int streak = 0, prev = 0;
			boolean open = false;
			
			for(int x = 0, y = i; x < size && y >= 0; x++, y--) {
				
				if(x >= game.getWidth() || y >= game.getHeight()) continue;
				
				if(x + y == i) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					
				} else if(board[x + i][y + i] == 0) {
					score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak + (open ? 1 : 0) - 4);
					streak = 0;
					prev = 0;
					open = true;
					
				} else if(board[x + i][y + i] != prev) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					streak = 1;
					prev = board[x + i][y + i];
					open = false;
					
				} else if(board[x + i][y + i] == prev) {
					streak++;
				}
			}
		}
		
		for(int i = 0; i < size; i++) {
			
			int streak = 0, prev = 0;
			boolean open = false;
			
			for(int x = 0, y = i; x < size && y >= 0; x++, y--) {
				
				if(x >= game.getWidth() || y >= game.getHeight()) continue;
				
				if(x + y == i) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					
				} else if(board[x + i][game.getHeight() - (y + i) - 1] == 0) {
					score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak + (open ? 1 : 0) - 4);
					streak = 0;
					prev = 0;
					open = true;
					
				} else if(board[x + i][game.getHeight() - (y + i) - 1] != prev) {
					if(open) score += (prev == 0 ? 1 : -2) * Math.pow(2, 2 * streak - 4);
					streak = 1;
					prev = board[x + i][game.getHeight() - (y + i) - 1];
					open = false;
					
				} else if(board[x + i][game.getHeight() - (y + i) - 1] == prev) {
					streak++;
				}
			}
		}
		
		return score;
	}
	
	private void loadBoard() {
		
		for(int x = 0; x < game.getWidth(); x++) {
			for(int y = 0; y < game.getHeight(); y++) {
				
				if(game.isEmpty(x, y)) 		   board[x][y] = 0;
				else if(game.isFriendly(x, y)) board[x][y] = 1;
				else if(game.isOpponent(x, y)) board[x][y] = 2;
			}
		}
	}
	
	private int detectWin(int[][] board) {
		
		for(int y = 0; y < game.getHeight(); y++) {
			int streak = 0, prev = 0;
			for(int x = 0; x < game.getWidth(); x++) {
				if(board[x][y] != 0 && board[x][y] == prev) streak++;
				else { streak = 1; prev = board[x][y]; }
				if(streak >= game.getTarget()) return prev;
			}
		}
		
		for(int x = 0; x <= game.getWidth() - game.getTarget(); x++) {
			for(int y = 0; y <= game.getHeight() - game.getTarget(); y++) {
				int streak = 0, prev = 0;
				for(int i = 0; i < game.getTarget(); i++) {
					if(board[x + i][y + i] != 0 && board[x + i][y + i] == prev) streak++;
					else { streak = 1; prev = board[x + i][y + i]; }
					if(streak >= game.getTarget()) return prev;
				}
			}
		}
		
		for(int x = 0; x < game.getWidth(); x++) {
			int streak = 0, prev = 0;
			for(int y = 0; y < game.getHeight(); y++) {
				if(board[x][y] != 0 && board[x][y] == prev) streak++;
				else { streak = 1; prev = board[x][y]; }
				if(streak >= game.getTarget()) return prev;
			}
		}
		
		for(int x = game.getTarget() - 1; x < game.getWidth(); x++) {
			for(int y = 0; y <= game.getHeight() - game.getTarget(); y++) {
				int streak = 0, prev = 0;
				for(int i = 0; i < game.getTarget(); i++) {
					if(board[x - i][y + i] != 0 && board[x - i][y + i] == prev) streak++;
					else { streak = 1; prev = board[x - i][y + i]; }
					if(streak >= game.getTarget()) return prev;
				}
			}
		}
		
		return 0;
	}
	
	@Override
	public String toString() { return "SwagBot"; }
	
	private static class Position {
		
		int x, y;
		
		Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public String toString() { return "(" + x + ", " + y + ")"; }
		
		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Position)) return false;
			Position p = (Position) o;
			return p.x == x && p.y == y;
		}
	}
}