package bots.legacy;

import games.legacy.ConnectFour;
import games.legacy.util.Player;

public class BigBoiChecker implements Player<ConnectFour> {
	

	@Override
	public void takeTurn(ConnectFour game) {
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

//Check Up
		for (int q=0; q <= 5; q++){
			for(int i = 0; i < 6; i++) {
			   Player<?> p = game.getPiece(i,q );			//1
			   if(p != null && p != this) {	
				  p = game.getPiece(i, q+1);				//2
				  if(p != null && p != this) {
					  p = game.getPiece(i, q+2);			//3
					  if(p != null && p != this) {
						  p = game.getPiece(i, game.HEIGHT);
						  if(p == null) {
							  game.placePiece((int) (i));
						  }
							//PLACE PIECE AT [ (i+5,i)
						 
					   }
				   }
				  
			  }
			}
		}
//Checks Diagonal Left
for (int q=0; q <= 5; q++){
	for(int i = 6; i > 2; i--) {
		   Player<?> p = game.getPiece(i, q);			//1
		   if(p != null && p != this) {	
			  p = game.getPiece(i-1, q+1);				//2
			  if(p != null && p != this) {
				  p = game.getPiece(i-2, q+2);			//3
				  if(p != null && p != this) {
					  p = game.getPiece(i-3, q+2);
					  if(p != null) {
						  p = game.getPiece(i-1, game.HEIGHT);
						  if(p == null) {
						  game.placePiece((int) (i-1));
					  
						//PLACE PIECE AT [ (i+5,i)
					  }
				  }
			  }
		  
		   }
	}

}
}
//Checks Diagonal Right
for (int q=0; q <= 5; q++){
	for(int i = 0; i < 4; i++) {
		   Player<?> p = game.getPiece(i, i);			//1
		   if(p != null && p != this) {	
			  p = game.getPiece(i+1, i+1);				//2
			  if(p != null && p != this) {
				  p = game.getPiece(i+2, i+2);			//3
				  if(p != null && p != this) {
					  p = game.getPiece(i+2, i+3);
					  if(p != null) {
						  p = game.getPiece(i, game.HEIGHT);
						  if(p == null) {
							  game.placePiece((int) (i));
						  game.placePiece((int) (i+1));
					  
						//PLACE PIECE AT [ (i+5,i)
					  }
				  }
			  }
		  
		   }
	}		
}	
}
//Checks right
for (int q=0; q <= 5; q++){
		for(int i = 0; i <= 6; i++) {
			   Player<?> p = game.getPiece(i, q);			//1
			   if(p != null && p != this) {	
				  p = game.getPiece(i+1, q);				//2
				  if(p != null && p != this) {
					  p = game.getPiece(i+2, q);			//3
					  if(p != null && p != this) {
						  p = game.getPiece(i, game.HEIGHT);
						  if(p == null) {
							  game.placePiece((int) (i));
						  game.placePiece((int) (i+1));
						  
							//PLACE PIECE AT [ (i+5,i)
						 
					   }
				   }
				  
			  }
			}
}
}
//Checks left
for (int q=0; q <= 5; q++){

		for(int i = 6; i > 3; i--) {
		   Player<?> p = game.getPiece(i,q );			//1
		   if(p != null && p != this) {	
			  p = game.getPiece(i-1, q);				//2
			  if(p != null && p != this) {
				  p = game.getPiece(i-2, q);			//3
				  if(p != null && p != this) {
					  p = game.getPiece(i, game.HEIGHT);
					  if(p == null) {
						  game.placePiece((int) (i));
					  game.placePiece((int) (i-1));
					  
						//PLACE PIECE AT [ (i+5,i)
					 
				 }
			   }
			  
		   	}
		}
		}
}
game.placePiece((int) (Math.random() * game.WIDTH));
		
		
	}
	
	@Override
	public String toString() { return "Ryan Bot"; }
}