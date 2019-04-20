package games.castle;

import java.util.Arrays;
import java.util.List;

import games.castle.render.Colour;
import games.castle.render.Tile;
import games.castle.render.TileRenderer;
import games.castle.render.Window;

public class Castle {

	private List<CastlePlayer> players;
	
	public Castle(CastlePlayer... players) {
		this.players = Arrays.asList(players);
		start();
	}
	
	private void start() {
		
		Window window = new Window(1200, 900, "Castle Game v1.0");
		window.addRenderer(TileRenderer.INSTANCE);
		window.setColour(Colour.TEAL);
		
		Tile t = new Tile();
		
	}
	
	public interface CastlePlayer {}
}