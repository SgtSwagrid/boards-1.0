package launch;

import bots.legacy.SwagConnectBot;
import games.Amazons;
import games.Amazons.AmazonsController;
import games.Chess;
import games.Chess.ChessController;
import games.Chomp;
import games.Chomp.ChompController;
import games.Clobber;
import games.Clobber.ClobberController;
import games.HyperMNK;
import games.HyperMNK.HyperMNKController;
import games.Reversi;
import games.Reversi.ReversiController;
import games.legacy.ConnectFour;
import games.legacy.ConnectFour.ConnectFourController;
import games.HyperMNK.Gravity;

public class Main {
    
    private static final int HYPER_MNK = 0;
    private static final int AMAZONS = 1;
    private static final int CHOMP = 2;
    private static final int CLOBBER = 3;
    private static final int REVERSI = 4;
    private static final int CHESS = 5;
	
    private static int game = CLOBBER;
    
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
	            new Amazons(10, 10, new AmazonsController(), new AmazonsController());
	            break;
	            
	        case CHOMP:
	            new Chomp(6, 6, new ChompController(), new ChompController());
	            break;
	            
	        case CLOBBER:
	            new Clobber(5, 6, new ClobberController(), new ClobberController());
	            break;
	            
	        case REVERSI:
	            new Reversi(8, 8, new ReversiController(), new ReversiController());
	            break;
	            
	        case CHESS:
	            new Chess(new ChessController(), new ChessController());
	            break;
	            
	        case 69:
	            new ConnectFour(new SwagConnectBot(3000), new ConnectFourController());
	            break;
	    }
	}
}