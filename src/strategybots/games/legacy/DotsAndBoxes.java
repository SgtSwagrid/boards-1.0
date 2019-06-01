package strategybots.games.legacy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

import strategybots.games.legacy.util.Game;
import strategybots.games.legacy.util.Player;

public class DotsAndBoxes implements Game {
	
	public enum Orientation { HORIZONTAL, VERTICAL }
	
	public enum Side { LEFT, RIGHT, TOP, BOTTOM }
	
	private static final String TITLE = "Dots and Boxes";
	
	private static final int CELL_SIZE = 104, LINE_WIDTH = 16, PADDING = 32, LETTER_SIZE = 64;
	
	private static final Color PLAYER_COLOR1 = new Color(58, 83, 155), PLAYER_COLOR2 = new Color(214, 69, 65),
							   LINE_COLOR1 = new Color(210, 215, 211), LINE_COLOR2 = new Color(46, 49, 49),
							   BCKG_COLOR = new Color(232, 236, 241);
	
	public final int WIDTH = 5, HEIGHT = 5;
	
	private Player<DotsAndBoxes> player1, player2, currentPlayer;
	private Player<?> winner;
	
	private int player1Score = 0, player2Score = 0;
	
	private boolean ready = true;
	
	private Player<?>[][] board = new Player<?>[HEIGHT][WIDTH];
	private boolean[][] hBoard = new boolean[HEIGHT][WIDTH + 1];
	private boolean[][] vBoard = new boolean[HEIGHT + 1][WIDTH];
	
	private JFrame window;
	private JButton[][] boxes = new JButton[WIDTH][HEIGHT];
	private JButton[][] hLines = new JButton[WIDTH][HEIGHT + 1];
	private JButton[][] vLines = new JButton[HEIGHT + 1][WIDTH];
	
	public DotsAndBoxes(Player<DotsAndBoxes> player1, Player<DotsAndBoxes> player2) {
		
		this.player1 = player1;
		this.player2 = player2;
		currentPlayer = player1;
		createWindow();
		player1.init(this);
		player2.init(this);
		simulateGame();
	}
	
	public boolean drawLine(int x, int y, Orientation o) {
		
		if(winner != null) return false;
		
		validateMove(currentPlayer, x, y, o);
		
		JButton line = null;
		if(o == Orientation.HORIZONTAL) {
			hBoard[x][y] = true;
			line = hLines[x][y];
		} else if(o == Orientation.VERTICAL) {
			vBoard[x][y] = true;
			line = vLines[x][y];
		}
		
		line.setBackground(LINE_COLOR2);
		
		boolean capture = false;
		
		for(int[] neighbour : getNeighbours(x, y, o)) {
			
			if(isSurrounded(neighbour[0], neighbour[1])) {
				
				board[neighbour[0]][neighbour[1]] = currentPlayer;
				boxes[neighbour[0]][neighbour[1]].setBackground(
						currentPlayer == player1 ? PLAYER_COLOR1 : PLAYER_COLOR2);
				boxes[neighbour[0]][neighbour[1]].setText(
						currentPlayer.toString().charAt(0) + "");
				
				if(currentPlayer == player1) player1Score++;
				else player2Score++;
				window.setTitle(TITLE + " - " + currentPlayer + "'s Turn - "
						+ player1Score + " : " + player2Score);
				
				capture = true;
			}
		}
		
		if(player1Score + player2Score == WIDTH * HEIGHT)
			capture = false;
		
		if(!capture) {
			currentPlayer = currentPlayer == player1 ? player2 : player1;
			ready = false;
		}
		
		return capture;
	}
	
	public boolean drawLine(int x, int y, Side s) {
		
		switch(s) {
			case LEFT: return drawLine(x, y, Orientation.VERTICAL);
			case RIGHT: return drawLine(x + 1, y, Orientation.VERTICAL);
			case TOP: return drawLine(x, y + 1, Orientation.HORIZONTAL);
			case BOTTOM: return drawLine(x, y, Orientation.HORIZONTAL);
			default: return false;
		}
	}
	
	public boolean hasLine(int x, int y, Orientation o) {
		return (o == Orientation.HORIZONTAL && hBoard[x][y]) ||
			   (o == Orientation.VERTICAL && vBoard[x][y]);
	}
	
	public boolean hasLine(int x, int y, Side s) {
		
		switch(s) {
			case LEFT: return hasLine(x, y, Orientation.VERTICAL);
			case RIGHT: return hasLine(x + 1, y, Orientation.VERTICAL);
			case TOP: return hasLine(x, y + 1, Orientation.HORIZONTAL);
			case BOTTOM: return hasLine(x, y, Orientation.HORIZONTAL);
			default: return false;
		}
	}
	
	public Player<DotsAndBoxes> getCurrentPlayer() { return currentPlayer; }
	
	public int getScore(Player<DotsAndBoxes> player) {
		return player == player1 ? player1Score : player2Score;
	}
	
	public int getOpponentScore(Player<DotsAndBoxes> player) {
		return player == player1 ? player2Score : player1Score;
	}
	
	public Player<?> getOwner(int x, int y) {
		return board[x][y];
	}
	
	public boolean isOccupied(int x, int y) { return board[x][y] != null; }
	
	public boolean isFriendly(Player<DotsAndBoxes> player, int x, int y) {
		return board[x][y] == player;
	}
	
	public boolean isOpponent(Player<DotsAndBoxes> player, int x, int y) {
		return board[x][y] != null && board[x][y] != player;
	}
	
	private boolean isSurrounded(int x, int y) {
		return hasLine(x, y, Side.LEFT) && hasLine(x, y, Side.RIGHT) &&
			   hasLine(x, y, Side.TOP)  && hasLine(x, y, Side.BOTTOM);
	}
	
	private int[][] getNeighbours(int x, int y, Orientation o) {
		
		if(o == Orientation.HORIZONTAL) {
			if(y == 0) return new int[][] {{x, y}};
			else if(y == HEIGHT) return new int[][] {{x, y - 1}};
			else return new int[][] {{x, y - 1}, {x, y}};
			
		} else if(o == Orientation.VERTICAL) {
			if(x == 0) return new int[][] {{x, y}};
			else if(x == WIDTH) return new int[][] {{x - 1, y}};
			else return new int[][] {{x - 1, y}, {x, y}};
			
		} else return null;
	}
	
	private void simulateGame() {
		
		try {
			
			while((winner = checkWin()) == null) {
				
				Player<DotsAndBoxes> player = currentPlayer;
				window.setTitle(TITLE + " - " + currentPlayer + "'s Turn - "
						+ player1Score + " : " + player2Score);
				currentPlayer.takeTurn(this);
				
				ready = true;
				
				if(player == currentPlayer)
					throw new IllegalMoveException(player, "Player didn't draw enough lines.");
			}
			
		} catch(IllegalMoveException e) {
			e.printStackTrace();
			winner = currentPlayer == player1 ? player2 : player1;
		}
		
		window.setTitle(TITLE + " - " + winner + " Wins! - "
				+ player1Score + " : " + player2Score);
		System.out.println(winner + " has won.");
	}
	
	private Player<?> checkWin() {
		return player1Score + player2Score == WIDTH * HEIGHT ?
				player1Score > player2Score ? player1 : player2 : null;
	}
	
	private void validateMove(Player<DotsAndBoxes> player, int x, int y, Orientation o) {
		
		if(hasLine(x, y, o))
			throw new IllegalMoveException(player, "There is already a "
					+ o + " line at (" + x + ", " + y + ").");
		
		if(!ready)
			throw new IllegalMoveException(player, "Player drew too many lines.");
	}
	
	@SuppressWarnings("serial")
	private void createWindow() {
		
		int windowWidth = WIDTH * (CELL_SIZE + LINE_WIDTH) + LINE_WIDTH + PADDING + 32;
		int windowHeight = HEIGHT * (CELL_SIZE + LINE_WIDTH) + LINE_WIDTH + PADDING + 40;
		
		window = new JFrame(TITLE) {
			public Insets getInsets() {
				return new Insets(3 * PADDING / 2, PADDING, PADDING / 2, PADDING);
			}
		};
		
		window.setSize(windowWidth, windowHeight);
		window.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		window.setBackground(BCKG_COLOR);
		window.setResizable(false);
		window.setLocationByPlatform(true);
		
		for(int y = 2 * HEIGHT; y >= 0; y--) {
			for(int x = 0; x <= 2 * WIDTH; x++) {
				
				if(x % 2 == 1 && y % 2 == 0) {
					
					JButton hLine = new JButton();
					hLine.setPreferredSize(new Dimension(CELL_SIZE, LINE_WIDTH));
					hLine.setBackground(LINE_COLOR1);
					hLine.setBorder(BorderFactory.createEmptyBorder());
					window.add(hLine);
					hLines[x / 2][y / 2] = hLine;
					
				} else if(x % 2 == 0 && y % 2 == 1) {
					
					JButton vLine = new JButton();
					vLine.setPreferredSize(new Dimension(LINE_WIDTH, CELL_SIZE));
					vLine.setBackground(LINE_COLOR1);
					vLine.setBorder(BorderFactory.createEmptyBorder());
					window.add(vLine);
					vLines[x / 2][y / 2] = vLine;
					
				} else if(x % 2 == 1 && y % 2 == 1) {
					
					JButton box = new JButton();
					box.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
					box.setBackground(BCKG_COLOR);
					box.setForeground(BCKG_COLOR);
					box.setFont(new Font("Arial", Font.BOLD, LETTER_SIZE));
					box.setBorder(BorderFactory.createEmptyBorder());
					window.add(box);
					boxes[x / 2][y / 2] = box;
					
				} else if(x % 2 == 0 && y % 2 == 0) {
					
					JButton dot = new JButton();
					dot.setPreferredSize(new Dimension(LINE_WIDTH, LINE_WIDTH));
					dot.setBackground(LINE_COLOR2);
					dot.setBorder(BorderFactory.createEmptyBorder());
					window.add(dot);
					
				}
			}
		}
		
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static class DotsAndBoxesController implements Player<DotsAndBoxes> {
		
		private static final long COOLDOWN = 100;
		private static long previous = System.currentTimeMillis();
		
		private String name = "Controller";
		
		private volatile boolean ready = false;
		
		public DotsAndBoxesController() {}
		
		public DotsAndBoxesController(String name) { this.name = name; }
		
		@Override
		public void init(DotsAndBoxes game) {
			
			for(int i = 0; i < game.WIDTH; i++) {
				for(int j = 0; j <= game.HEIGHT; j++) {
					
					int x = i, y = j;
					
					game.hLines[x][y].addActionListener(l -> {
						
						if(game.getCurrentPlayer() == this) {
							
							if(System.currentTimeMillis() - previous > COOLDOWN) {
								ready = !game.drawLine(x, y, Orientation.HORIZONTAL);
								previous = System.currentTimeMillis();
							}
						}
					});
				}
			}
			
			for(int i = 0; i <= game.WIDTH; i++) {
				for(int j = 0; j < game.HEIGHT; j++) {
					
					int x = i, y = j;
					
					game.vLines[x][y].addActionListener(l -> {
						
						if(game.getCurrentPlayer() == this) {
							
							if(System.currentTimeMillis() - previous > COOLDOWN) {
								ready = !game.drawLine(x, y, Orientation.VERTICAL);
								previous = System.currentTimeMillis();
							}
						}
					});
				}
			}
		}
		
		@Override
		public void takeTurn(DotsAndBoxes game) {
			ready = false;
			while(!ready) {}
		}
		
		@Override
		public String toString() { return name; }
	}
}