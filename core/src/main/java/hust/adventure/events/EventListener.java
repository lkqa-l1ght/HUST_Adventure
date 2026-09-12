package hust.adventure.events;

@FunctionalInterface
public interface EventListener {
    void onEvent(GameEvent<?> event);
}
