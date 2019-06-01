package strategybots.bots.legacy;

import strategybots.games.legacy.ConnectFour;
import strategybots.games.legacy.util.Player;

public class SwagConnectBot implements Player<ConnectFour> {
	
	private ConnectFour game;
	private int board[][], height[];
	
	private volatile int move, depth;
	
	private long timeout;
	
	private int turn = 0;
	
	private TranspositionTable table;
	
	public SwagConnectBot(long timeout) {
		this.timeout = timeout;
	}
	
	@Override
	public void init(ConnectFour game) {
		this.game = game;
		board = new int[game.WIDTH][game.HEIGHT];
		height = new int[game.WIDTH];
		table = new TranspositionTable(6700417, board, 3);
	}
	
	@Override
	public void takeTurn(ConnectFour game) {
		
		turn++;
		
		loadBoard();
		table.reload(board);
		
		Thread t = new Thread() {
			public void run() {
				getMove(board, height);
			}
		};
		
		t.start();
		
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		game.placePiece(move);
		
		t.interrupt();
		
		System.out.println("Depth: " + depth);
	}
	
	private void getMove(int[][] board, int[] height) {
		
		TranspositionTable table = null, previousTable = null;
		
		for(int i = 0; !Thread.interrupted(); i++) {
			previousTable = table;
			table = new TranspositionTable(6700417, board, 3);
			getMoveRecurse(board, height, 0, i, -Integer.MAX_VALUE,
					Integer.MAX_VALUE, table, previousTable);
			depth = i;
		}
	}
	
	private int getMoveRecurse(int[][] board, int[] height,
			int depth, int maxDepth, int a, int b,
			TranspositionTable table, TranspositionTable previousTable) {
		
		if(depth > maxDepth) return 0;
		
		int a1 = a;
		
		if(table.exists()) {
			
			switch(table.flag()) {
				case 0: return table.value();
				case 1: a = Math.max(a, table.value()); break;
				case 2: b = Math.min(b, table.value()); break;
			}
			
			if(a >= b)
				return table.value();
		}
		
		int value = Integer.MIN_VALUE;
		
		int move = 0;
		boolean full = true;
		
		int[] moves = new int[game.WIDTH];
		int[] priorities = new int[game.WIDTH];
		
		if(previousTable != null) {
			
			for(int i = 0; i < game.WIDTH; i++) {
				priorities[i] = Integer.MIN_VALUE;
			}
			
			for(int x = 0; x < game.WIDTH; x++) {
				
				if(height[x] == game.HEIGHT) continue;
				
				previousTable.update(x, height[x]++, 0, depth % 2 + 1);
				
				
				
				if(previousTable.exists()) {
					
					int priority = previousTable.value();
					
					int registerMove = 0, registerPriority = 0;
					boolean done = false;
					for(int i = 0; i < game.WIDTH; i++) {
						
						if(priority < priorities[i]) {
							done = true;
							registerMove = moves[i];
							registerPriority = moves[i];
							if(!done) {
								moves[i] = x;
								priorities[i] = priority;
							}
							if(i + 1 < game.WIDTH) {
								moves[i + 1] = registerMove;
								priorities[i + 1] = registerPriority;
							}
						}
					}
					
				}
					
					
				previousTable.update(x, --height[x], depth % 2 + 1, 0);
			}
		} else moves = new int[] {0, 1, 2, 3, 4, 5, 6};
		
		
		for(int x : moves) {
			
			if(height[x] == game.HEIGHT) continue;
			
			full = false;
			
			board[x][height[x]++] = depth % 2 + 1;
			table.update(x, height[x] - 1, 0, depth % 2 + 1);
			if(previousTable != null) previousTable.update(x, height[x] - 1, 0, depth % 2 + 1);
			
			if(detectWin(board) != 0) {
				
				value = 42 - (turn + depth);
				move = x;
				
			} else {
				
				int result = -getMoveRecurse(board, height, depth + 1, maxDepth, -b, -a, table, previousTable);
				
				if(result > value) {
					value = result;
					move = x;
				}
			}
			
			a = value > a ? value : a;
			
			board[x][--height[x]] = 0;
			table.update(x, height[x], depth % 2 + 1, 0);
			if(previousTable != null) previousTable.update(x, height[x], depth % 2 + 1, 0);
			
			if(a >= b) break;
			
			if(Thread.currentThread().isInterrupted()) break;
		}
		
		int flag;
		if(value <= a1) 	flag = 2;
		else if(value >= b) flag = 1;
		else 				flag = 0;
		
		table.put(value, (int) Math.pow(game.WIDTH,
				maxDepth - depth), flag);
		
		if(depth == 0) this.move = move;
	
		return full ? 0 : value;
	}
	
	private int detectWin(int[][] board) {
		
		for(int y = 0; y < game.HEIGHT; y++) {
			int streak = 0, prev = 0;
			for(int x = 0; x < game.WIDTH; x++) {
				if(board[x][y] != 0 && board[x][y] == prev) streak++;
				else { streak = 1; prev = board[x][y]; }
				if(streak >= game.TARGET) return prev;
			}
		}
		
		for(int x = 0; x <= game.WIDTH - game.TARGET; x++) {
			for(int y = 0; y <= game.HEIGHT - game.TARGET; y++) {
				int streak = 0, prev = 0;
				for(int i = 0; i < game.TARGET; i++) {
					if(board[x + i][y + i] != 0 && board[x + i][y + i] == prev) streak++;
					else { streak = 1; prev = board[x + i][y + i]; }
					if(streak >= game.TARGET) return prev;
				}
			}
		}
		
		for(int x = 0; x < game.WIDTH; x++) {
			int streak = 0, prev = 0;
			for(int y = 0; y < game.HEIGHT; y++) {
				if(board[x][y] != 0 && board[x][y] == prev) streak++;
				else { streak = 1; prev = board[x][y]; }
				if(streak >= game.TARGET) return prev;
			}
		}
		
		for(int x = game.TARGET - 1; x < game.WIDTH; x++) {
			for(int y = 0; y <= game.HEIGHT - game.TARGET; y++) {
				int streak = 0, prev = 0;
				for(int i = 0; i < game.TARGET; i++) {
					if(board[x - i][y + i] != 0 && board[x - i][y + i] == prev) streak++;
					else { streak = 1; prev = board[x - i][y + i]; }
					if(streak >= game.TARGET) return prev;
				}
			}
		}
		
		return 0;
	}
	
	private void loadBoard() {
		
		for(int x = 0; x < game.WIDTH; x++) {
			for(int y = 0; y < game.HEIGHT; y++) {
				
				if(game.getPiece(x, y) == null) {
					board[x][y] = 0;
					height[x] = y;
					break;
					
				} else {
					board[x][y] = game.getPiece(x, y) == this ? 1 : 2;
					if(y == game.HEIGHT - 1) {
						height[x] = game.HEIGHT;
					}
				}
			}
		}
	}
	
	@Override
	public String toString() { return "SwagBot"; }
	
	private static class TranspositionTable {
		
		private int hash;
		
		private int[] keys, values, priorities, flags;
		
		private int[][][] zobrist;
		
		public TranspositionTable(int tableSize, int[][] board, int numPieces) {
			
			zobrist = generateZobrist(board, numPieces);
			hash = zobristHash(board);
			
			keys = new int[tableSize];
			values = new int[tableSize];
			priorities = new int[tableSize];
			flags = new int[tableSize];
			
		}
		
		public void put(int value, int priority, int flag) {
			
			int key = (int) (hash % values.length);
			
			if(priority >= priorities[key] || keys[key] == hash) {
				
				if(keys[key] == hash) {
					priorities[key] += priority;
					
				} else {
					keys[key] = hash;
					priorities[key] = priority;
				}
				
				values[key] = value;
				flags[key] = flag;
			}
		}
		
		public int value() {
			return values[(int) (hash % values.length)];
		}
		
		public int flag() {
			return flags[(int) (hash % values.length)];
		}
		
		public boolean exists() {
			return keys[(int) (hash % values.length)] == hash;
		}
		
		public void update(int x, int y, int oldPiece, int newPiece) {
			hash ^= zobrist[x][y][oldPiece];
			hash ^= zobrist[x][y][newPiece];
		}
		
		public void reload(int[][] board) {
			hash = zobristHash(board);
		}
		
		public void clear() {
			keys = new int[keys.length];
		}
		
		private int zobristHash(int[][] board) {
			
			int hash = 0;
			
			for(int x = 0; x < board.length; x++) {
				for(int y = 0; y < board[x].length; y++) {
					
					hash ^= zobrist[x][y][board[x][y]];
				}
			}
			
			return hash;
		}
		
		private int[][][] generateZobrist(int[][] board, int numPieces) {
			
			int[][][] zobrist = new int[board.length][board[0].length][numPieces];
			
			for(int x = 0; x < board.length; x++) {
				for(int y = 0; y < board[x].length; y++) {
					for(int i = 0; i < numPieces; i++) {
						
						zobrist[x][y][i] = (int) (Math.random() * Integer.MAX_VALUE);
					}
				}
			}
			
			return zobrist;
		}
	}
}