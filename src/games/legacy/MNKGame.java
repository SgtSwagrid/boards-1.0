package games.legacy;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

import games.legacy.util.Game;
import games.legacy.util.Player;

public class MNKGame implements Game {
	
	private static String TITLE = "m,n,k-game";
	
	private static final int CELL_SIZE = 112, DISC_SIZE = 112;
	private static final String PIECE1 = "X", PIECE2 = "O";
	
	private static final Color PLAYER_COLOR1 = new Color(58, 83, 155), PLAYER_COLOR2 = new Color(214, 69, 65),
							   BCKG_COLOR1 = new Color(232, 236, 241), BCKG_COLOR2 = new Color(189, 195, 199),
							   BCKG_WIN_COLOR = new Color(123, 239, 178);
							   
	
	private int width = 5, height = 5, target = 4;
	
	private Player<MNKGame> player1, player2, currentPlayer;
	private Player<?> winner;
	
	private boolean ready = true;
	
	private Player<?>[][] board;
	
	private JFrame window;
	private JButton[][] buttons;
	
	public MNKGame(Player<MNKGame> player1, Player<MNKGame> player2) {
		
		this.player1 = player1;
		this.player2 = player2;
		currentPlayer = player1;
		createWindow();
		player1.init(this);
		player2.init(this);
		simulateGame();
	}
	
	public MNKGame(int width, int height, int target,
			Player<MNKGame> player1, Player<MNKGame> player2) {
		
		board = new Player<?>[width][height];
		buttons = new JButton[width][height];
		this.width = width;
		this.height = height;
		this.target = target;
		this.player1 = player1;
		this.player2 = player2;
		currentPlayer = player1;
		createWindow();
		player1.init(this);
		player2.init(this);
		simulateGame();
	}
	
	public void placePiece(int x, int y) {
		
		if(winner != null) return;
		
		validateMove(currentPlayer, x, y);
		
		board[x][y] = currentPlayer;
		buttons[x][y].setForeground(
				currentPlayer == player1 ? PLAYER_COLOR1 : PLAYER_COLOR2);
		buttons[x][y].setText(currentPlayer == player1 ? PIECE1 : PIECE2);
		
		currentPlayer = currentPlayer == player1 ? player2 : player1;
		ready = false;
	}
	
	public boolean isEmpty(int x, int y) {
		return board[x][y] == null;
	}
	
	public boolean isFriendly(int x, int y) {
		return board[x][y] == currentPlayer;
	}
	
	public boolean isOpponent(int x, int y) {
		return board[x][y] != null && board[x][y] != currentPlayer;
	}
	
	public boolean isCurrentPlayer(Player<?> player) {
		return player == currentPlayer;
	}
	
	public int getWidth() { return width; }
	
	public int getHeight() { return height; }
	
	public int getTarget() { return target; }
	
	private void simulateGame() {
		
		try {
		
			while((winner = checkWin()) == null) {
				
				Player<MNKGame> player = currentPlayer;
				window.setTitle(TITLE + " - " + currentPlayer + "'s Turn");
				currentPlayer.takeTurn(this);
				
				ready = true;
				
				if(player == currentPlayer)
					throw new IllegalMoveException(player, "Player didn't place a piece.");
			}
			
		} catch(IllegalMoveException e) {
			e.printStackTrace();
			winner = currentPlayer == player1 ? player2 : player1;
		}
		
		window.setTitle(TITLE + " - " + winner + " Wins!");
		System.out.println(winner + " has won.");
	}
	
	private Player<?> checkWin() {
		
		List<List<int[]>> streaks = new LinkedList<>();
		
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				
				if(board[x][y] != null) {
					
					List<int[]> streak;
					
					streak = new ArrayList<>(target);
					for(int i = 0; i < target; i++)
						streak.add(new int[] {x + i, y});
					streaks.add(streak);
					
					streak = new ArrayList<>(target);
					for(int i = 0; i < target; i++)
						streak.add(new int[] {x + i, y + i});
					streaks.add(streak);
					
					streak = new ArrayList<>(target);
					for(int i = 0; i < target; i++)
						streak.add(new int[] {x, y + i});
					streaks.add(streak);
					
					streak = new ArrayList<>(target);
					for(int i = 0; i < target; i++)
						streak.add(new int[] {x - i, y + i});
					streaks.add(streak);
					
				}
			}
		}
		
		for(List<int[]> streak : streaks) {
			
			Player<?> player = board[streak.get(0)[0]][streak.get(0)[1]];
			boolean win = true;
			
			for(int[] pos : streak.subList(1, streak.size())) {
				
				win &= 0 <= pos[0] && pos[0] < width &&
					   0 <= pos[1] && pos[1] < height &&
					   board[pos[0]][pos[1]] == player;
			}
			
			if(win) {
				for(int[] pos : streak) {
					buttons[pos[0]][pos[1]].setBackground(BCKG_WIN_COLOR);
				}
				return player;
			}
		}
		return null;
	}
	
	private void validateMove(Player<MNKGame> player, int x, int y) {
		
		if(board[x][y] != null)
			throw new IllegalMoveException(player,
					"Position (" + x + ", " + y + ") is already taken.");
		
		if(!ready)
			throw new IllegalMoveException(player, "Player placed multiple pieces.");
	}
	
	private void createWindow() {
		
		window = new JFrame(TITLE);
		window.setSize(width * CELL_SIZE, height * CELL_SIZE);
		window.setLayout(new GridLayout(height, width));
		window.setResizable(false);
		window.setLocationByPlatform(true);
		
		for(int y = height - 1; y >= 0; y--) {
			for(int x = 0; x < width; x++) {
				
				JButton button = new JButton();
				button.setBackground((x + y) % 2 == 0 ? BCKG_COLOR1 : BCKG_COLOR2);
				button.setFont(new Font("Arial", Font.BOLD, DISC_SIZE));
				button.setBorder(BorderFactory.createEmptyBorder());
				window.add(button);
				buttons[x][y] = button;
			}
		}
		
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static class MNKGameController implements Player<MNKGame> {
		
		private static final long COOLDOWN = 100;
		private static long previous = System.currentTimeMillis();
		
		private String name = "Controller";
		
		private volatile boolean ready = false;
		
		public MNKGameController() {}
		
		public MNKGameController(String name) { this.name = name; }
		
		@Override
		public void init(MNKGame game) {
			
			for(int x = 0; x < game.width; x++) {
				for(int y = 0; y < game.height; y++) {
					
					int xx = x, yy = y;
					game.buttons[x][y].addActionListener(l -> {
						
						if(game.isCurrentPlayer(this)) {
							
							if(System.currentTimeMillis() - previous > COOLDOWN) {
								game.placePiece(xx, yy);
								previous = System.currentTimeMillis();
								ready = true;
							}
						}
					});
				}
			}
		}
		
		@Override
		public void takeTurn(MNKGame game) {
			ready = false;
			while(!ready) {}
		}
		
		@Override
		public String toString() { return name; }
	}
}