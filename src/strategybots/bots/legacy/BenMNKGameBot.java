package strategybots.bots.legacy;

import strategybots.games.legacy.MNKGame;
import strategybots.games.legacy.util.Player;

public class BenMNKGameBot implements Player<MNKGame> {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CLASSFIELDS:
 *     m, width of the board
 *     n, height of the board
 *     k, number of pieces in a row to win
 *     board, represents the position and location of
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	private int m;
	private int n;
	private int k;
	private Piece[][] board;
	private Vector[] pieceList;
	private int pieceCount;
	private Vector[] potential;
	private int potentialCount;
	
	@Override
	public void init(MNKGame game) {
		// initialising values
		m = game.getWidth();
		n = game.getHeight();
		k = game.getTarget();
		board = new Piece[m][n];
		pieceList = new Vector[100]; //blurrrrrrrh... hardcoding
		potential = new Vector[100];
		pieceCount = 0;
		potentialCount = 0;
	}
	
	@Override
	public void takeTurn(MNKGame game) {
		int x, y;
		boolean placed = false;
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		updateGameState(game);
		
		for(int i = 0; i < potentialCount; i++) {
			if(game.isEmpty(potential[i].getX(), potential[i].getY()) && !placed) {
					game.placePiece(potential[i].getX(), potential[i].getY());
					break;
			}
		}
		
		if(!placed)
		{
			x = (int) (Math.random() * game.getWidth());
			y = (int) (Math.random() * game.getHeight());
			
			do
			{
				if(game.isEmpty(x, y)) {
					game.placePiece(x, y);
					break;
				}
			} while(!game.isEmpty(x, y));
		}
	}
	
	@Override
	public String toString() { return "Ben's Bot"; }
	
	private void updateGameState(MNKGame game) {
		Vector tempVector = null;
		Piece tempPiece = null;
		int x, y;
		int adjX, adjY;
		
		// update board pieces
		for(int i = 0; i < m; i++) {
			for(int j = 0; j < n; j++) {
				// checking for update
				if(!game.isEmpty(i, j) && board[i][j] == null) {
					System.out.println("adding new piece to the piecelist at (" + i + ", " + j + ")");
					tempVector = new Vector(i, j);
					pieceList[pieceCount] = tempVector;
					board[i][j] = new Piece(tempVector);
					pieceCount++;
					System.out.println(pieceCount);
				}
			}
		}
		
		// update individual pieces
		for(int i = 0; i < pieceCount; i++) {
			tempVector = pieceList[i];
			x = tempVector.getX();
			y = tempVector.getY();
			System.out.println("checking pieces around (" + x + ", " + y + ")");
			
			tempPiece = board[x][y];
			
			if(game.isFriendly(x, y))
			{
				for(int j = 0; j < 3; j++) {
					for(int k = 0; k < 3; k++) {
						// update adjacencies
						adjX = x-1+j;
						adjY = y-1+k;
						if( (adjX < m && adjY < n && adjX >= 0 && adjY >= 0) && !(adjX == x && adjY == y)) {
							System.out.println("@ (" + adjX + " " + adjY + ")");
							if(game.isFriendly(adjX, adjY)) {
								System.out.println(" > adjacent friendly piece at (" + adjX + ", " + adjY + ")");
								// need to prevent double ups
								tempPiece.addAdjacent(board[adjX][adjY]);
								board[adjX][adjY].addAdjacent(tempPiece);
							}
						}
					}
				}
			}
			else if(game.isOpponent(x, y)) {
				for(int j = 0; j < 3; j++) {
					for(int k = 0; k < 3; k++) {
						// update adjacencies
						adjX = x-1+j;
						adjY = y-1+k;
						if( (adjX < m && adjY < n && adjX >= 0 && adjY >= 0) && !(adjX == x && adjY == y)) {
							System.out.println("@ (" + adjX + " " + adjY + ")");
							if(game.isFriendly(adjX, adjY)) {
								System.out.println(" > adjacent friendly piece at (" + adjX + ", " + adjY + ")");
								// need to prevent double ups
								tempPiece.addAdjacent(board[adjX][adjY]);
								board[adjX][adjY].addAdjacent(tempPiece);
							}
						}
					}
				}
			}
		}
	}
	
	private void consecutive() {
		for(int i = 0; i < pieceCount; i++) {
			board[pieceList[i].getX()][pieceList[i].getY()].consecutiveCalc();
		}
		
		// refresh
		for(int i = 0; i < pieceCount; i++) {
			board[pieceList[i].getX()][pieceList[i].getY()].resetVisited();
		}
	}
	
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CLASS NAME: Piece
 * AUTHOR: Ben Belke
 * PURPOSE: Store the number of times a piece has been used
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	private class Piece {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CLASSFIELDS:
 *     usedCount, when cycling through the board for connections will count how many times its been implemented in adjacencies, max. 4
 *     posQuality, number of adjacent pieces of the same colour
 *     negQuality, number of adjacent pieces of different colour
 *     consecutivePieces, adjacent friendly pieces
 *     consecutiveCount, number of adjacent friendly pieces
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private Vector position;
		private int usedCount;
		private int posQuality;
		private int negQuality;
		private boolean visited;
		private boolean hot;
		
		private Piece[] adjacentPieces;
		private int adjacentCount;
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SUBMODULE: alternate constructor
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		public Piece(Vector inPosition) {
			usedCount = 0;
			adjacentPieces = new Piece[8];
			adjacentCount = 0;
			position = inPosition;
			visited = false;
			hot = false;
		}
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SUBMODULE: getters
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private int getUsedCount() { return usedCount; }
		private int getPosQuality() { return posQuality; }
		private int getNegQuality() { return negQuality; }
		private Vector getPosition() { return position; }
		private boolean getHot() { return hot; }
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SUBMODULE: mutators
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private void used() {
			usedCount++;
		}
		
		private void addAdjacent(Piece adjacentPiece)
		{
			// checks for double ups
			boolean found = false;
			for(int i = 0; i < adjacentCount; i++) {
				if((adjacentPieces[i].getPosition()).isEqual(adjacentPiece.getPosition())) {
					System.out.println("piece already recognises its adjacent");
					found = true;
				}
			}
			if(!found)
			{
				adjacentPieces[adjacentCount] = adjacentPiece;
				adjacentCount++;
			}
		}
		
		private void consecutiveCalc() {
			consecutiveCalcRec(this);
		}
		
		private void consecutiveCalcRec(Piece previous) {
			visited = true;
			
			for(int i = 0; i < adjacentCount; i++) {
				if(adjacentPieces[i] != null && !visited) {
					adjacentPieces[i].consecutiveCalcRec(adjacentPieces[i]);
				}
			}
			
			// base case
			if(adjacentCount > 1) {
				for(int i = 0; i < adjacentCount; i++) {
					for(int j = 0; j < adjacentCount; j++) {
						if(adjacentPieces[i] != adjacentPieces[j]) {
							if(Math.abs(adjacentPieces[i].getPosition().getX() + adjacentPieces[i].getPosition().getY() - (adjacentPieces[j].getPosition().getX() + adjacentPieces[j].getPosition().getY())) % 2 == 0) {
								// sets the 3 in a row to hot
								adjacentPieces[i].setHot();
								this.setHot();
								adjacentPieces[j].setHot();
								System.out.println("THIS IS HOT");
							}
						}
					}
				}
			}
			
			if(adjacentCount == 1 && hot && adjacentPieces[adjacentCount].getHot()) {
				potential[potentialCount] = new Vector(adjacentPieces[adjacentCount].getPosition().getY(), adjacentPieces[adjacentCount].getPosition().getX());
				potentialCount++;
			}
			
		}
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SUBMODULE: resetters
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private void  resetCount() {
			usedCount = 0;
		}
		
		private void resetVisited() {
			visited =  false;
		}
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  SUBMODULE: setters
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private void setHot() {
			hot = true;
		}
	}
	
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CLASS NAME: Vector
 * AUTHOR: Ben Belke
 * PURPOSE: Object that contains an (x, y) vector
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	private class Vector {
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CLASSFIELDS:
 *     x, x coordinate
 *     y, y coordinate
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private int x;
		private int y;
		
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SUBMODULE: alternate constructor
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private Vector(int inX, int inY) {
			x = inX;
			y = inY;
		}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SUBMODULE: getters 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		private int getX() { return x; }
		private int getY() { return y; }
		
		private boolean isEqual(Object inObject) {
			boolean equal = false;
			Vector inVector;
			
			if(inObject instanceof Vector){
				inVector = (Vector)inObject;
				if(inVector.getX() == x) {
					if(inVector.getY() == y) {
						equal = true;
					}
				}
			}
			
			return equal;
		}
	}
}