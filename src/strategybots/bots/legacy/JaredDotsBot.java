package strategybots.bots.legacy;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.base.Game.Player;


public class JaredDotsBot implements Player<DotsAndBoxes> {

    @Override
    public void init(DotsAndBoxes game, int playerId) {
        
    }

    @Override
    public void takeTurn(DotsAndBoxes game, int playerId) 
    {
        if(!game.hasLine(Orien.VERT, 10, 9))
        {
            game.drawLine(Orien.VERT, 10, 9);
        }
        else if(!game.hasLine(Orien.HORZ, 9, 10))
        {
            game.drawLine(Orien.HORZ, 9, 10);
        }
        try
        {
            Thread.sleep(50);
            makeMove(game);          
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }       
    }

    public void makeMove(DotsAndBoxes game)
    {
        int horz = 0, vert = 1;
        boolean win = false;
        int i = 0, j = 0;
        while(i < 10)
        {
            j = 0;
            while(j < 10)
            {
                win = false;
                win = canWin(i, j, game);
                j++;
                if(win == true)
                {
                    j = 0;
                    i = 0;
                }
            }
            i++;
        }

        int orien, tempR, tempC, counter = 0;
        boolean keep = true;
        orien = (int)(2*Math.random());
        if(orien == 0)
        {
            do
            {
                tempR = (int)(Math.random()*10);
                tempC = (int)(Math.random()*10);          
            }while(game.hasLine(Orien.VERT, tempC, tempR) && !willWin(tempR, tempC, game));
            game.drawLine(Orien.VERT, tempR, tempC);
        }
        else
        {
            do
            {
                tempR = (int)(Math.random()*10);
                tempC = (int)(Math.random()*10);
            }while(game.hasLine(Orien.HORZ, tempC, tempR) && !willWin(tempR, tempC, game)); 
            game.drawLine(Orien.HORZ, tempC, tempR);
        }
    }
    
    public boolean willWin(int c, int r, DotsAndBoxes game)
    {      
        int count = 0;

        if(game.hasLine(Orien.VERT, c + 1, r))
            count++;
        if(c < 9 && r < 9 && game.hasLine(Orien.VERT, c + 1, r + 1))
            count++;
        if(c < 9 && r < 9 && game.hasLine(Orien.HORZ, c + 1, r + 1))
            count++;
        if(game.hasLine(Orien.HORZ, c, r + 1))
            count++;
        if(count == 2)
        {
            return true;
        }
                 
        count = 0;

        if(c > 0 && game.hasLine(Orien.VERT, c - 1, r))
            count++;
        if(c > 0 && r > 0 && game.hasLine(Orien.VERT, c - 1, r - 1))
            count++;
        if(c > 0 && r > 0 && game.hasLine(Orien.HORZ, c - 1, r - 1))
            count++;
        if(r > 0 && game.hasLine(Orien.HORZ, c, r - 1))
            count++;
        if(count == 2)
        {    
            return true;
        }
        
        return false;
    }
    
    

    public boolean canWin(int r, int c, DotsAndBoxes game)
    {
        boolean win = false;
        int count = 0;
        int empty = -1;

        if(game.hasLine(Orien.VERT, c, r))
            count++;
        else
            empty = 1;
        if(game.hasLine(Orien.VERT, c + 1, r))
            count++;
        else
            empty = 2;
        if(game.hasLine(Orien.HORZ, c, r))
            count++;
        else
            empty = 3;
        if(game.hasLine(Orien.HORZ, c, r + 1))
            count++;
        else 
            empty = 4;

        if(count == 3)
        {
            win = true;

            switch(empty)
            {
                case 1: 
                    game.drawLine(Orien.VERT, c, r);
                    break;
                case 2: 
                    game.drawLine(Orien.VERT, c + 1, r);
                    break;
                case 3:
                    game.drawLine(Orien.HORZ, c, r);
                    break;
                case 4:
                    game.drawLine(Orien.HORZ, c, r + 1);
                    break;
                default:
                    break;
            }
        }
        return win;
    }

    @Override
    public String getName() { return "Jared Bot"; }
}
