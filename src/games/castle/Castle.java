package games.castle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import games.castle.event.Event;
import games.castle.event.InputEvent.MouseEvent.MouseButtonEvent.ClickEvent;
import games.castle.render.Colour;
import games.castle.render.Tile;
import games.castle.render.TileRenderer;
import games.castle.render.Window;

public class Castle {

	private List<CastlePlayer> players;
	
	private CastlePlayer currentPlayer;
	private int currentPlayerId;
	private Optional<CastlePlayer> winner = Optional.empty();
	
	private List<Troop> selectedTroops = new LinkedList<>();
	
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
	
	public CastlePlayer getCurrentPlayer() {
		return currentPlayer;
	}
	
	public List<Troop> getSelectedTroops() {
		return selectedTroops;
	}
	
	public void addSelectedTroop(Troop troop) {
		if(!selectedTroops.isEmpty() && selectedTroops.get(0).getTile() != troop.getTile())
			clearSelectedTroops();
		troop.select();
		selectedTroops.add(troop);
	}
	
	public void removeSelectedTroop(Troop troop) {
		selectedTroops.remove(troop);
		troop.unselect();
	}
	
	public void clearSelectedTroops() {
		selectedTroops.forEach(Troop::unselect);
		selectedTroops.clear();
	}
	
	private void start() {
		
		Window window = new Window(1050, 1050, "Castle Game v1.0");
		window.addRenderer(TileRenderer.INSTANCE);
		window.setColour(Colour.TEAL);
		
		createBoard();
		
		while(!winner.isPresent()) {
			
			currentPlayer = players.get(currentPlayerId);
			
			
			currentPlayerId = currentPlayerId % players.size() + 1;
			
			break; //temporary
		}
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
		
		new Troop(this, players.get(0), board[1][0]);
	}
	
	public interface CastlePlayer {
		
		
	}
}