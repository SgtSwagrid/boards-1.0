package games.castle;

import java.util.Arrays;
import java.util.List;

import games.castle.render.Window;

public class Castle {

	private List<CastlePlayer> players;
	
	public Castle(CastlePlayer... players) {
		this.players = Arrays.asList(players);
		start();
	}
	
	private void start() {
		
		Window window = new Window(1200, 900, "Castle Game v1.0");
	}
	
	public interface CastlePlayer {}
}