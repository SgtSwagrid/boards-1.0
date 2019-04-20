package games.castle.render;

import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.opengl.Display;

import games.castle.event.Event;
import games.castle.event.InputEvent.MouseEvent.MouseButtonEvent.ClickEvent;

public class Button extends Tile {
	
	private Optional<Consumer<ClickEvent>> onLeftClick = Optional.empty(),
			onRightClick = Optional.empty();
	
	public Button() {
		
		Event.addHandler(ClickEvent.class, e -> {
			
			int mouseX = e.MOUSE_X - Display.getWidth() / 2;
			int mouseY = e.MOUSE_Y - Display.getHeight() / 2;
			
			if(mouseX > getPosition().x - getSize().x / 2.0F
					&& mouseX < getPosition().x + getSize().x / 2.0F
					&& mouseY > getPosition().y - getSize().y / 2.0F
					&& mouseY < getPosition().y + getSize().y / 2.0F) {
				
				switch(e.BUTTON) {
					case 0:
						if(onLeftClick.isPresent())
							onLeftClick.get().accept(e);
						break;
					case 1:
						if(onRightClick.isPresent())
							onRightClick.get().accept(e);
						break;
				}
			}
		});
	}
	
	protected void onLeftClick(Consumer<ClickEvent> onLeftClick) {
		this.onLeftClick = Optional.of(onLeftClick);
	}
	
	protected void onRightClick(Consumer<ClickEvent> onRightClick) {
		this.onRightClick = Optional.of(onRightClick);
	}
}