package strategybots.bots.legacy;

import strategybots.games.ConnectFour;
import strategybots.games.base.Game.Player;

public class ConnectFourBot implements Player<ConnectFour> {
    
    private ConnectFour GAME;
    
    @Override
    public void init(ConnectFour game, int playerId) {
        GAME = game;
    }
    
    @Override
    public void takeTurn(ConnectFour game, int playerId) {
        
        ConnectFourState state = new ConnectFourState();
        state.evaluate(2000L);
        state.getBestMove().getBestMove();
        
    }
    
    private class ConnectFourState extends GameState {

        protected ConnectFourState() {
            super(null);
            
        }

        @Override
        protected boolean isTerminal() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        protected int heuristic() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        protected boolean hasSuccessor() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        protected GameState nextSuccessor() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected void reset() {
            // TODO Auto-generated method stub
            
        }

        @Override
        protected GameState fork() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}