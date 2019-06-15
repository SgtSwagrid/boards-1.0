package strategybots.games;

/**
 * <b>Pentago implementation.</b><br>
 * <br>
 * Rules: <a href="https://en.wikipedia.org/wiki/Pentago">Wikipedia</a><br>
 * <br>
 * Bot players can be made by implementing 'Player<Pentago>'.<br>
 * Human players can be made by instantiating 'PentagoController'.
 * 
 * @author Alec Dorrington
 */
public class Pentago extends TicTacToe {

    public Pentago(int width, int height, int target,
            Player<TicTacToe> player1, Player<TicTacToe> player2) {
        super(width, height, target, player1, player2);
    }
}