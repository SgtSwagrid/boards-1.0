package launch;

import games.Amazons;
import games.Amazons.AmazonsController;
import games.Chomp;
import games.Chomp.ChompController;
import games.HyperMNK;
import games.HyperMNK.HyperMNKController;
import games.HyperMNK.Gravity;

public class Main {
    
    private static final int HYPER_MNK = 0;
    private static final int AMAZONS = 1;
    private static final int CHOMP = 2;
	
    private static int game = AMAZONS;
    
	public static void main(String[] args) {
	    
	    switch(game) {
	        
	        case HYPER_MNK:
	            HyperMNK.create()
    	            .dimensions(7, 6)
    	            .target(4)
    	            .gravity(Gravity.NEGATIVE, 1)
    	            .player(new HyperMNKController())
    	            .player(new HyperMNKController())
    	            .start();
	            break;
	            
	        case AMAZONS:
	            new Amazons(8, 8, new AmazonsController(), new AmazonsController());
	            break;
	            
	        case CHOMP:
	            new Chomp(8, 8, new ChompController(), new ChompController());
	            break;
	    }
	}
}