package games.legacy.util;

public interface Player<T extends Game> {
	
	public default void init(T game) {}
	
	public void takeTurn(T game);
	
}