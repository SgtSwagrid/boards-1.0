package games;

import java.util.Optional;

import games.util.Chessboard;
import games.util.Game;
import games.util.Player;

/**
 * <b>Chomp implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Chomp">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Chomp>'.<br>
 * Human players can be made by instantiating 'ChompController'.
 * 
 * @author Alec Dorrington
 */
public class Chomp extends Game {
    
    /** Title of the window. */
    private static final String TITLE = "Game of the Amazons";
    
    /** Chessboard instance, manages the window, and tile layout. */
    private Chessboard board;
    
    /** Players participating in this game. */
    private Player<Chomp> player1, player2;
    /** The winner of the game. */
    private Optional<Player<Chomp>> winne = Optional.empty();
    /** The player whose turn is currently active. */
    private Player<Chomp> currentPlayer;
    /** The ID of the player whose turn is currently active. */
    private volatile int currentPlayerId = 1;
    
    /** The dimensions of the game board */
    
    
    public static final class ChompController implements Player<Chomp> {

        @Override
        public void takeTurn(Chomp game, int playerId) {
            // TODO Auto-generated method stub
            
        }
        
    }
}