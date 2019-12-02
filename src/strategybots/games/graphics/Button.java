package strategybots.games.graphics;

import java.util.HashSet;
import java.util.Set;

import strategybots.games.event.Event;
import strategybots.games.event.Event.EventHandler;
import strategybots.games.event.InputEvent.MouseEvent.MouseMoveEvent;
import strategybots.games.event.InputEvent.MouseEvent.MouseButtonEvent.ClickEvent;
import strategybots.games.event.InputEvent.MouseEvent.MouseButtonEvent.ClickHoldEvent;
import strategybots.games.event.InputEvent.MouseEvent.MouseButtonEvent.ClickReleaseEvent;

public class Button extends Tile {
    
    private Set<EventHandler> handlers = new HashSet<>();
    
    private boolean mouseOver = false, pressed = false;
    
    {
        handlers.add(Event.addHandler(ClickEvent.class, e -> {
            
            if(checkBounds(e.MOUSE_X, e.MOUSE_Y)) {
                if(e.BUTTON == 0) onLeftClick(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 1) onRightClick(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 2) onMiddleClick(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                pressed = true;
            }
        }));
        
        handlers.add(Event.addHandler(ClickHoldEvent.class, e -> {
            
            if(pressed) {
                if(e.BUTTON == 0) onLeftHold(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 1) onRightHold(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 2) onMiddleHold(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
            }
        }));
        
        handlers.add(Event.addHandler(ClickReleaseEvent.class, e -> {
            
            if(pressed) {
                if(e.BUTTON == 0) onLeftRelease(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 1) onRightRelease(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 2) onMiddleRelease(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                pressed = false;
            }
        }));
        
        handlers.add(Event.addHandler(MouseMoveEvent.class, e -> {
            
            boolean inBounds = checkBounds(e.MOUSE_X, e.MOUSE_Y);
            
            if(!mouseOver && inBounds) {
                onMouseEnter(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                mouseOver = true;
                
            } else if(mouseOver && !inBounds) {
                onMouseLeave(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                mouseOver = false;
                pressed = false;
                
            } else if(mouseOver && inBounds) {
                onMouseOver(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
            }
        }));
    }

    public Button(Window window) { super(window); }
    
    @Override
    public void delete() {
        super.delete();
        handlers.forEach(h -> Event.removeHandler(h));
    }
    
    protected void onLeftClick(int rx, int ry) {}
    
    protected void onLeftHold(int rx, int ry) {}
    
    protected void onLeftRelease(int rx, int ry) {}
    
    protected void onRightClick(int rx, int ry) {}
    
    protected void onRightHold(int rx, int ry) {}
    
    protected void onRightRelease(int rx, int ry) {}
    
    protected void onMiddleClick(int rx, int ry) {}
    
    protected void onMiddleHold(int rx, int ry) {}
    
    protected void onMiddleRelease(int rx, int ry) {}
    
    protected void onMouseEnter(int rx, int ry) {}
    
    protected void onMouseOver(int rx, int ry) {}
    
    protected void onMouseLeave(int rx, int ry) {}
    
    protected boolean checkBounds(int mouseX, int mouseY) {
        
        int x1 = getX() - getWidth() / 2;
        int x2 = getX() + getWidth() / 2;
        int y1 = getY() - getHeight() / 2;
        int y2 = getY() + getHeight() / 2;
        
        return x1 <= mouseX && x2 >= mouseX && y1 <= mouseY && y2 >= mouseY;
    }
    
    private int getRx(int x) {
        return x - getX() + getWidth()/2;
    }
    
    private int getRy(int y) {
        return y - getY() + getHeight()/2;
    }
}