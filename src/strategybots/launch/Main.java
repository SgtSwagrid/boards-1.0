package strategybots.launch;

import strategybots.bots.SwagmaxC4;
import strategybots.games.Amazons;
import strategybots.games.Chess;
import strategybots.games.Chomp;
import strategybots.games.Clobber;
import strategybots.games.Checkers;
import strategybots.games.Checkers.CheckersController;
import strategybots.games.DotsAndBoxes;
import strategybots.games.Reversi;
import strategybots.games.Amazons.AmazonsController;
import strategybots.games.Chess.ChessController;
import strategybots.games.Chomp.ChompController;
import strategybots.games.Clobber.ClobberController;
import strategybots.games.ConnectFour;
import strategybots.games.ConnectFour.ConnectFourController;
import strategybots.games.DotsAndBoxes.DotsController;
import strategybots.games.Reversi.ReversiController;
import strategybots.games.legacy.HyperMNK;
import strategybots.games.legacy.HyperMNK.Gravity;
import strategybots.games.legacy.HyperMNK.HyperMNKController;
import strategybots.games.TicTacToe;
import strategybots.games.TicTacToe.TicTacToeController;;

public class Main {
    
    private static final int HYPER_MNK = 0;
    private static final int AMAZONS = 1;
    private static final int CHOMP = 2;
    private static final int CLOBBER = 3;
    private static final int REVERSI = 4;
    private static final int CHESS = 5;
    private static final int DOTS = 6;
    private static final int CHECKERS = 7;
    private static final int TICTACTOE = 8;
    private static final int CONNECTFOUR = 9;
	
    private static final int GAME = CONNECTFOUR;
    
	public static void main(String[] args) {
	    
	    switch(GAME) {
	        
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
	            
	        case AMAZONS: new Amazons(new AmazonsController(), new AmazonsController()); break;
            case CHECKERS: new Checkers(new CheckersController(), new CheckersController()); break;
            case CHESS: new Chess(new ChessController(), new ChessController()); break;
	        case CHOMP: new Chomp(new ChompController(), new ChompController()); break;
	        case CLOBBER: new Clobber(new ClobberController(), new ClobberController()); break;
            case DOTS: new DotsAndBoxes(new DotsController(), new DotsController()); break;
	        case REVERSI: new Reversi(new ReversiController(), new ReversiController()); break;
	        case TICTACTOE: new TicTacToe(new TicTacToeController(), new TicTacToeController()); break;
	        case CONNECTFOUR: new ConnectFour(new ConnectFourController(), new ConnectFourController()); break;
	    }
	}
}