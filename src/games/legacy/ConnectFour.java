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

public class ConnectFour implements Game {
	
	private static String TITLE = "Connect Four";
	
	private static final int CELL_SIZE = 112, DISC_SIZE = 140;
	private static final String PIECE = "�?";
	
	private static final Color PLAYER_COLOR1 = new Color(58, 83, 155), PLAYER_COLOR2 = new Color(214, 69, 65),
							   BCKG_COLOR1 = new Color(232, 236, 241), BCKG_COLOR2 = new Color(189, 195, 199),
							   BCKG_WIN_COLOR = new Color(123, 239, 178);
							   
	
	public final int WIDTH = 7, HEIGHT = 6, TARGET = 4;
	
	private Player<ConnectFour> player1, player2, currentPlayer;
	private Player<?> winner;
	
	private boolean ready = true;
	
	private Player<?>[][] board = new Player<?>[WIDTH][HEIGHT];
	
	private JFrame window;
	private JButton[][] buttons = new JButton[WIDTH][HEIGHT];
	
	public ConnectFour(Player<ConnectFour> player1, Player<ConnectFour> player2) {
		
		this.player1 = player1;
		this.player2 = player2;
		currentPlayer = player1;
		createWindow();
		player1.init(this);
		player2.init(this);
		simulateGame();
	}
	
	public void placePiece(int column) {
		
		if(winner != null) return;
		
		validateMove(currentPlayer, column);
		
		for(int y = 0; y < HEIGHT; y++) {
			
			if(board[column][y] == null) {
				board[column][y] = currentPlayer;
				buttons[column][y].setForeground(
						currentPlayer == player1 ? PLAYER_COLOR1 : PLAYER_COLOR2);
				buttons[column][y].setText(PIECE);
				break;
			}
		}
		currentPlayer = currentPlayer == player1 ? player2 : player1;
		ready = false;
	}
	
	public Player<?> getPiece(int x, int y) { return board[x][y]; }
	
	public Player<ConnectFour> getCurrentPlayer() { return currentPlayer; }
	
	private void simulateGame() {
		
		try {
		
			while((winner = checkWin()) == null) {
				
				Player<ConnectFour> player = currentPlayer;
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
		
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < HEIGHT; y++) {
				
				if(board[x][y] != null) {
					
					List<int[]> streak;
					
					streak = new ArrayList<>(TARGET);
					for(int i = 0; i < TARGET; i++)
						streak.add(new int[] {x + i, y});
					streaks.add(streak);
					
					streak = new ArrayList<>(TARGET);
					for(int i = 0; i < TARGET; i++)
						streak.add(new int[] {x + i, y + i});
					streaks.add(streak);
					
					streak = new ArrayList<>(TARGET);
					for(int i = 0; i < TARGET; i++)
						streak.add(new int[] {x, y + i});
					streaks.add(streak);
					
					streak = new ArrayList<>(TARGET);
					for(int i = 0; i < TARGET; i++)
						streak.add(new int[] {x - i, y + i});
					streaks.add(streak);
					
				} else continue;
			}
		}
		
		for(List<int[]> streak : streaks) {
			
			Player<?> player = board[streak.get(0)[0]][streak.get(0)[1]];
			boolean win = true;
			
			for(int[] pos : streak.subList(1, streak.size())) {
				
				win &= 0 <= pos[0] && pos[0] < WIDTH &&
					   0 <= pos[1] && pos[1] < HEIGHT &&
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
	
	private void validateMove(Player<ConnectFour> player, int column) {
		
		if(board[column][HEIGHT - 1] != null)
			throw new IllegalMoveException(player, "Column " + column + " is full.");
		
		if(!ready)
			throw new IllegalMoveException(player, "Player placed multiple pieces.");
	}
	
	private void createWindow() {
		
		window = new JFrame(TITLE);
		window.setSize(WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);
		window.setLayout(new GridLayout(HEIGHT, WIDTH));
		window.setResizable(false);
		window.setLocationByPlatform(true);
		
		for(int y = HEIGHT - 1; y >= 0; y--) {
			for(int x = 0; x < WIDTH; x++) {
				
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
	
	public static class ConnectFourController implements Player<ConnectFour> {
		
		private static final long COOLDOWN = 100;
		private static long previous = System.currentTimeMillis();
		
		private String name = "Controller";
		
		private volatile boolean ready = false;
		
		public ConnectFourController() {}
		
		public ConnectFourController(String name) { this.name = name; }
		
		@Override
		public void init(ConnectFour game) {
			
			for(int x = 0; x < game.WIDTH; x++) {
				for(int y = 0; y < game.HEIGHT; y++) {
					
					int column = x;
					game.buttons[x][y].addActionListener(l -> {
						
						if(game.getCurrentPlayer() == this) {
							
							if(System.currentTimeMillis() - previous > COOLDOWN) {
								game.placePiece(column);
								previous = System.currentTimeMillis();
								ready = true;
							}
						}
					});
				}
			}
		}
		
		@Override
		public void takeTurn(ConnectFour game) {
			ready = false;
			while(!ready) {}
		}
		
		@Override
		public String toString() { return name; }
	}
}