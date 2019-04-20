package launch;

import games.castle.Castle;
import games.castle.Castle.CastlePlayer;

public class Main {
	
	public static void main(String[] args) {
		new Castle(new CastlePlayer() {}, new CastlePlayer() {});
	}
}