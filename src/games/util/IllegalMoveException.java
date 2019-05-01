package games.util;

/**
 * Exception to be thrown when a player makes a move which isn't valid.
 * @author Alec Dorrington
 */
public class IllegalMoveException extends RuntimeException {
    
    private static final long serialVersionUID = 7081468376529411598L;
    
    public IllegalMoveException() {}
    
    public IllegalMoveException(String message) { super(message); }
}