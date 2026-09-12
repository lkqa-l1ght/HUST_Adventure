package hust.adventure.wave;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration and unit tests for WaveManager spawn timing, game time limits,
 * and different spawn strategy formations.
 */
public class WaveManagerTest {

    private EntityFactory entityFactory;

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
        EventDispatcher.resetInstance();
        entityFactory = mock(EntityFactory.class);
    }

    private WaveEntry makeWave(float start, float end, float interval, int count, String pattern, String enemyType) throws Exception {
        WaveEntry w = new WaveEntry();
        setField(w, "timeStart", start);
        setField(w, "timeEnd", end);
        setField(w, "spawnInterval", interval);
        setField(w, "spawnCount", count);
        setField(w, "pattern", pattern);
        setField(w, "enemyType", enemyType);
        return w;
    }

    private void setField(Object obj, String name, Object val) throws Exception {
        Field f = WaveEntry.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, val);
    }

    @Test
    public void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new WaveManager(new Array<>(), null));
    }

    @Test
    public void testTimeAccumulationAndLimitReached() {
        WaveManager waveManager = new WaveManager(new Array<>(), entityFactory);
        Camera camera = new Camera() {
            @Override
            public void update() {}
            @Override
            public void update(boolean updateFrustum) {}
        };
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        camera.position.set(400f, 300f, 0f);

        List<GameEvent<?>> events = new ArrayList<>();
        EventDispatcher.getInstance().addListener(EventType.TIME_LIMIT_REACHED, events::add);

        // Update with 10s delta
        waveManager.update(10f, camera);
        assertEquals(10f, waveManager.getGameTime(), 0.01f);
        assertTrue(events.isEmpty());

        // Fast-forward to 1800s (maxTime)
        waveManager.update(1790f, camera);
        assertEquals(1800f, waveManager.getGameTime(), 0.01f);
        assertEquals(1, events.size());
        assertEquals(EventType.TIME_LIMIT_REACHED, events.get(0).getType());

        // Further updates should not trigger another event or increase time
        waveManager.update(10f, camera);
        assertEquals(1800f, waveManager.getGameTime(), 0.01f);
        assertEquals(1, events.size());
    }

    @Test
    public void testWaveActivationAndDeactivation() throws Exception {
        Array<WaveEntry> waves = new Array<>();
        // Wave active between 10s and 20s
        waves.add(makeWave(10f, 20f, 5f, 3, "RANDOM_EDGE", "bug"));

        WaveManager waveManager = new WaveManager(waves, entityFactory);
        Camera camera = new Camera() {
            @Override
            public void update() {}
            @Override
            public void update(boolean updateFrustum) {}
        };
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        camera.position.set(400f, 300f, 0f);

        // 1. Game time 5s: wave not active yet, no spawns should occur
        waveManager.update(5f, camera);
        verify(entityFactory, never()).createEnemy(anyString(), anyFloat(), anyFloat());

        // 2. Game time 11s: wave active, first spawn should occur immediately upon activation
        waveManager.update(6f, camera);
        verify(entityFactory, times(3)).createEnemy(eq("bug"), anyFloat(), anyFloat());

        // 3. Game time 13s: interval is 5s, so no new spawn should occur yet
        waveManager.update(2f, camera);
        verify(entityFactory, times(3)).createEnemy(eq("bug"), anyFloat(), anyFloat());

        // 4. Game time 16s: interval of 5s exceeded, second spawn should occur
        waveManager.update(3f, camera);
        verify(entityFactory, times(6)).createEnemy(eq("bug"), anyFloat(), anyFloat());

        // 5. Game time 25s: wave deactivated, no spawns
        waveManager.update(9f, camera);
        verify(entityFactory, times(6)).createEnemy(eq("bug"), anyFloat(), anyFloat());
    }

    @Test
    public void testOneShotWave() throws Exception {
        Array<WaveEntry> waves = new Array<>();
        // Wave active between 10s and 20s, but interval is 0 -> should spawn exactly once
        waves.add(makeWave(10f, 20f, 0f, 2, "RANDOM_EDGE", "boss"));

        WaveManager waveManager = new WaveManager(waves, entityFactory);
        Camera camera = new Camera() {
            @Override
            public void update() {}
            @Override
            public void update(boolean updateFrustum) {}
        };
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        camera.position.set(400f, 300f, 0f);

        // 1. Activate wave at 11s -> should spawn 2 enemies
        waveManager.update(11f, camera);
        verify(entityFactory, times(2)).createEnemy(eq("boss"), anyFloat(), anyFloat());

        // 2. Update again at 15s -> should NOT spawn again because it is a one-shot
        waveManager.update(4f, camera);
        verify(entityFactory, times(2)).createEnemy(eq("boss"), anyFloat(), anyFloat());
    }

    @Test
    public void testMultipleOverlappingWaves() throws Exception {
        Array<WaveEntry> waves = new Array<>();
        waves.add(makeWave(10f, 30f, 10f, 1, "RANDOM_EDGE", "bug"));
        waves.add(makeWave(20f, 40f, 10f, 1, "RANDOM_EDGE", "error"));

        WaveManager waveManager = new WaveManager(waves, entityFactory);
        Camera camera = new Camera() {
            @Override
            public void update() {}
            @Override
            public void update(boolean updateFrustum) {}
        };
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        camera.position.set(400f, 300f, 0f);

        // Game time 15s -> only first wave active, spawns 1 "bug"
        waveManager.update(15f, camera);
        verify(entityFactory, times(1)).createEnemy(eq("bug"), anyFloat(), anyFloat());
        verify(entityFactory, never()).createEnemy(eq("error"), anyFloat(), anyFloat());

        // Game time 25s -> both waves active:
        // First wave spawns again at 20s (15s + 10s interval reached at 25s update)
        // Second wave activates at 20s, spawns immediately at 25s update
        waveManager.update(10f, camera);
        verify(entityFactory, times(2)).createEnemy(eq("bug"), anyFloat(), anyFloat());
        verify(entityFactory, times(1)).createEnemy(eq("error"), anyFloat(), anyFloat());
    }

    @Test
    public void testIsFinished() throws Exception {
        Array<WaveEntry> waves = new Array<>();
        waves.add(makeWave(0f, 10f, 2f, 1, "RANDOM_EDGE", "bug"));
        waves.add(makeWave(5f, 20f, 2f, 1, "RANDOM_EDGE", "error"));

        WaveManager waveManager = new WaveManager(waves, entityFactory);
        Camera camera = new Camera() {
            @Override
            public void update() {}
            @Override
            public void update(boolean updateFrustum) {}
        };
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        camera.position.set(400f, 300f, 0f);

        // Before any updates, gameTime is 0, which is < 10 and < 20, so not finished
        assertFalse(waveManager.isFinished());

        // Update to 15s -> Wave 1 is finished, but Wave 2 is not, so not finished
        waveManager.update(15f, camera);
        assertFalse(waveManager.isFinished());

        // Update to 25s -> both waves are finished
        waveManager.update(10f, camera);
        assertTrue(waveManager.isFinished());
    }
}
