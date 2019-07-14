package strategybots.bots;

import java.util.Random;

import strategybots.games.ConnectFour;
import strategybots.games.util.Game.Player;

public class C4_Dummy implements Player<ConnectFour> {
    
    private Random rand = new Random();

    @Override
    public void takeTurn(ConnectFour game, int playerId) {
        
        /*int pos;
        
        do {
            pos = rand.nextInt(game.getWidth());
        } while(!game.validatePlacement(pos));
        
        game.placeStone(pos);*/
        
        for(int i = 0; i < game.getWidth(); i++) {
            
            if(game.validatePlacement(i)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                game.placeStone(i);
                break;
            }
        }
    }
}