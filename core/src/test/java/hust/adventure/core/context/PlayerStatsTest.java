package hust.adventure.core.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerStats class verifying progression, clamps, and death events.
 */
public class PlayerStatsTest {
    private PlayerStats stats;

    @BeforeEach
    public void setUp() {
        EventDispatcher.resetInstance();
        stats = new PlayerStats();
    }

    @Test
    public void testDefaultValues() {
        assertEquals(300f, stats.getHp(), 0.01f);
        assertEquals(300f, stats.getMaxHp(), 0.01f);
        assertEquals(100f, stats.getStamina(), 0.01f);
        assertEquals(100f, stats.getMaxStamina(), 0.01f);
        assertEquals(100f, stats.getMorale(), 0.01f);
        assertEquals(1, stats.getLevel());
        assertEquals(0f, stats.getExp(), 0.01f);
        assertEquals(10f, stats.getExpToNextLevel(), 0.01f);
        assertEquals(1.0f, stats.getDamageMultiplier(), 0.01f);
    }

    @Test
    public void testHpClamping() {
        stats.setHp(350f);
        assertEquals(300f, stats.getHp(), 0.01f); // Cap to maxHp

        stats.setHp(-50f);
        assertEquals(0f, stats.getHp(), 0.01f); // Floor to 0
    }

    @Test
    public void testStaminaClamping() {
        stats.setStamina(150f);
        assertEquals(100f, stats.getStamina(), 0.01f); // Cap to maxStamina

        stats.setStamina(-10f);
        assertEquals(0f, stats.getStamina(), 0.01f); // Floor to 0
    }

    @Test
    public void testSetMaxHpCappingCurrentHp() {
        stats.setHp(80f);
        stats.setMaxHp(50f);
        assertEquals(50f, stats.getHp(), 0.01f); // Capped to maxHp
    }

    @Test
    public void testReset() {
        stats.setHp(50f);
        stats.setStamina(30f);
        stats.setLevel(5);
        stats.reset();
        assertEquals(300f, stats.getHp(), 0.01f);
        assertEquals(100f, stats.getStamina(), 0.01f);
        assertEquals(1, stats.getLevel());
    }

    @Test
    public void testPlayerDiedEventDispatchedOnDeath() {
        EventListener listener = mock(EventListener.class);
        EventDispatcher.getInstance().addListener(EventType.PLAYER_DIED, listener);

        stats.setHp(0f);

        verify(listener, times(1)).onEvent(argThat(event -> event.getType() == EventType.PLAYER_DIED));
    }
}
