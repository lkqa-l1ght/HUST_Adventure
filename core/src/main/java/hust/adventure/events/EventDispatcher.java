package hust.adventure.events;
 
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
 
/**
 * Central event dispatcher for the game.
 */
public class EventDispatcher {
    private static EventDispatcher instance;
 
    private final Map<EventType, List<EventListener>> listeners;
    private boolean isDispatching;
    private final List<Runnable> queueActions;
 
    private EventDispatcher() {
        listeners = new EnumMap<>(EventType.class);
        for (EventType type : EventType.values()) {
            listeners.put(type, new ArrayList<>());
        }
        isDispatching = false;
        queueActions = new ArrayList<>();
    }
 
    public static EventDispatcher getInstance() {
        if (instance == null) {
            instance = new EventDispatcher();
        }
        return instance;
    }
 
    // For testing
    public static void resetInstance() {
        instance = null;
    }
 
    public void addListener(EventType type, EventListener listener) {
        if (isDispatching) {
            queueActions.add(() -> addListenerInternal(type, listener));
        } else {
            addListenerInternal(type, listener);
        }
    }
 
    private void addListenerInternal(EventType type, EventListener listener) {
        List<EventListener> eventListeners = listeners.get(type);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }
 
    public void removeListener(EventType type, EventListener listener) {
        if (isDispatching) {
            queueActions.add(() -> removeListenerInternal(type, listener));
        } else {
            removeListenerInternal(type, listener);
        }
    }
 
    private void removeListenerInternal(EventType type, EventListener listener) {
        List<EventListener> eventListeners = listeners.get(type);
        eventListeners.remove(listener);
    }
 
    /**
     * Dispatches an event to all registered listeners.
     *
     * @param event The event to dispatch.
     */
    public void dispatch(GameEvent<?> event) {
        isDispatching = true;
        List<EventListener> eventListeners = listeners.get(event.getType());
 
        for (EventListener listener : eventListeners) {
            listener.onEvent(event);
        }
 
        isDispatching = false;
 
        for (Runnable action : queueActions) {
            action.run();
        }
        queueActions.clear();
    }

    /**
     * Dispatches a sound effect playback event.
     *
     * @param sfxPath the asset path of the sound effect to play
     */
    public void playSfx(final String sfxPath) {
        if (sfxPath != null && !sfxPath.trim().isEmpty()) {
            dispatch(new GameEvent<>(EventType.PLAY_SFX, sfxPath));
        }
    }
}
