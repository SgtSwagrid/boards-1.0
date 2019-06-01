package strategybots.games.castle;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.util.vector.Vector2f;

import strategybots.games.castle.Castle.CastlePlayer;
import strategybots.games.castle.render.Button;
import strategybots.games.castle.render.Texture;

public class CastleTile extends Button {
	
	public static final int TILE_SIZE = 150;
	
	private static final float RENDER_DEPTH = 0.1F;
	
	private Castle castle;
	
	private int x, y;
	
	private Map<CastlePlayer, List<Troop>> troops = new LinkedHashMap<>();
	
	{
		size = new Vector2f(TILE_SIZE, TILE_SIZE);
		depth = RENDER_DEPTH;
		texture = Texture.getTexture("res/texture/castle_tile.png");
		
		
	}
	
	public CastleTile(Castle castle, int x, int y) {
		this.castle = castle;
		setPosition(x, y);
	}
	
	public void setPosition(int x, int y) {
		this.x = x; this.y = y;
		position.x = (2 * x - castle.getBoardSize() + 1) * TILE_SIZE / 2;
		position.y = (2 * y - castle.getBoardSize() + 1) * TILE_SIZE / 2;
	}
	
	public Castle getCastle() { return castle; }
	
	public int getX() { return x; }
	
	public int getY() { return y; }
	
	public void addTroop(Troop troop) {
		
		if(!troops.containsKey(troop.getOwner()))
			troops.put(troop.getOwner(), new LinkedList<>());
		
		troops.get(troop.getOwner()).add(troop);
		updateTroopOrder();
	}
	
	public void removeTroop(Troop troop) {
		if(troops.containsKey(troop.getOwner())) {
			troops.get(troop.getOwner()).remove(troop);
			updateTroopOrder();
		}
	}
	
	private void updateTroopOrder() {
		int id = 0;
		for(Entry<CastlePlayer, List<Troop>> entry : troops.entrySet()) {
			for(Troop troop : entry.getValue()) {
				troop.updateOrdering(id++);
			}
		}
	}
}