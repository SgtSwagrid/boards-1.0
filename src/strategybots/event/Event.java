package strategybots.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * Implementation of an event system.
 * Allows for the registration of event handlers and the triggering of events.
 * An instance of a subclass of Event represents a single given occurrence of an event.
 * @author Alec
 */
public abstract class Event {
    
    /** Set of all registered EventHandlers, grouped by Event type. */
    private static Map<Class<?>, Set<EventHandler>>
            events = new HashMap<>();
    
    private static Semaphore eventsLock = new Semaphore(1);
    
    /**
     * Register an EventHandler. This EventHandler will be triggered
     * whenever the associated Event is itself triggered.
     * No key will be associated with this handler.
     * @param event an event type.
     * @param handler an event handler (an action to take).
     */
    public static <E extends Event> EventHandler addHandler(
            Class<E> event, Consumer<E> action) {
        
        return addHandler(event, null, action);
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
    public static <E extends Event> EventHandler addHandler(
            Class<E> event, Object key, Consumer<E> action) {
        
        try {
            eventsLock.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        
        //If no EventHandlers of the given event type have yet been added,
        //create a new list to put such handlers in.
        if(!events.containsKey(event)) {
            events.put(event, new HashSet<>());
        }
        
        //Add this handler to the list.
        EventHandler handler = new EventHandler(action, event, Optional.ofNullable(key));
        events.get(event).add(handler);
        eventsLock.release();
        return handler;
    }
    
    /**
     * Remove the given event handler. It will cease to exist.
     * Alternatively, a key can be used to remove handlers, using removeHandlers().
     * @param handler the handler to remove.
     */
    public static void removeHandler(EventHandler handler) {
        
        try {
            eventsLock.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        
        for(Class<?> event : events.keySet()) {
            events.get(event).remove(handler);
        }
        eventsLock.release();
    }
    
    /**
     * Trigger this event. All EventHandlers of matching type will
     * subsequently be triggered, using the given event as a parameter.
     */
    @SuppressWarnings("unchecked")
    protected void trigger() {
        
        try {
            eventsLock.acquire();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        
        if(events.containsKey(getClass())) {
            
            for(EventHandler handler : events.get(getClass())) {
                
                if(!handler.condition.isPresent() || checkCondition(handler.condition.get())) {
                    
                    handler.action.accept(this);
                }
            }
        }
        eventsLock.release();
    }
    
    protected boolean checkCondition(Object condition) { return true; }
    
    /**
     * Used to represent an EventHandler function, and any associated data.
     */
    @SuppressWarnings({ "rawtypes"})
    public static class EventHandler {
        
        /** The EventHandler itself. */
        Consumer action;
        
        /** The event type. */
        Class event;
        
        /** Additional condition to be met before a handler is called. */
        Optional<Object> condition;
        
        /**
         * Constructs a new EventHandler with the given values.
         * @param action the handler itself.
         * @param event the event type.
         * @param condition additional condition to be met before a handler is called.
         */
        EventHandler(Consumer<?> action, Class event, Optional<Object> condition) {
            this.action = action;
            this.event = event;
            this.condition = condition;
        }
    }
}