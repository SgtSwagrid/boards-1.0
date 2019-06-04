package strategybots.launch;

import strategybots.bots.C4_MCTS;
import strategybots.bots.Swagmax;
import strategybots.bots.SwagmaxC4;
import strategybots.bots.legacy.SwagConnectBot;
import strategybots.games.Amazons;
import strategybots.games.Chess;
import strategybots.games.Chomp;
import strategybots.games.Clobber;
import strategybots.games.Checkers;
import strategybots.games.Checkers.CheckersController;
import strategybots.games.Dots;
import strategybots.games.HyperMNK;
import strategybots.games.Reversi;
import strategybots.games.Amazons.AmazonsController;
import strategybots.games.Chess.ChessController;
import strategybots.games.Chomp.ChompController;
import strategybots.games.Clobber.ClobberController;
import strategybots.games.Dots.DotsController;
import strategybots.games.HyperMNK.Gravity;
import strategybots.games.HyperMNK.HyperMNKController;
import strategybots.games.Reversi.ReversiController;
import strategybots.games.legacy.ConnectFour;
import strategybots.games.legacy.ConnectFour.ConnectFourController;

public class Main {
    
    private static final int HYPER_MNK = 0;
    private static final int AMAZONS = 1;
    private static final int CHOMP = 2;
    private static final int CLOBBER = 3;
    private static final int REVERSI = 4;
    private static final int CHESS = 5;
    private static final int DOTS = 6;
    private static final int CHECKERS =7;
	
    private static int game = AMAZONS;
    
	public static void main(String[] args) {
	    
	    switch(game) {
	        
	        case HYPER_MNK:
	            HyperMNK.create()
    	            .dimensions(7, 6)
    	            .target(4)
    	            .gravity(Gravity.NEGATIVE, 1)
    	            
    	            .player(new SwagmaxC4(2000))
    	            .player(new HyperMNKController())
    	            
    	            
    	            
    	            
    	            // Type 0 indicates god bot, 1 indicates dumb level bot, 2 is for kinda smart but also possible to be dumb
    	            //.player(new C4_MCTS(0, 2000)) // 2000 is the number of thinking ms
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
	            
	        case DOTS:
	            new Dots(6, 6, new DotsController(), new DotsController());
	            break;
	            
	        case CHECKERS:
	            new Checkers(8, 8, 3, new CheckersController(), new CheckersController());
	            break;
	            
	        case 69:
	            new ConnectFour(new SwagConnectBot(3000), new ConnectFourController());
	            break;
	    }
	}
}