package games.castle;

import java.util.Arrays;
import java.util.List;

import games.castle.render.Colour;
import games.castle.render.Tile;
import games.castle.render.TileRenderer;
import games.castle.render.Window;

public class Castle {

	private List<CastlePlayer> players;
	
	private Colour[] colours = new Colour[] {
			Colour.RED, Colour.BLUE, Colour.GREEN, Colour.YELLOW
	};
	
	private int boardSize;
	
	private CastleTile[][] board;
	
	public Castle(CastlePlayer... players) {
		this.players = Arrays.asList(players);
		boardSize = players.length <= 4 ? 6 : 7;
		start();
	}
	
	public int getBoardSize() { return boardSize; }
	
	public Colour getColour(CastlePlayer player) {
		return colours[players.indexOf(player)];
	}
	
	private void start() {
		
		Window window = new Window(1050, 1050, "Castle Game v1.0");
		window.addRenderer(TileRenderer.INSTANCE);
		window.setColour(Colour.TEAL);
		
		createBoard();
		
	}
	
	private void createBoard() {
		
		board = new CastleTile[boardSize][boardSize];
		
		for(int x = 0; x < boardSize; x++) {
			for(int y = 0; y < boardSize; y++) {
				
				board[x][y] = new CastleTile(this, x, y);
			}
		}
		
		new Troop(this, players.get(0), board[0][0]);
		new Troop(this, players.get(0), board[0][0]);
		new Troop(this, players.get(1), board[0][0]);
		new Troop(this, players.get(0), board[0][0]);
		
		new Troop(this, players.get(1), board[0][0]);
		new Troop(this, players.get(1), board[0][0]);
	}
	
	public interface CastlePlayer {
		
		
	}
}