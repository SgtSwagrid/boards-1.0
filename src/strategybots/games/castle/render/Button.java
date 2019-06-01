package strategybots.games.castle.render;

import org.lwjgl.opengl.Display;

import strategybots.games.castle.event.Event;
import strategybots.games.castle.event.InputEvent.MouseEvent.MouseButtonEvent.ClickEvent;

public class Button extends Tile {
	
	public Button() {
		
		Event.addHandler(ClickEvent.class, e -> {
			
			int mouseX = e.MOUSE_X - Display.getWidth() / 2;
			int mouseY = e.MOUSE_Y - Display.getHeight() / 2;
			
			if(mouseX > getPosition().x - getSize().x / 2.0F
					&& mouseX < getPosition().x + getSize().x / 2.0F
					&& mouseY > getPosition().y - getSize().y / 2.0F
					&& mouseY < getPosition().y + getSize().y / 2.0F) {
				
				switch(e.BUTTON) {
					case 0: onLeftClick(); break;
					case 1: onRightClick(); break;
				}
			}
		});
	}
	
	protected void onLeftClick() {}
	
	protected void onRightClick() {}
}