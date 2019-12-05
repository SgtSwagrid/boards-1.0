package strategybots.games.graphics;

import java.util.HashSet;
import java.util.Set;

import strategybots.games.event.Event;
import strategybots.games.event.Event.EventHandler;
import strategybots.games.event.InputEvent.MouseEvent.MouseMoveEvent;
import strategybots.games.event.InputEvent.MouseEvent.MouseButtonEvent.ClickEvent;
import strategybots.games.event.InputEvent.MouseEvent.MouseButtonEvent.ClickHoldEvent;
import strategybots.games.event.InputEvent.MouseEvent.MouseButtonEvent.ClickReleaseEvent;

/**
 * Tile with mouse listener events.
 * 
 * @author Alec Dorrington
 */
public class Button extends Tile {
    
    private static final long serialVersionUID = 194361961032057326L;

    /** Event handlers for this button. */
    private Set<EventHandler> handlers = new HashSet<>();
    
    /** Current state of interaction between button and mouse. */
    private boolean mouseOver = false, pressed = false;
    
    public Button(Window window) {
        
        super(window);
        
        //Single-click events.
        handlers.add(Event.addHandler(ClickEvent.class, e -> {
            
            if(checkBounds(e.MOUSE_X, e.MOUSE_Y)) {
                if(e.BUTTON == 0) onLeftClick(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 1) onRightClick(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 2) onMiddleClick(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                pressed = true;
            }
        }));
        
        //Click-hold events.
        handlers.add(Event.addHandler(ClickHoldEvent.class, e -> {
            
            if(pressed) {
                if(e.BUTTON == 0) onLeftHold(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 1) onRightHold(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 2) onMiddleHold(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
            }
        }));
        
        //Click-release events.
        handlers.add(Event.addHandler(ClickReleaseEvent.class, e -> {
            
            if(pressed) {
                if(e.BUTTON == 0) onLeftRelease(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 1) onRightRelease(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                if(e.BUTTON == 2) onMiddleRelease(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                pressed = false;
            }
        }));
        
        //Mouse movement events.
        handlers.add(Event.addHandler(MouseMoveEvent.class, e -> {
            
            //Whether cursor is current in bounds of button.
            boolean inBounds = checkBounds(e.MOUSE_X, e.MOUSE_Y);
            
            //Mouse enter.
            if(!mouseOver && inBounds) {
                onMouseEnter(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                mouseOver = true;
                
            //Mouse leave.
            } else if(mouseOver && !inBounds) {
                onMouseLeave(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
                mouseOver = false;
                pressed = false;
                
            //Mouse over.
            } else if(mouseOver && inBounds) {
                onMouseOver(getRx(e.MOUSE_X), getRy(e.MOUSE_Y));
            }
        }));
    }
    
    @Override
    public void destroy() {
        super.destroy();
        handlers.forEach(h -> Event.removeHandler(h));
    }
    
    /**
     * Called once when button is left-clicked.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onLeftClick(int rx, int ry) {}
    
    /**
     * Called repeatedly while button is left-pressed. 
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onLeftHold(int rx, int ry) {}
    
    /**
     * Called once when button is left-released.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onLeftRelease(int rx, int ry) {}
    
    /**
     * Called once when button is right-clicked.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onRightClick(int rx, int ry) {}
    
    /**
     * Called repeatedly while button is right-pressed.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onRightHold(int rx, int ry) {}
    
    /**
     * Called once when button is right-released.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onRightRelease(int rx, int ry) {}
    
    /**
     * Called once when button is middle-clicked.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onMiddleClick(int rx, int ry) {}
    
    /**
     * Called repeatedly while button is middle-pressed.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onMiddleHold(int rx, int ry) {}
    
    /**
     * Called once when button is middle-released.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onMiddleRelease(int rx, int ry) {}
    
    /**
     * Called once when cursor enters button.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onMouseEnter(int rx, int ry) {}
    
    /**
     * Called repeatedly while cursor over button.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onMouseOver(int rx, int ry) {}
    
    /**
     * Called once when cursor leaves button.
     * @param rx the relative x-coordinate of the cursor.
     * @param ry the relative y-coordinate of the cursor.
     */
    protected void onMouseLeave(int rx, int ry) {}
    
    /**
     * @param mouseX current x-coordinate of cursor.
     * @param mouseY current y-coordinate of cursor.
     * @return whether the cursor is currently over this button.
     */
    protected final boolean checkBounds(int mouseX, int mouseY) {
        
        //Get boundary corners of tile.
        int x1 = getX() - getWidth() / 2;
        int x2 = getX() + getWidth() / 2;
        int y1 = getY() - getHeight() / 2;
        int y2 = getY() + getHeight() / 2;
        
        return x1 <= mouseX && x2 >= mouseX && y1 <= mouseY && y2 >= mouseY;
    }
    
    /**
     * Gets the relative x-coordinate of the cursor,
     * in pixels, from the bottom-left corner of the button.
     * @param x the absolute window x-coordinate of the cursor, in pixels.
     * @return the relative x-coordinate of the cursor.
     */
    private int getRx(int x) {
        return x - getX() + getWidth()/2;
    }
    
    /**
     * Gets the relative y-coordinate of the cursor,
     * in pixels, from the bottom-left corner of the button.
     * @param y the absolute window y-coordinate of the cursor, in pixels.
     * @return the relative y-coordinate of the cursor.
     */
    private int getRy(int y) {
        return y - getY() + getHeight()/2;
    }
}