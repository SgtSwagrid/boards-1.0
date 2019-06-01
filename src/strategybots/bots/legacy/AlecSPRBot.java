package strategybots.bots.legacy;

import strategybots.games.legacy.ScissorsPaperRock;
import strategybots.games.legacy.ScissorsPaperRock.Gesture;
import strategybots.games.legacy.util.Player;

public class AlecSPRBot implements Player<ScissorsPaperRock> {
	
	@Override
	public void takeTurn(ScissorsPaperRock game) {
		
		switch((int) (Math.random() * 3)) {
		
			case 0: game.playGesture(this, Gesture.SCISSORS); break;
			case 1: game.playGesture(this, Gesture.PAPER); break;
			case 2: game.playGesture(this, Gesture.ROCK); break;
			
		}
	}
	
	@Override
	public String toString() { return "SwagBot"; }
}