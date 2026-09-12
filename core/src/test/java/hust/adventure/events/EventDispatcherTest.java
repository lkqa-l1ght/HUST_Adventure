package hust.adventure.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventDispatcher listener registration, removal, event routing,
 * and concurrent modification queue operations during dispatch.
 */
public class EventDispatcherTest {

    @BeforeEach
    public void setUp() {
        EventDispatcher.resetInstance();
    }

    @Test
    public void testGetInstanceAndReset() {
        EventDispatcher instance1 = EventDispatcher.getInstance();
        assertNotNull(instance1);
        EventDispatcher instance2 = EventDispatcher.getInstance();
        assertSame(instance1, instance2);

        EventDispatcher.resetInstance();
        EventDispatcher instance3 = EventDispatcher.getInstance();
        assertNotNull(instance3);
        assertNotSame(instance1, instance3);
    }

    @Test
    public void testAddListenerAndDispatch() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        List<GameEvent<?>> received = new ArrayList<>();
        EventListener listener = received::add;

        dispatcher.addListener(EventType.LEVEL_UP, listener);

        GameEvent<Integer> event = new GameEvent<>(EventType.LEVEL_UP, 5);
        dispatcher.dispatch(event);

        assertEquals(1, received.size());
        assertSame(event, received.get(0));
        assertEquals(5, received.get(0).getData());
    }

    @Test
    public void testRemoveListener() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        List<GameEvent<?>> received = new ArrayList<>();
        EventListener listener = received::add;

        dispatcher.addListener(EventType.LEVEL_UP, listener);
        dispatcher.removeListener(EventType.LEVEL_UP, listener);

        GameEvent<Integer> event = new GameEvent<>(EventType.LEVEL_UP, 5);
        dispatcher.dispatch(event);

        assertTrue(received.isEmpty());
    }

    @Test
    public void testDispatchDifferentEventTypes() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        List<GameEvent<?>> receivedLevelUp = new ArrayList<>();
        List<GameEvent<?>> receivedPlayerDied = new ArrayList<>();

        dispatcher.addListener(EventType.LEVEL_UP, receivedLevelUp::add);
        dispatcher.addListener(EventType.PLAYER_DIED, receivedPlayerDied::add);

        GameEvent<Integer> event = new GameEvent<>(EventType.LEVEL_UP, 2);
        dispatcher.dispatch(event);

        assertEquals(1, receivedLevelUp.size());
        assertTrue(receivedPlayerDied.isEmpty());
    }

    @Test
    public void testDuplicateListenerNotAdded() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        int[] callCount = {0};
        EventListener listener = event -> callCount[0]++;

        dispatcher.addListener(EventType.LEVEL_UP, listener);
        dispatcher.addListener(EventType.LEVEL_UP, listener);

        GameEvent<Integer> event = new GameEvent<>(EventType.LEVEL_UP, 1);
        dispatcher.dispatch(event);

        assertEquals(1, callCount[0]);
    }

    @Test
    public void testAddListenerDuringDispatch() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        List<GameEvent<?>> receivedNew = new ArrayList<>();
        EventListener newListener = receivedNew::add;

        EventListener triggerListener = new EventListener() {
            @Override
            public void onEvent(GameEvent<?> event) {
                dispatcher.addListener(EventType.LEVEL_UP, newListener);
            }
        };

        dispatcher.addListener(EventType.LEVEL_UP, triggerListener);

        // First dispatch: should trigger listener addition queue, but newListener should NOT receive this event
        GameEvent<Integer> event1 = new GameEvent<>(EventType.LEVEL_UP, 1);
        dispatcher.dispatch(event1);
        assertTrue(receivedNew.isEmpty());

        // Second dispatch: newListener is now registered, should receive this event
        GameEvent<Integer> event2 = new GameEvent<>(EventType.LEVEL_UP, 2);
        dispatcher.dispatch(event2);
        assertEquals(1, receivedNew.size());
        assertSame(event2, receivedNew.get(0));
    }

    @Test
    public void testRemoveListenerDuringDispatch() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        List<GameEvent<?>> receivedRemoved = new ArrayList<>();
        EventListener removedListener = receivedRemoved::add;

        EventListener triggerListener = new EventListener() {
            @Override
            public void onEvent(GameEvent<?> event) {
                dispatcher.removeListener(EventType.LEVEL_UP, removedListener);
            }
        };

        dispatcher.addListener(EventType.LEVEL_UP, triggerListener);
        dispatcher.addListener(EventType.LEVEL_UP, removedListener);

        // First dispatch: both are in listeners list. During dispatch, removal is queued.
        // Even if queued, it might be called if it was after triggerListener in the iteration.
        // Let's verify that the removal is completed after dispatch.
        GameEvent<Integer> event1 = new GameEvent<>(EventType.LEVEL_UP, 1);
        dispatcher.dispatch(event1);

        // Clear received and dispatch again: removedListener should NOT receive it
        receivedRemoved.clear();
        GameEvent<Integer> event2 = new GameEvent<>(EventType.LEVEL_UP, 2);
        dispatcher.dispatch(event2);
        assertTrue(receivedRemoved.isEmpty());
    }

    @Test
    public void testDispatchNullTypeThrowsException() {
        assertThrows(NullPointerException.class, () -> new GameEvent<>(null, "data"));
    }

    @Test
    public void testPlaySfxDispatchesCorrectEvent() {
        EventDispatcher dispatcher = EventDispatcher.getInstance();
        List<GameEvent<?>> received = new ArrayList<>();
        dispatcher.addListener(EventType.PLAY_SFX, received::add);

        dispatcher.playSfx("audio/sfx/ui_click.wav");

        assertEquals(1, received.size());
        assertEquals(EventType.PLAY_SFX, received.get(0).getType());
        assertEquals("audio/sfx/ui_click.wav", received.get(0).getData());

        // Test null or whitespace ignored safely
        dispatcher.playSfx(null);
        dispatcher.playSfx("   ");
        assertEquals(1, received.size());
    }
}
