package strategybots.event;

import static strategybots.event.InputEvent.KeyboardEvent.*;
import static strategybots.event.InputEvent.MouseEvent.*;
import static strategybots.event.InputEvent.MouseEvent.MouseButtonEvent.*;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

/**
 * Manages user input, triggering any relevant events when appropriate.
 * Ensure to call update() as often as possible for this to work properly.
 * @author Alec
 */
public class InputHandler {
    
    /** The set of all keys which are currently held down. */
    private Set<Integer> heldKeys = new HashSet<>();
    
    /** The set of all mouse buttons which are currently held down.  */
    private Set<Integer> heldButtons = new HashSet<>();
    
    public void init() {
        
        try {
            //Initialize keyboard and mouse.
            Keyboard.create();
            Mouse.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }
    
    public void update() {
        
        updateKeyboard();
        updateMouseClicks();
        updateCursor();
        updateScrollWheel();
    }
    
    /**
     * Trigger appropriate events for any keyboard activity.
     */
    private void updateKeyboard() {
        
        //Trigger key hold events when appropriate.
        for(int key : heldKeys) {
            new KeyHoldEvent(key);
        }
        
        //For each key which has been pressed since the last update.
        while(Keyboard.next()) {
            
            int key = Keyboard.getEventKey();
            
            //Key pressed.
            if(Keyboard.getEventKeyState()) {
                new KeyPressEvent(key);
                heldKeys.add(key);
                
            //Key released.
            } else {
                new KeyReleaseEvent(key);
                heldKeys.remove(key);
            }
        }
    }
    
    /**
     * Trigger appropriate events for any mouse button activity.
     */
    private void updateMouseClicks() {
        
        int x = Mouse.getX() - Display.getWidth() / 2;
        int y = Mouse.getY() - Display.getHeight() / 2;
        
        //Trigger click hold events when appropriate.
        for(int button : heldButtons) {
            new ClickHoldEvent(button, x, y);
        }
        
        //For each mouse button which has been pressed since the last update.
        while(Mouse.next()) {
            
            int button = Mouse.getEventButton();
            
            
            
            //Button pressed.
            if(Mouse.getEventButtonState()) {
                new ClickEvent(button, x, y);
                heldButtons.add(button);
                
            //Button released.
            } else {
                new ClickReleaseEvent(button, x, y);
                heldButtons.remove(button);
            }
        }
    }
    
    /**
     * Trigger an appropriate event if the mouse has moved.
     */
    private void updateCursor() {
        
        int x = Mouse.getX() - Display.getWidth() / 2;
        int y = Mouse.getY() - Display.getHeight() / 2;
        
        int mouseDX = Mouse.getDX();
        int mouseDY = Mouse.getDY();
        
        if(mouseDX != 0 || mouseDY != 0) {
            new MouseMoveEvent(mouseDX, mouseDY, x, y);
        }
    }
    
    /**
     * Trigger an appropriate event if the scroll wheel has moved.
     */
    private void updateScrollWheel() {
        
        int x = Mouse.getX() - Display.getWidth() / 2;
        int y = Mouse.getY() - Display.getHeight() / 2;

        int dWheel = Mouse.getDWheel();
        
        if(dWheel != 0) {
            new ScrollWheelEvent(dWheel, x, y);
        }
    }
}