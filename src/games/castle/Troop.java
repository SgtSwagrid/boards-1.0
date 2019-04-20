package games.castle;

import java.util.Optional;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import games.castle.Castle.CastlePlayer;
import games.castle.render.Button;
import games.castle.render.Colour;
import games.castle.render.Tile;

public class Troop extends Button {
	
	private static final int TROOP_SIZE = 30;
	private static final int TROOP_SPACING = 5;
	private static final int HIGHLIGHT_SIZE = 3;
	
	private static final float RENDER_DEPTH = 0.3F;
	
	private Castle castle;
	
	private CastleTile tile;
	
	private CastlePlayer owner;
	
	private boolean selected = false;
	private Optional<SelectHighlight> selectHighlight = Optional.empty();
	Vector2f highlightSize = new Vector2f(TROOP_SIZE + HIGHLIGHT_SIZE * 2,
			TROOP_SIZE + HIGHLIGHT_SIZE * 2);
	
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
		
		onLeftClick(e -> {
			
			if(castle.getCurrentPlayer() == owner) {
				
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					
					if(selected) castle.removeSelectedTroop(this);
					else castle.addSelectedTroop(this);
					
				} else {
					castle.clearSelectedTroops();
					castle.addSelectedTroop(this);
				}
			}
		});
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
	
	public void select() {
		selected = true;
		selectHighlight = Optional.ofNullable(new SelectHighlight(position, highlightSize));
	}
	
	public void unselect() {
		selectHighlight.get().destroy();
		selectHighlight = Optional.empty();
		selected = false;
	}
	
	private class SelectHighlight extends Tile {
		{
			depth = 0.2F;
			colour = Colour.GREEN;
		}
		public SelectHighlight(Vector2f position, Vector2f size) {
			this.position = position;
			this.size = size;
		}
	}
}