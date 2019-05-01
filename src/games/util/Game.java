package games.util;

import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class Game {
    
    protected Player[] players;
    protected Optional<Player> winner = Optional.empty();
    
    protected Player currentPlayer;
    protected volatile int currentPlayerId = 1;
    
    protected Game(Player[] players) {
        this.players = players;
    }
    
    public int getNumPlayers() { return players.length; }
    
    public int getCurrentPlayerId() { return currentPlayerId; }
    
    public Optional<Player> getWinner() { return winner; }
    
    protected void start() {
        
        for(int i = 0; i < players.length; i++) {
            players[i].init(this, i);
        }
        
        while(!winner.isPresent()) {
            
            currentPlayer = players[currentPlayerId - 1];
            
            setupTurn();
            currentPlayer.takeTurn(this, currentPlayerId);
            verifyTurn();
            
            currentPlayerId = currentPlayerId % players.length + 1;
        }
    }
    
    protected void setupTurn() {}
    
    protected void verifyTurn() {}
}