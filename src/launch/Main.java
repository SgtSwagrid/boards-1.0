package launch;

import bots.C4_MCTS;
import games.Amazons;
import games.Amazons.AmazonsController;
import games.HyperMNK;
import games.HyperMNK.HyperMNKController;
import games.HyperMNK.Gravity;

public class Main {
	
	public static void main(String[] args) {
	    
	    /*HyperMNK.create()
    	    .dimensions(7, 6)
    	    .target(4)
    	    .gravity(Gravity.NEGATIVE, 1)
    	    .player(new HyperMNKController())
    	    .player(new C4_MCTS())
    	    .start();//*/
	    
	    new Amazons(10, 10, new AmazonsController(), new AmazonsController());
	}
}