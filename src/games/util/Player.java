package games.util;

/**
 * Supertype for all player implementations.
 * 
 * @author Alec Dorrington
 */
public interface Player<G extends Game> {
    
    /**
     * Called once before the game begins.
     * @param game the game being played.
     * @param playerId the ID of this player.
     */
    default void init(G game, int playerId) {}
    
    /**
     * Called once whenever the player is expected to take a turn.<br>
     * Implementations of this must complete a whole turn as per the rules of the specific game.
     * @param game the game being played.
     * @param playerId the ID of this player.
     */
    void takeTurn(G game, int playerId);
    
    /**
     * Called to determine the name of this player.<br>
     * Used only for display purposes.
     * @return the player name (defaults to "Bot").
     */
    default String getName() { return "Bot"; }
}