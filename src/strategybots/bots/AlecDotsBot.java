package strategybots.bots;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.util.Game.Player;


public class AlecDotsBot implements Player<DotsAndBoxes> {
    
    @Override
    public void init(DotsAndBoxes game, int playerId) {
        
    }

    @Override
    public void takeTurn(DotsAndBoxes game, int playerId) {
        
        game.drawLine(Orien.VERT, 0, 0);
    }
    
    @Override
    public String getName() { return "Alec Bot"; }
}
