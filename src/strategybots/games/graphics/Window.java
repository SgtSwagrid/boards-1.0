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

public class Window {
    
    private static final int SCREEN_WIDTH, SCREEN_HEIGHT;
    
    private final TileRenderer TILE_RENDERER = new TileRenderer();
    private final InputHandler INPUT_HANDLER = new InputHandler();
    
    private volatile int
            width  = SCREEN_WIDTH,
            height = SCREEN_HEIGHT,
            x      = SCREEN_WIDTH / 2,
            y      = SCREEN_HEIGHT / 2;
    
    private volatile String title = "";
    
    private volatile boolean open = false, resizable = true,
            moved = false, resized = false, fullscreen = false;
    
    private Colour colour = Colour.WHITE;
    
    private int maxFps = 240;
    
    static {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = (int) screen.getWidth();
        SCREEN_HEIGHT = (int) screen.getHeight();
    }
    
    public Window() {
        open();
    }
    
    public Window(int width, int height) {
        setSize(width, height);
        open();
    }
    
    public Window(String title) {
        this.title = title;
        open();
    }
    
    public Window(int width, int height, String title) {
        setSize(width, height);
        this.title = title;
        open();
    }
    
    public TileRenderer getRenderer() { return TILE_RENDERER; }
    
    public String getTitle() { return title; }
    
    public void setTitle(String title) { this.title = title; }
    
    public int getWidth() { return width; }
    
    public int getHeight() { return height; }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        resized = true;
    }
    
    public int getX() { return x; }
    
    public int getY() { return y; }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        moved = true;
    }
    
    public boolean isOpen() { return open; }
    
    public void close() { open = false; }
    
    public boolean isResizable() { return resizable; }
    
    public void setResizable(boolean resizable) { this.resizable = resizable; };
    
    public boolean isFullscreen() { return fullscreen; }
    
    public void setFullscreen(boolean fullscreen) { this.fullscreen = fullscreen; }
    
    public Colour getColour() { return colour; }
    
    public void setColour(Colour colour) { this.colour = colour; }
    
    public int getMaxFps() { return maxFps; }
    
    public void setMaxFps(int maxFps) { this.maxFps = maxFps; }
    
    public void open() {
        
        new Thread("render") {
            @Override public void run() {
                
                create();
                TILE_RENDERER.doInit();
                INPUT_HANDLER.init();
                open = true;
                
                while(!Display.isCloseRequested() && open) {
                    
                    render();
                    TILE_RENDERER.doRender();
                    INPUT_HANDLER.update();
                }
                TILE_RENDERER.destroy();
                Display.destroy();
                open = false;
            }
        }.start();
        
        while(!open);
    }
    
    private void create() {
        
        ContextAttribs context = new ContextAttribs(3, 2)
                .withForwardCompatible(true)
                .withProfileCore(true);
        
        try {
            
            Display.setDisplayMode(new DisplayMode(width, height));
            Display.create(new PixelFormat().withSamples(4), context);
            
        } catch(LWJGLException e) {
            e.printStackTrace();    
        }
        
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    
    private void render() {
        
        if(Display.getWidth() != width || Display.getHeight() != height) {
            
            if(resized) {
                
                try {
                    Display.setDisplayMode(new DisplayMode(width, height));
                    resized = false;
                } catch(LWJGLException e) {
                    e.printStackTrace();
                }
                
            } else {
                width = Display.getWidth();
                height = Display.getHeight();
            }
            
            glViewport(0, 0, width, height);
            new WindowResizeEvent(width, height);
        }
        
        resized = false;
        
        if(Display.getX() != x || Display.getY() != y) {
            
            if(moved) {
                Display.setLocation(x, y);
                
            } else {
                x = Display.getX();
                y = Display.getY();
            }
        }
        
        moved = false;
        
        if(Display.getTitle() != title) {
            Display.setTitle(title);
        }
        
        if(Display.isResizable() ^ resizable) {
            Display.setResizable(resizable);
        }
        
        if(Display.isFullscreen() ^ fullscreen) {
            try {
                Display.setFullscreen(fullscreen);
            } catch (LWJGLException e) {
                e.printStackTrace();
            }
        }
        
        Display.sync(maxFps);
        Display.update();
        
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
        glClearColor(colour.R, colour.G, colour.B, colour.A);
    }
    
    public static class WindowResizeEvent extends Event {
        
        public final int WIDTH, HEIGHT;
        
        private WindowResizeEvent(int width, int height) {
            WIDTH = width;
            HEIGHT = height;
            trigger();
        }
    }
}