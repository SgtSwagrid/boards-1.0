package strategybots.games.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import strategybots.games.event.Event;
import strategybots.games.event.InputHandler;

/**
 * A graphical window in which contents may be rendered.
 * 
 * @author Alec Dorrington
 */
public class Window {
    
    /** Default size of the window. */
    private static final int SCREEN_WIDTH, SCREEN_HEIGHT;
    
    /** Shader with which window contents are rendered. */
    private final Shader SHADER;
    /** Input handler for dealing with input events. */
    private final InputHandler INPUT_HANDLER;
    
    /** Position and size of the window. */
    private volatile int
            width  = SCREEN_WIDTH,
            height = SCREEN_HEIGHT,
            x      = SCREEN_WIDTH / 2,
            y      = SCREEN_HEIGHT / 2;
    
    /** Title of the window. */
    private volatile String title = "";
    
    /** Indicators as to whether particular properties
     *  have been programmatically 'dirtied'. */
    private volatile boolean open = false, resizable = true,
            moved = false, resized = false, fullscreen = false;
    
    /** Background colour of window. */
    private Colour colour = Colour.WHITE;
    
    /** Maximum FPS of window. */
    private int maxFps = 240;
    
    static {
        //Fill screen for default window size.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = (int) screen.getWidth();
        SCREEN_HEIGHT = (int) screen.getHeight();
    }
    
    /**
     * Open a new window.
     * @param shader the shader with which window contents are to be rendered.
     * @param handler the input handler for dealing with input events.
     */
    public Window(Shader shader, InputHandler handler) {
        SHADER = shader;
        INPUT_HANDLER = handler;
        open();
    }
    
    /**
     * Open a new window.
     * @param width the initial width of the window (pixels).
     * @param height the initial height of the window (pixels).
     * @param shader the shader with which window contents are to be rendered.
     * @param handler the input handler for dealing with input events.
     */
    public Window(int width, int height, Shader shader, InputHandler handler) {
        SHADER = shader;
        INPUT_HANDLER = handler;
        setSize(width, height);
        open();
    }
    
    /**
     * Open a new window.
     * @param title the tile of the window.
     * @param shader the shader with which window contents are to be rendered.
     * @param handler the input handler for dealing with input events.
     */
    public Window(String title, Shader shader, InputHandler handler) {
        SHADER = shader;
        INPUT_HANDLER = handler;
        this.title = title;
        open();
    }
    
    /**
     * Open a new window.
     * @param width the initial width of the window (pixels).
     * @param height the initial height of the window (pixels).
     * @param title the title of the window.
     * @param shader the shader with which window contents are to be rendered.
     * @param handler the input handler for dealing with input events.
     */
    public Window(int width, int height, String title,
            Shader shader, InputHandler handler) {
        SHADER = shader;
        INPUT_HANDLER = handler;
        setSize(width, height);
        this.title = title;
        open();
    }
    
    /**
     * @return the shader with which the window content is rendered.
     */
    public Shader getShader() { return SHADER; }
    
    /**
     * @return the title of the window.
     */
    public String getTitle() { return title; }
    
    /**
     * @param title the new title of the window.
     */
    public void setTitle(String title) { this.title = title; }
    
    /**
     * @return the current width of the window.
     */
    public int getWidth() { return width; }
    
    /**
     * @return the current height of the window.
     */
    public int getHeight() { return height; }
    
    /**
     * @param width the new width of the window.
     * @param height the new height of the window.
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        resized = true;
    }
    
    /**
     * @return the current x-coordinate of the window.
     */
    public int getX() { return x; }
    
    /**
     * @return the current y-coordinate of the window.
     */
    public int getY() { return y; }
    
    /**
     * @param x the new x-coordinate of the window.
     * @param y the new y-coordinate of the window.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        moved = true;
    }
    
    /**
     * @return whether the window is currently open.
     */
    public boolean isOpen() { return open; }
    
    /**
     * Close the window.
     */
    public void close() { open = false; }
    
    /**
     * @return whether the window is resizable by the user.
     */
    public boolean isResizable() { return resizable; }
    
    /**
     * @param resizable whether the window is resizable by the user.
     */
    public void setResizable(boolean resizable) { this.resizable = resizable; };
    
    /**
     * @return whether the window is in fullscreen mode.
     */
    public boolean isFullscreen() { return fullscreen; }
    
    /**
     * @param fullscreen whether the window is in fullscreen mode.
     */
    public void setFullscreen(boolean fullscreen) { this.fullscreen = fullscreen; }
    
    /**
     * @return the current background colour of the window.
     */
    public Colour getColour() { return colour; }
    
    /**
     * @param colour the new background colour of the window.
     */
    public void setColour(Colour colour) { this.colour = colour; }
    
    /**
     * @return the current maximum FPS of the window.
     */
    public int getMaxFps() { return maxFps; }
    
    /**
     * @param maxFps the new maximum FPS of the window.
     */
    public void setMaxFps(int maxFps) { this.maxFps = maxFps; }
    
    /**
     * Open the window in a new thread.
     */
    private void open() {
        
        new Thread("render") {
            @Override public void run() {
                
                //Create the window, and initialize its components.
                create();
                SHADER.doInit();
                INPUT_HANDLER.init();
                open = true;
                
                //While the window remains open.
                while(!Display.isCloseRequested() && open) {
                    
                    //Update the window and its components.
                    update();
                    SHADER.doRender();
                    INPUT_HANDLER.update();
                }
                //Close the window and destroy its components.
                SHADER.destroy();
                INPUT_HANDLER.destroy();
                Display.destroy();
                open = false;
            }
        }.start();
        
        //Wait for the window to open before returning.
        while(!open);
    }
    
    /**
     * Initialize the window.
     */
    private void create() {
        
        //Set version details.
        ContextAttribs context = new ContextAttribs(3, 2)
                .withForwardCompatible(true)
                .withProfileCore(true);
        
        try {
            //Set size of window.
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create(new PixelFormat().withSamples(4), context);
            
        } catch(LWJGLException e) {
            e.printStackTrace();    
        }
        
        //Initialize OpenGL properties.
        
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    
    /**
     * Update window properties, update display and clear buffers.
     */
    private void update() {
        
        updateSize();
        updatePosition();
        updateProperties();
        
        //Update display.
        Display.sync(maxFps);
        Display.update();
        
        //Clear buffers.
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
        glClearColor(colour.R, colour.G, colour.B, colour.A);
    }
    
    /**
     * Update size of window.
     */
    private void updateSize() {
        
        //If actual window size doesn't match stored window size.
        if(Display.getWidth() != width || Display.getHeight() != height) {
            
            //If window size updated by program,
            if(resized) {
                
                //Update actual window size to match.
                try {
                    Display.setDisplayMode(new DisplayMode(width, height));
                    resized = false;
                } catch(LWJGLException e) {
                    e.printStackTrace();
                }
                
            //If window size changed by user,
            } else {
                //Update stored window size to match.
                width = Display.getWidth();
                height = Display.getHeight();
            }
            
            //Update OpenGL viewport.
            glViewport(0, 0, width, height);
            //Trigger window resize event.
            new WindowResizeEvent(width, height);
        }
        resized = false;
    }
    
    /**
     * Update position of window.
     */
    private void updatePosition() {
        
        //If actual window position doesn't match stored window position.
        if(Display.getX() != x || Display.getY() != y) {
            
            //If window position updated by program,
            if(moved) {
                //Update actual window position to match.
                Display.setLocation(x, y);
                
            //If window position changed by user,
            } else {
                //Update stored window position to match.
                x = Display.getX();
                y = Display.getY();
            }
        }
        moved = false;
    }
    
    /**
     * Update miscellaneous properties of window.
     */
    private void updateProperties() {
        
        //Update title.
        if(Display.getTitle() != title) {
            Display.setTitle(title);
        }
        
        //Update whether window is resizable.
        if(Display.isResizable() ^ resizable) {
            Display.setResizable(resizable);
        }
        
        //Update windowed/fullscreen mode.
        if(Display.isFullscreen() ^ fullscreen) {
            try {
                Display.setFullscreen(fullscreen);
            } catch (LWJGLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Event which is triggered whenever the window is resized.
     * 
     * @author Alec Dorrington
     */
    public static class WindowResizeEvent extends Event {
        
        /** The new size of the window. */
        public final int WIDTH, HEIGHT;
        
        private WindowResizeEvent(int width, int height) {
            WIDTH = width;
            HEIGHT = height;
            trigger();
        }
    }
}