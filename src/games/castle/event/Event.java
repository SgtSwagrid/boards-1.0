package games.castle.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementation of an event system.
 * Allows for the registration of event handlers and the triggering of events.
 * An instance of a subclass of Event represents a single given occurrence of an event.
 * @author Alec
 */
public abstract class Event {
	
	/** List of all registered EventHandlers, grouped by Event type. */
	private static Map<Class<?>, List<EventHandler>>
			events = new HashMap<>();
	
	/**
	 * Register an EventHandler. This EventHandler will be triggered
	 * whenever the associated Event is itself triggered.
	 * No key will be associated with this handler.
	 * @param event an event type.
	 * @param handler an event handler (an action to take).
	 */
	public static <E extends Event> void addHandler(
			Class<E> event, Consumer<E> action) {
		
		addHandler(event, null, action);
	}
	
	/**
	 * Register an EventHandler. This EventHandler will be triggered
	 * whenever the associated Event is itself triggered.
	 * This handler can be removed later by calling removeHandler(handler)
	 * or removeHandlers(key).
	 * @param event an event type.
	 * @param handler an event handler (an action to take).
	 * @param key a key to use for later removal.
	 */
	public static <E extends Event> void addHandler(
			Class<E> event, Object key, Consumer<E> action) {
		
		//If no EventHandlers of the given event type have yet been added,
		//create a new list to put such handlers in.
		if(!events.containsKey(event)) {
			events.put(event, new ArrayList<>());
		}
		
		//Add this handler to the list.
		events.get(event).add(
				new EventHandler(action, event, key));
	}
	
	/**
	 * Remove the given event handler. It will cease to exist.
	 * Alternatively, a key can be used to remove handlers, using removeHandlers().
	 * @param handler the handler to remove.
	 */
	public static void removeHandler(Consumer<?> action) {
		
		//Check every event handler for every event.
		loop: for(Class<?> event : events.keySet()) {
			
			for(int i = 0; i < events.get(event).size(); i++) {
				
				//If it matches, remove it, and finish.
				if(events.get(event).get(i).action == action) {
					events.get(event).remove(i);
					break loop;
				}
			}
		}
	}
	
	/**
	 * Remove all event handlers matching the given key.
	 * Checks keys to see if they refer to the same instance,
	 * this does not check for equivalency.
	 * Alternatively, handlers can be removed directly with removeHandler().
	 * @param key a key to refer to particular handlers.
	 */
	public static void removeHandlers(Object key) {
		
		//Check every event handler for every event.
		for(Class<?> event : events.keySet()) {
			
			for(int i = 0; i < events.get(event).size(); i++) {
				
				//If it matches, remove it.
				if(events.get(event).get(i).key == key) {
					events.get(event).remove(i);
				}
			}
		}
	}
	
	/**
	 * Trigger this event. All EventHandlers of matching type will
	 * subsequently be triggered, using the given event as a parameter.
	 */
	@SuppressWarnings("unchecked")
	protected void trigger() {
		
		//Concurrency.newThread("event_handler", () -> {
			
			//For each type of event registered.
			for(Class<?> eventType : events.keySet()) {
				
				//If the type matches the current event.
				if(eventType.isInstance(this)) {
					
					//Trigger each event handler for this event type.
					for(EventHandler handler : events.get(eventType)) {
						
						handler.action.accept(this);
					}
				}
			}
		//});
	}
	
	/**
	 * Used to represent an EventHandler function, and any associated data.
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	private static class EventHandler {
		
		/** The EventHandler itself. */
		Consumer action;
		
		/** The event type. */
		Class event;
		
		/** The key used to find and remove this handler. */
		Object key;
		
		/**
		 * Constructs a new EventHandler with the given values.
		 * @param action the handler itself.
		 * @param event the event type.
		 * @param key the key used to find and remove this handler.
		 */
		EventHandler(Consumer<?> action, Class event, Object key) {
			this.action = action;
			this.event = event;
			this.key = key;
		}
	}
}