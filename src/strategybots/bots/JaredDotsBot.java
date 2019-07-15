package strategybots.bots;

import strategybots.games.DotsAndBoxes;
import strategybots.games.DotsAndBoxes.Orien;
import strategybots.games.DotsAndBoxes.Side;
import strategybots.games.util.Game.Player;


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
        int i = 0, j = 0;
        while(i < 10)
        {
            j = 0;
            while(j < 10)
            {
                canWin(i, j, game);
                j++;
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
                counter++;
                if(counter > 1000)
                {
                    keep = false;
                    break;
                }           
            }while(game.hasLine(Orien.VERT, tempC, tempR) && !willWin(tempR, tempC, game)); 
            
            if(keep == true)
                game.drawLine(Orien.VERT, tempC, tempR);
            else
            {
           //     findNext(game);
            }
        }
        else
        {
            do
            {
                tempR = (int)(Math.random()*10);
                tempC = (int)(Math.random()*10);
                counter++;
                if(counter > 1000)
                {
                    keep = false;
                    break;
                }
            }while(game.hasLine(Orien.HORZ, tempC, tempR) && !willWin(tempR, tempC, game)); 
            
            if(keep == true)
                game.drawLine(Orien.HORZ, tempC, tempR);
            else
            {
             //   findNext(game);
            }
        }

    }
    
    public void findNext(DotsAndBoxes game)
    {
        for(int i = 0; i <= 10; i++)
        {
            for(int j = 0; j <= 10; j++)
            {
                if(!game.hasLine(Orien.HORZ, i + 1, j))
                {
                    game.drawLine(Orien.HORZ, i + 1, j);
                    break;
                }
                if(!game.hasLine(Orien.HORZ, i, j))
                {
                    game.drawLine(Orien.HORZ, i, j);
                    break;
                }
                if(!game.hasLine(Orien.VERT, i, j))
                {
                    game.drawLine(Orien.VERT, i, j);
                    break;
                }
                if(!game.hasLine(Orien.HORZ, i, j + 1))
                {
                    game.drawLine(Orien.VERT, i, j + 1);
                    break;
                }
            }
        }
    }
    
    public boolean willWin(int c, int r, DotsAndBoxes game)
    {
        boolean win = false;
        int count = 1;

        if(game.hasLine(Orien.VERT, c + 1, r))
            count++;
        if(game.hasLine(Orien.VERT, c, r))
            count++;
        if(game.hasLine(Orien.HORZ, c, r))
            count++;
        if(game.hasLine(Orien.HORZ, c, r + 1))
            count++;

        if(count == 2)
        {
            win = true;
        }
        return win;
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
