package games.castle.render;

import java.util.HashSet;
import java.util.Set;

import games.castle.render.Window.Renderer;

public class TileRenderer implements Renderer {
	
	public static TileRenderer INSTANCE = new TileRenderer();
	
	private Set<Tile> tiles = new HashSet<>();
	
	@Override
	public void init() {
		Shapes.SQUARE.getClass();
		TileShader.INSTANCE.getClass();
	}
	
	@Override
	public void render() {
		
		TileShader.INSTANCE.start();
		
		for(Tile tile : tiles) {
			//System.out.println(tile.g);
			TileShader.INSTANCE.render(tile);
		}
		TileShader.INSTANCE.stop();
	}
	
	@Override
	public void stop() {
		TileShader.INSTANCE.destroy();
	}
	
	public void addTile(Tile tile) { tiles.add(tile); }
	
	public void removeTile(Tile tile) { tiles.remove(tile); }
}