package games.castle;

import java.util.Arrays;
import java.util.List;

public class Castle {

	private List<CastlePlayer> players;
	
	public Castle(CastlePlayer... players) {
		this.players = Arrays.asList(players);
	}
	
	public interface CastlePlayer {}
}