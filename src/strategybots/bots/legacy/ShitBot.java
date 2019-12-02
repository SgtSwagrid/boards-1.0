package strategybots.bots.legacy;

import java.util.Random;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.base.Game.Player;

public class ShitBot implements Player<DotsAndBoxes> {
    
    Random rand = new Random();

    @Override
    public void takeTurn(DotsAndBoxes game, int playerId) {
        
        boolean[][][] state = getState(game);
        
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                
                if(numSides(state, x, y) == 3) {
                    
                    place(game, x, y); break;
                }
            }
        }
        
        boolean orien = rand.nextInt(2)==1;
        
        game.drawLine(orien ? Orien.VERT : Orien.HORZ,
                rand.nextInt(game.getWidth()+(orien?1:0)), rand.nextInt(game.getHeight()+(orien?0:1)));
        
    }
    
    private int numSides(boolean[][][] state, int x, int y) {
        
        return (state[0][x][y]?1:0) + (state[0][x][y+1]?1:0) +
                (state[1][x][y]?1:0) + (state[1][x+1][y]?1:0);
    }
    
    private boolean[][][] getState(DotsAndBoxes game) {
        
        boolean[][][] state = new boolean[2][][];
        
        state[0] = new boolean[game.getWidth()][game.getHeight()+1];
        state[1] = new boolean[game.getWidth()+1][game.getHeight()];
        
        for(int x = 0; x < game.getWidth(); x++) {
            for(int y = 0; y < game.getHeight()+1; y++) {
                state[0][x][y] = game.hasLine(Orien.HORZ, x, y);
            }
        }
        
        for(int x = 0; x < game.getWidth()+1; x++) {
            for(int y = 0; y < game.getHeight(); y++) {
                state[1][x][y] = game.hasLine(Orien.VERT, x, y);
            }
        }
        return state;
    }
    
    private void place(DotsAndBoxes game, int x, int y) {
        if(!game.hasLine(Orien.VERT, x, y)) game.drawLine(Orien.VERT, x, y);
        else if(!game.hasLine(Orien.VERT, x+1, y)) game.drawLine(Orien.VERT, x+1, y);
        else if(!game.hasLine(Orien.HORZ, x, y)) game.drawLine(Orien.HORZ, x, y);
        else if(!game.hasLine(Orien.HORZ, x, y+1)) game.drawLine(Orien.HORZ, x, y+1);
    }
    
    @Override
    public String getName() { return "Shit Bot"; }
}