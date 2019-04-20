package games.castle;

import org.lwjgl.util.vector.Vector2f;

import games.castle.Castle.CastlePlayer;
import games.castle.render.Tile;

public class Troop extends Tile {
	
	private static final int TROOP_SIZE = 30;
	private static final int TROOP_SPACING = 5;
	
	private static final float RENDER_DEPTH = 0.2F;
	
	private Castle castle;
	
	private CastleTile tile;
	
	private CastlePlayer owner;
	
	{
		size = new Vector2f(TROOP_SIZE, TROOP_SIZE);
		depth = RENDER_DEPTH;
	}
	
	public Troop(Castle castle, CastlePlayer owner, CastleTile tile) {
		this.castle = castle;
		this.owner = owner;
		this.tile = tile;
		colour = castle.getColour(owner);
		tile.addTroop(this);
	}
	
	public void move(CastleTile destination) {
		tile.removeTroop(this);
		tile = destination;
		tile.addTroop(this);
	}
	
	public Castle getCastle() { return castle; }
	
	public CastleTile getTile() { return tile; }
	
	public CastlePlayer getOwner() { return owner; }
	
	public void updateOrdering(int index) {
		
		int tileSize = CastleTile.TILE_SIZE - TROOP_SPACING * 2;
		
		int row = index % (tileSize / (TROOP_SIZE + TROOP_SPACING));
		int column = index / (tileSize / (TROOP_SIZE + TROOP_SPACING));
		
		System.out.println(row + ", " + column);
		
		position.x = tile.getPosition().x - tileSize / 2 + row * (
				TROOP_SIZE + TROOP_SPACING) + TROOP_SIZE / 2;
		
		position.y = tile.getPosition().y + tileSize / 2 - column * (
				TROOP_SIZE + TROOP_SPACING) - TROOP_SIZE / 2;
	}
}