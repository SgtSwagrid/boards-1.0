package strategybots.launch;

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
import strategybots.games.Pentago;
import strategybots.games.Pentago.PentagoController;
import strategybots.games.Reversi.ReversiController;
import strategybots.games.TicTacToe;
import strategybots.games.TicTacToe.TicTacToeController;;

public class Main {
    
    private static final int AMAZONS = 1;
    private static final int CHECKERS = 2;
    private static final int CHESS = 3;
    private static final int CHOMP = 4;
    private static final int CLOBBER = 5;
    private static final int CONNECTFOUR = 6;
    private static final int DOTSANDBOXES = 7;
    private static final int PENTAGO = 8;
    private static final int REVERSI = 9;
    private static final int TICTACTOE = 10;
	
    private static final int GAME = DOTSANDBOXES;
    
	public static void main(String[] args) {
	    
	    switch(GAME) {
	        
	        case AMAZONS: new Amazons(new AmazonsController(), new AmazonsController()); break;
            case CHECKERS: new Checkers(new CheckersController(), new CheckersController()); break;
            case CHESS: new Chess(new ChessController(), new ChessController()); break;
	        case CHOMP: new Chomp(new ChompController(), new ChompController()); break;
	        case CLOBBER: new Clobber(new ClobberController(), new ClobberController()); break;
	        case CONNECTFOUR: new ConnectFour(new ConnectFourController(), new ConnectFourController()); break;
            case DOTSANDBOXES: new DotsAndBoxes(new DotsController(), new DotsController()); break;
            case PENTAGO: new Pentago(new PentagoController(), new PentagoController()); break;
	        case REVERSI: new Reversi(new ReversiController(), new ReversiController()); break;
	        case TICTACTOE: new TicTacToe(new TicTacToeController(), new TicTacToeController()); break;
	    }
	    
	    
	}
}