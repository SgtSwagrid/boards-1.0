package strategybots.games.castle.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import strategybots.games.castle.event.Input;
import strategybots.games.castle.event.Timing;

public class Window {
	
	private static final int SCREEN_WIDTH, SCREEN_HEIGHT;
	
	private List<Renderer> renderers = new LinkedList<>();
	
	private volatile int
			width  = SCREEN_WIDTH,
			height = SCREEN_HEIGHT,
			x      = SCREEN_WIDTH / 2,
			y      = SCREEN_HEIGHT / 2;
	
	private volatile String title = "";
	
	private volatile boolean open = false, resizable = true,
			moved = false, resized = false, fullscreen = false;
	
	private Colour colour = Colour.BLACK;
	
	private int maxFps = 240;
	
	private Semaphore lock = new Semaphore(1);
	
	static {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		SCREEN_WIDTH = (int) screen.getWidth();
		SCREEN_HEIGHT = (int) screen.getHeight();
	}
	
	public Window() {
		init();
	}
	
	public Window(int width, int height) {
		setSize(width, height);
		init();
	}
	
	public Window(String title) {
		this.title = title;
		init();
	}
	
	public Window(int width, int height, String title) {
		setSize(width, height);
		this.title = title;
		init();
	}
	
	public void addRenderer(Renderer renderer) { renderers.add(renderer); }
	
	public void removeRenderer(Renderer renderer) { renderers.remove(renderer); }
	
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
	
	private void init() {
		
		acquireLock();
		
		new Thread("Window") {
			@Override public void run() {
				
				create();
				renderers.forEach(Renderer::init);
				open = true;
				
				releaseLock();
				
				while(!Display.isCloseRequested() && open) {
					
					update();
					renderers.forEach(Renderer::render);
					Input.update(Window.this);
					Timing.update();
				}
				
				acquireLock();
				renderers.forEach(Renderer::stop);
				Display.destroy();
				open = false;
				releaseLock();
			}
		}.start();
	}
	
	/**
	 * Create a new window ready for OpenGL rendering.
	 * 'update()' must be subsequently called upon each render loop.
	 */
	private void create() {
		
		//Create new OpenGL context to attach to window.
		ContextAttribs context = new ContextAttribs(3, 2)
				.withForwardCompatible(true)
				.withProfileCore(true);
		
		try {
			
			//Create a new window.
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.create(new PixelFormat().withSamples(4), context);
			
		} catch(LWJGLException e) {
			e.printStackTrace();	
		}
		
		glEnable(GL_MULTISAMPLE);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_BACK);
		
		/*Event.addHandler(WindowResizeEvent.class, e -> {
			//glViewport(0, 0, e.WIDTH, e.HEIGHT);
			update();
		});*/
	}
	
	private void update() {
		
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
			//new WindowResizeEvent(width, height);
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
	
	public void acquireLock() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void releaseLock() {
		lock.release();
	}
	
	/*public static class WindowResizeEvent extends Event {
		
		public final int WIDTH, HEIGHT;
		
		private WindowResizeEvent(int width, int height) {
			WIDTH = width;
			HEIGHT = height;
			trigger();
		}
	}*/
	
	public interface Renderer {
		
		void init();
		
		void render();
		
		void stop();
	}
}