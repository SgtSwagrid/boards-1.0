package launch;

import games.Amazons;
import games.Amazons.AmazonsController;

public class Main {
	
	public static void main(String[] args) {
	    
	    new Amazons(10, 10,
	            new AmazonsController(),
	            new AmazonsController());
	}
}