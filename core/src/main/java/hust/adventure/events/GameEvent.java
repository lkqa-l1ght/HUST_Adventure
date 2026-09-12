package hust.adventure.events;
 
import java.util.Objects;
 
/**
 * Generic event wrapper for the game's event system.
 * 
 * @param <T> The type of data carried by the event.
 */
public class GameEvent<T> {
    private final EventType type;
    private final T data;
 
    /**
     * Creates a new game event.
     *
     * @param type The type of the event.
     * @param data The payload data.
     * @throws NullPointerException if type is null.
     */
    public GameEvent(final EventType type, final T data) {
        this.type = Objects.requireNonNull(type, "EventType cannot be null");
        this.data = data;
    }
 
    public EventType getType() {
        return type;
    }
 
    public T getData() {
        return data;
    }
}
