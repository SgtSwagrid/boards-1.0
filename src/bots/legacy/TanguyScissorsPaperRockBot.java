package bots.legacy;

import games.legacy.ScissorsPaperRock;
import games.legacy.ScissorsPaperRock.Gesture;
import games.legacy.util.Player;

public class TanguyScissorsPaperRockBot implements Player<ScissorsPaperRock> {
	
	@Override
	public void takeTurn(ScissorsPaperRock game) {
		
		switch((int) (Math.random() * 3)) {
		
			case 0: game.playGesture(this, Gesture.SCISSORS); break;
			case 1: game.playGesture(this, Gesture.PAPER); break;
			case 2: game.playGesture(this, Gesture.ROCK); break;
			
		}
	}
	
	@Override
	public String toString() { return "Tanguy's Bot"; }
}