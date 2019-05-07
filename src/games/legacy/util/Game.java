package games.legacy.util;

public interface Game {
	
	@SuppressWarnings("serial")
	public static class IllegalMoveException extends RuntimeException {
		
		public IllegalMoveException(Player<?> player, String message) {
			super("Illegal move by " + player + ": " + message);
		}
	}
}