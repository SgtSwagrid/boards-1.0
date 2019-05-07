package games.legacy;

import games.legacy.util.Game;
import games.legacy.util.Player;

public class ScissorsPaperRock implements Game {
	
	public enum Gesture { SCISSORS, PAPER, ROCK }
	
	public final int ROUNDS = 11;
	
	private Player<ScissorsPaperRock> player1, player2;
	private int player1Score = 0, player2Score = 0;
	private Gesture player1Gesture = Gesture.SCISSORS, player2Gesture = Gesture.SCISSORS;
	private Gesture player1Previous = null, player2Previous = null;
	
	public ScissorsPaperRock(Player<ScissorsPaperRock> player1, Player<ScissorsPaperRock> player2) {
		this.player1 = player1;
		this.player2 = player2;
		simulateGame();
	}
	
	public void playGesture(Player<ScissorsPaperRock> player, Gesture gesture) {
		if(player == player1) player1Gesture = gesture;
		else player2Gesture = gesture;
	}
	
	public int getScore(Player<ScissorsPaperRock> player) {
		return player == player1 ? player1Score : player2Score;
	}
	
	public int getOpponentScore(Player<ScissorsPaperRock> player) {
		return player == player1 ? player2Score : player1Score;
	}
	
	public int getRound() {
		return player1Score + player2Score;
	}
	
	public Gesture getOpponentsPreviousMove(Player<ScissorsPaperRock> player) {
		return player == player1 ? player2Previous : player1Previous;
	}
	
	private void simulateGame() {
		
		while((player1Score + player2Score) < ROUNDS) {
			
			player1.takeTurn(this);
			player2.takeTurn(this);
			
			if(player1Gesture == Gesture.SCISSORS && player2Gesture == Gesture.PAPER) player1Score++;
			if(player1Gesture == Gesture.PAPER && player2Gesture == Gesture.SCISSORS) player2Score++;
			if(player1Gesture == Gesture.PAPER && player2Gesture == Gesture.ROCK) player1Score++;
			if(player1Gesture == Gesture.ROCK && player2Gesture == Gesture.PAPER) player2Score++;
			if(player1Gesture == Gesture.ROCK && player2Gesture == Gesture.SCISSORS) player1Score++;
			if(player1Gesture == Gesture.SCISSORS && player2Gesture == Gesture.ROCK) player2Score++;
			
			player1Previous = player1Gesture;
			player2Previous = player2Gesture;
			
			System.out.println(player1Gesture + " vs " + player2Gesture);
		}
		
		System.out.println("Winner: " + (player1Score > player2Score ? player1 : player2) +
				" ( " + player1Score + " : " + player2Score + " ).");
	}
}