package strategybots.bots.legacy;

import strategybots.games.legacy.ScissorsPaperRock;
import strategybots.games.legacy.ScissorsPaperRock.Gesture;
import strategybots.games.legacy.util.Player;

public class AdrianRockBot implements Player<ScissorsPaperRock> {
	
	private Gesture prevWinning;
	private int prevOScore, prevScore;
	
	@Override
	public void takeTurn(ScissorsPaperRock game) {

		
		prevWinning = game.getOpponentsPreviousMove(this);
		
		if (prevWinning != null) {
		
			switch((int) (Math.random() * 3)) {
			
				case 0: game.playGesture(this, Gesture.SCISSORS); break;
				case 1: game.playGesture(this, Gesture.PAPER); break;
				case 2: game.playGesture(this, Gesture.ROCK); break;
				
			}
		}
		else
		{
			game.playGesture(this, prevWinning);
		}
	}
	
	@Override
	public String toString() { return "Adrian's Rock off bot"; }
}