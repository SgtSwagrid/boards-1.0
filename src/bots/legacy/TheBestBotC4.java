package bots.legacy;

import games.legacy.ConnectFour;
import games.legacy.util.Player;

public class TheBestBotC4 implements Player<ConnectFour> {

	int placed = 0;
	int goingFirst = 2; //check the turn order
	int moveNumber = 0;
	
	@Override
	public void takeTurn(ConnectFour game) {
		
		int placed = 0;
		//nice little delay here
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		moveNumber++; //keep track of move number
		
		//determines the turn order
		if(goingFirst > 1) {
			goingFirst = turnOrder(game);
		}
		
		//int[] columnsFull = columnFull(game);
		
		
		if(placed == 0) {
			placed = checkEvilThreeHorz(game);
		}
		if(placed == 0) {
			placed = checkThreeHorz(game);
		}
		if(placed == 0) {
			game.placePiece((int) (Math.random() * game.WIDTH));
		}
		
		//Player<?> p = game.getPiece(3, 0);
		
		//if(p != null && p != this);
		
	}
	
	private int turnOrder(ConnectFour game) {
		
		int First = 0;
		
		for(int i = 0; i < 7; i++) {
			
			Player<?> p = game.getPiece(i, 0);
			
			if(p != null) {
				First = First + 1;	
			}
		}
			
		if(First != 0) {
			First = 0;
		}
		else {
			First = 1;
		}
		
		return(First);
	}
	
	private int[] columnFull(ConnectFour game) {

		
		int[] columnsFull = new int[7];
		
		//checks for any full columns
		for(int i = 0; i < 7; i++) {
			
			if(game.getPiece(i, 5) != null){
				columnsFull[i] = 1;
			}
			else {
				columnsFull[i] = 0;
			}
		}
		
		return(columnsFull);
	}
	
	//next few for three detection
	private int checkThreeHorz(ConnectFour game) {
		
		int[] columnsFull = columnFull(game);
		
		int placed = 0;
		
		for(int i = 1; i < 5; i++) {
			for(int j = 0; j < 6; j++){
				if(game.getPiece(i, j) == this && game.getPiece(i+1, j) == this && game.getPiece(i-1, j) == this) {
					System.out.println("Three found!");
					//check if its a win on the left
					if(j > 0) { // not on bottom row
						if(i > 1 && game.getPiece(i-2, j) == null && game.getPiece(i-2, j-1) != null) {
						
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i-2);
								placed = 1;
							}
						}
					}
					else {
						if(i > 1 && game.getPiece(i-2, j) == null) {
							
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i-2);
								placed = 1;
							}
							
						}
					}
					//check if its a win on the right
					if(j > 0) { // not on bottom row
						if(i < 6 && game.getPiece(i+2, j) == null && game.getPiece(i+2, j-1) != null) {
						
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i+2);
								placed = 1;
							}
							
						}
					}
					else {
						if(i > 6 && game.getPiece(i+2, j) == null) {
							
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i+2);
								placed = 1;
							}
							
						}
					}
				}
			}
		}
		return(placed);
	}
	
private int checkEvilThreeHorz(ConnectFour game) {
		
		int[] columnsFull = columnFull(game);
	
		int placed = 0;
		
		for(int i = 1; i < 6; i++) {
			for(int j = 0; j < 6; j++){
				if(game.getPiece(i, j) != this && game.getPiece(i+1, j) != this && game.getPiece(i-1, j) != this && game.getPiece(i, j) != null && game.getPiece(i+1, j) != null && game.getPiece(i-1, j) != null) {
					//check if its an oops on the left
					if(j > 0) { // not on bottom row
						if(i > 1 && game.getPiece(i-2, j) == null && game.getPiece(i-2, j-1) != null) {
							System.out.println("Dangerous evil three found!");
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i-2);
								placed = 1;
							}
						}
					}
					else {
						if(i > 1 && game.getPiece(i-2, j) == null) {
							System.out.println("Dangerous evil three found!");
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i-2);
								placed = 1;
							}
							
						}
					}
					//check if its a win on the right
					if(j > 0) { // not on bottom row
						if(i < 5 && game.getPiece(i+2, j) == null && game.getPiece(i+2, j-1) != null) {
							System.out.println("Dangerous evil three found!");
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i+2);
								placed = 1;
							}
							
						}
					}
					else {
						if(i > 6 && game.getPiece(i+2, j) == null) {
							System.out.println("Dangerous evil three found!");
							if(placed == 0 && columnsFull[i] == 0) {
								game.placePiece(i+2);
								placed = 1;
							}
							
						}
					}
				}
			}
		}
		return(placed);
	}
	
	private void checkThreeVert(ConnectFour game) {
		
		for(int i = 0; i < 7; i++) {
			for(int j = 1; j < 5; i++) {
				if(game.getPiece(i, j) == this && game.getPiece(i, j-1) == this && game.getPiece(i, j+1) == this) {
					//if(game.getPiece())
				}
			}
		}
		
	}
	
	@Override
	public String toString() { return "Best Bot"; }
	
}
