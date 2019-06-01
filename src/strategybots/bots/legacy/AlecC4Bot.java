package strategybots.bots.legacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import strategybots.games.legacy.ConnectFour;
import strategybots.games.legacy.util.Player;

public class AlecC4Bot implements Player<ConnectFour> {
	
	
	private static final List<BiFunction<Integer, Integer, Integer>> fx = Arrays.asList(
			(x, i) -> x + i, (x, i) -> x + i, (x, i) -> x, (x, i) -> x - i);
	
	private static final List<BiFunction<Integer, Integer, Integer>> fy = Arrays.asList(
			(y, i) -> y, (y, i) -> y + i, (y, i) -> y + i, (y, i) -> y + i);
	
	private int column = 0;
	
	enum Result { WIN, LOSE, DRAW }
	
	private Result getOptimal(ConnectFour game, int[][] board, int level, int maxLevel) {
		
		boolean player = level % 2 == 0;
		
		//my turn
		if(player) {
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					if(board[x][y] != 0) continue;
					
					int val = board[x][y];
					board[x][y] = 1;
					
					List<List<Vector<Integer>>> s = getStreaks(game, board, true);
					
					if(s.size() != 0 && s.get(0).size() >= game.TARGET) {
						if(level == 0) game.placePiece(x);
						return Result.WIN;
					}
					
					board[x][y] = val;
					if(val == 0) break;
				}
			}
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					if(board[x][y] != 0) continue;
					
					int val = board[x][y];
					board[x][y] = 2;
					
					List<List<Vector<Integer>>> s = getStreaks(game, board, false);
					
					if(s.size() != 0 && s.get(0).size() >= game.TARGET) {
						if(level == 0) game.placePiece(x);
						
						return Result.WIN;
					}
					
					board[x][y] = val;
					if(val == 0) break;
				}
			}
			
			if(level == maxLevel) {
				
				return Result.DRAW;
			}
			
			for(int i = level; i < maxLevel; i++) {
				for(int x = 0; x < game.WIDTH; x++) {
					for(int y = 0; y < game.HEIGHT; y++) {
						
						if(board[x][y] != 0) continue;
						
						int val = board[x][y];
						board[x][y] = 1;
						
						Result r = getOptimal(game, board, level + 1, i);
						
						if(r == Result.LOSE) {
							if(level == 0) game.placePiece(x);
							return Result.WIN;
						} else if(r == Result.WIN) {
							return Result.LOSE;
						}
						
						
						
						board[x][y] = val;
						if(val == 0) break;
					}
				}
			}
			
			return Result.DRAW;
			
			//opponent turn
		} else {
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					if(board[x][y] != 0) continue;
					
					int val = board[x][y];
					board[x][y] = 2;
					
					List<List<Vector<Integer>>> s = getStreaks(game, board, true);
					
					if(s.size() != 0 && s.get(0).size() >= game.TARGET) {
						return Result.WIN;
					}
					
					board[x][y] = val;
					if(val == 0) break;
				}
			}
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					if(board[x][y] != 0) continue;
					
					int val = board[x][y];
					board[x][y] = 1;
					
					List<List<Vector<Integer>>> s = getStreaks(game, board, false);
					
					if(s.size() != 0 && s.get(0).size() >= game.TARGET) {
						return Result.WIN;
					}
					
					board[x][y] = val;
					if(val == 0) break;
				}
			}
			
			if(level == maxLevel) {
				
				return Result.DRAW;
			}
			
			for(int i = level; i < maxLevel; i++) {
				for(int x = 0; x < game.WIDTH; x++) {
					for(int y = 0; y < game.HEIGHT; y++) {
						
						if(board[x][y] != 0) continue;
						
						int val = board[x][y];
						board[x][y] = 1;
						
						Result r = getOptimal(game, board, level + 1, maxLevel);
						
						if(r == Result.LOSE) {
							return Result.WIN;
						} else if(r == Result.WIN) {
							return Result.LOSE;
						}
						
						board[x][y] = val;
						if(val == 0) continue;
					}
				}
			}	
			return Result.DRAW;
		}
		
	}
	
	/*private boolean turnRecurse(ConnectFour game, int[][] dup, int level, int maxLevel) {
		
		for(int i = 0; i < maxLevel; i++) {
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					int val = dup[x][y];
					
					if(i == 0) {
						
						
						
					} else {
						
						if(turnRecurse(game, dup, level + 1))
							return true;
						
					}
					
					
					
					dup[x][y] = 1;
					
					List<List<Vector<Integer>>> s = getStreaks(dup, true);
					
					if(s.size() != 0 && s.get(0).size() == game.TARGET) {
						game.placePiece(x);
						return true;
					}
					
					
					
					dup[x][y] = val;
					
					if(val == 0) continue;
					
					
					
				}
			}
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					int val = dup[x][y];
					
					dup[x][y] = 2;
					
					List<List<Vector<Integer>>> s = getStreaks(dup, false);
					
					if(s.size() != 0 && s.get(0).size() == game.TARGET) {
						System.out.println(x + ", " + y);
						game.placePiece(x);
						
						return true;
					}
					
					dup[x][y] = val;
					
					if(val == 0) continue;
					
					
					
				}
			}
			
		}
		
		
		
		
		
		return false;
	}*/
	
	@Override
	public void takeTurn(ConnectFour game) {
		
		//List<List<Vector<Integer>>> friendlyStreaks = getStreaks(getBoardFromGame(game), false);
		//List<List<Vector<Integer>>> enemyStreaks = getStreaks(getBoardFromGame(game), false);
		//System.out.println(enemyStreaks);
		
		//int[][] dup = duplicate(game, getBoardFromGame(game));
		
		if(getOptimal(game, getBoardFromGame(game), 0, 6) != Result.WIN) {
			
		
		
		
		int r = (int) (Math.random() * game.WIDTH);
		while(game.getPiece(r, game.HEIGHT - 1) != null) {
			r = (int) (Math.random() * game.WIDTH);
			
		}
		
		game.placePiece(r);
		
		}
		
		//if(!turnRecurse(game, dup, 0, 4))
		//	game.placePiece((int) (Math.random() * game.WIDTH));
		/*
		for(List<Vector<Integer>> streak : friendlyStreaks) {
			
			if(streak.size() == game.TARGET - 1) {
				
				for(Vector<Integer> end : getEnds(game, streak)) {
					
					int difficulty = getDifficulty(game, end);
					
					if(difficulty == 1) {
						
						game.placePiece(end.get(0));
						return;
					}
					
				}
				
			} else break;
		}
		
		for(List<Vector<Integer>> streak : enemyStreaks) {
			
			if(streak.size() == game.TARGET - 1) {
				
				for(Vector<Integer> end : getEnds(game, streak)) {
					
					
					
					int difficulty = getDifficulty(game, end);
					System.out.println("end: " + end + " diff: " + difficulty);
					if(difficulty == 1) {
						
						game.placePiece(end.get(0));
						return;
					}
					
				}
				
			} else break;
		}*/
		
		
	}
	
	private List<List<Vector<Integer>>> getStreaks(ConnectFour game, int[][] board, boolean friendly) {
		
		List<List<Vector<Integer>>> friendlyStreaks = new LinkedList<>();
		List<List<Vector<Integer>>> enemyStreaks = new LinkedList<>();
		
		for(int x = 0; x < game.WIDTH; x++) {
			for(int y = 0; y < game.HEIGHT; y++) {
				
				int player = board[x][y];
				
				if(player != 0) {
					
					for(int i = 0; i < 4; i++) {
						
						List<Vector<Integer>> streak = new ArrayList<>();
						
						for(int j = 0; j < game.TARGET; j++) {
							
							BiFunction<Integer, Integer, Integer> f1 = fx.get(i);
							BiFunction<Integer, Integer, Integer> f2 = fy.get(i);
							
							if(0 <= f1.apply(x, j) && f1.apply(x, j) < game.WIDTH &&
							   0 <= f2.apply(y, j) && f2.apply(y, j) < game.HEIGHT &&
									board[fx.get(i).apply(x, j)][fy.get(i).apply(y, j)] == player) {
								Vector<Integer> v = new Vector<>(2);
								v.add(fx.get(i).apply(x, j));
								v.add(fy.get(i).apply(y, j));
								streak.add(v);
							} else break;
						}
						
						if(streak.size() > 1) {
							if(player == 1) friendlyStreaks.add(streak);
							else enemyStreaks.add(streak);
						}
						
					}
				} else continue;
			}
		}
		
		friendlyStreaks = friendlyStreaks.stream().sorted((s1, s2) -> s2.size() - s1.size()).collect(Collectors.toList());
		enemyStreaks = enemyStreaks.stream().sorted((s1, s2) -> s2.size() - s1.size()).collect(Collectors.toList());
		
		return friendly ? friendlyStreaks : enemyStreaks;
	}
	
	private int[][] getBoardFromGame(ConnectFour game) {
		int[][] board = new int[game.WIDTH][game.HEIGHT];
		for(int x = 0; x < game.WIDTH; x++) {
			for(int y = 0; y < game.HEIGHT; y++) {
				if(game.getPiece(x, y) == null) board[x][y] = 0;
				else if(game.getPiece(x, y) == this) board[x][y] = 1;
				else board[x][y] = 2;
			}
		}
		return board;
	}
	
	private int[][] duplicate(ConnectFour game, int[][] b) {
		int[][] board = new int[game.WIDTH][game.HEIGHT];
		for(int x = 0; x < game.WIDTH; x++) {
			for(int y = 0; y < game.HEIGHT; y++) {
				board[x][y] = b[x][y];
			}
		}
		return board;
	}
	
	private List<Vector<Integer>> getEnds(ConnectFour game, List<Vector<Integer>> streak) {
		
		List<Vector<Integer>> ends = new ArrayList<>();
		
		Vector<Integer> tail1 = streak.get(0);
		Vector<Integer> tail1Next = streak.get(1);
		Vector<Integer> tail2 = streak.get(streak.size() - 1);
		Vector<Integer> tail2Next = streak.get(streak.size() - 2);
		
		Vector<Integer> end1 = new Vector<>(2);
		Vector<Integer> end2 = new Vector<>(2);
		
		end1.add(tail1.get(0) + (tail1.get(0) - tail1Next.get(0)));
		end1.add(tail1.get(1) + (tail1.get(1) - tail1Next.get(1)));
		end2.add(tail2.get(0) + (tail2.get(0) - tail2Next.get(0)));
		end2.add(tail2.get(1) + (tail2.get(1) - tail2Next.get(1)));
		
		if(0 <= end1.get(0) && end1.get(0) < game.WIDTH &&
				   0 <= end1.get(1) && end1.get(1) < game.HEIGHT)
			ends.add(end1);
		
		if(0 <= end2.get(0) && end2.get(0) < game.WIDTH &&
				   0 <= end2.get(1) && end2.get(1) < game.HEIGHT)
			ends.add(end2);
		
		return ends;
	}
	
	private int getDifficulty(ConnectFour game, Vector<Integer> pos) {
		if(game.getPiece(pos.get(0), pos.get(1)) != null)
			return Integer.MAX_VALUE;
		int difficulty = 1;
		for(int y = pos.get(1) - 1; y >= 0; y--, difficulty++) {
			if(game.getPiece(pos.get(0), y) != null)
				break;
		}
		return difficulty;
	}
	
	@Override
	public String toString() { return "SwagBot"; }
}