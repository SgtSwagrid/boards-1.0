package strategybots.graphics;

import strategybots.event.Event;
import strategybots.event.InputEvent.MouseEvent.MouseMoveEvent;
import strategybots.event.InputEvent.MouseEvent.MouseButtonEvent.ClickEvent;
import strategybots.event.InputEvent.MouseEvent.MouseButtonEvent.ClickHoldEvent;
import strategybots.event.InputEvent.MouseEvent.MouseButtonEvent.ClickReleaseEvent;

public class Button extends Tile {
    
    private boolean mouseOver = false, pressed = false;
    
    {
        Event.addHandler(ClickEvent.class, e -> {
            
            if(checkBounds(e.MOUSE_X, e.MOUSE_Y)) {
                if(e.BUTTON == 0) onLeftClick();
                if(e.BUTTON == 1) onRightClick();
                if(e.BUTTON == 2) onMiddleClick();
                pressed = true;
            }
        });
        
        Event.addHandler(ClickHoldEvent.class, e -> {
            
            if(pressed) {
                if(e.BUTTON == 0) onLeftHold();
                if(e.BUTTON == 1) onRightHold();
                if(e.BUTTON == 2) onMiddleHold();
            }
        });
        
        Event.addHandler(ClickReleaseEvent.class, e -> {
            
            if(pressed) {
                if(e.BUTTON == 0) onLeftRelease();
                if(e.BUTTON == 1) onRightRelease();
                if(e.BUTTON == 2) onMiddleRelease();
                pressed = false;
            }
        });
        
        Event.addHandler(MouseMoveEvent.class, e -> {
            
            boolean inBounds = checkBounds(e.MOUSE_X, e.MOUSE_Y);
            
            if(!mouseOver && inBounds) {
                onMouseEnter();
                mouseOver = true;
                
            } else if(mouseOver && !inBounds) {
                onMouseLeave();
                mouseOver = false;
                pressed = false;
                
            } else if(mouseOver && inBounds) {
                onMouseOver();
            }
        });
    }

    public Button(Window window) { super(window); }
    
    protected void onLeftClick() {}
    
    protected void onLeftHold() {}
    
    protected void onLeftRelease() {}
    
    protected void onRightClick() {}
    
    protected void onRightHold() {}
    
    protected void onRightRelease() {}
    
    protected void onMiddleClick() {}
    
    protected void onMiddleHold() {}
    
    protected void onMiddleRelease() {}
    
    protected void onMouseEnter() {}
    
    protected void onMouseOver() {}
    
    protected void onMouseLeave() {}
    
    protected boolean checkBounds(int mouseX, int mouseY) {
        
        int x1 = getX() - getWidth() / 2;
        int x2 = getX() + getWidth() / 2;
        int y1 = getY() - getHeight() / 2;
        int y2 = getY() + getHeight() / 2;
        
        return x1 <= mouseX && x2 >= mouseX && y1 <= mouseY && y2 >= mouseY;
    }
}