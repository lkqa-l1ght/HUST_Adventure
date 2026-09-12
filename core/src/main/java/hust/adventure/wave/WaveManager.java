package hust.adventure.wave;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.Gdx;
import hust.adventure.core.TimeProvider;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.TimeLimitReachedEvent;
import hust.adventure.utils.GamePools;

/**
 * Manages game time and enemy spawning based on WaveEntry configurations.
 * Optimized for zero heap allocations during the game loop.
 */
public class WaveManager implements TimeProvider {
    private final Array<WaveEntry> waves;
    private final EntityFactory entityFactory;
    private final Array<ActiveWave> allWaves;
    private final Array<ActiveWave> activeWaves;
    private final ObjectMap<String, SpawnStrategy> strategies;

    private float gameTime = 0f;
    private final float maxTime = 1800f; // 30 minutes
    private boolean limitReached = false;

    /**
     * Constructs a WaveManager with a specific list of wave entries.
     *
     * @param waves         the wave configurations for the current level (null-safe)
     * @param entityFactory the factory used to spawn enemies
     */
    public WaveManager(final Array<WaveEntry> waves, final EntityFactory entityFactory) {
        if (entityFactory == null) {
            throw new IllegalArgumentException("EntityFactory cannot be null");
        }

        this.waves = waves != null ? waves : new Array<>();
        this.entityFactory = entityFactory;
        this.allWaves = new Array<>(this.waves.size);
        this.activeWaves = new Array<>(this.waves.size);
        this.strategies = new ObjectMap<>();

        for (int i = 0; i < this.waves.size; i++) {
            allWaves.add(new ActiveWave(this.waves.get(i)));
        }

        // Register default strategies
        strategies.put("RANDOM_EDGE", new RandomEdgeSpawnStrategy());
        strategies.put("CIRCLE_AMBUSH", new CircleAmbushSpawnStrategy());
    }

    @Override
    public float getCurrentTime() {
        return gameTime;
    }

    @Override
    public float getMaxTime() {
        return maxTime;
    }

    /**
     * Updates game time and coordinates wave spawning.
     * Guaranteed to trigger zero garbage collection allocations.
     *
     * @param delta  the elapsed time since the last frame in seconds
     * @param camera the camera representing the player's viewport
     */
    public void update(final float delta, final Camera camera) {
        if (!limitReached) {
            gameTime += delta;
            if (gameTime >= maxTime) {
                limitReached = true;
                gameTime = maxTime;

                final TimeLimitReachedEvent data = new TimeLimitReachedEvent();
                final GameEvent<TimeLimitReachedEvent> event = new GameEvent<>(EventType.TIME_LIMIT_REACHED, data);
                EventDispatcher.getInstance().dispatch(event);
            }
        }

        if (limitReached) {
            return; // Stop normal spawning
        }

        // Check for new waves to activate
        for (int i = 0; i < allWaves.size; i++) {
            final ActiveWave wave = allWaves.get(i);
            if (gameTime >= wave.config.getTimeStart() && gameTime <= wave.config.getTimeEnd()) {
                if (!isActive(wave)) {
                    wave.spawnCooldown = 0f; // Start spawning immediately
                    activeWaves.add(wave);
                }
            }
        }

        // Update active waves
        for (int i = activeWaves.size - 1; i >= 0; i--) {
            final ActiveWave wave = activeWaves.get(i);

            // Deactivate if time passed
            if (gameTime > wave.config.getTimeEnd()) {
                activeWaves.removeIndex(i);
                continue;
            }

            wave.update(delta, camera);
        }
    }

    private boolean isActive(final ActiveWave target) {
        for (int i = 0; i < activeWaves.size; i++) {
            if (activeWaves.get(i) == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the current elapsed game time.
     *
     * @return the elapsed game time in seconds
     */
    public float getGameTime() {
        return gameTime;
    }

    /**
     * Checks if all waves have finished spawning.
     *
     * @return true if all waves have finished spawning, false otherwise
     */
    public boolean isFinished() {
        for (int i = 0; i < allWaves.size; i++) {
            if (gameTime < allWaves.get(i).config.getTimeEnd()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runtime wrapper for a WaveEntry to track state.
     */
    private class ActiveWave {
        final WaveEntry config;
        float spawnCooldown = 0f;

        ActiveWave(final WaveEntry config) {
            this.config = config;
        }

        void update(final float delta, final Camera camera) {
            if (config.getSpawnInterval() <= 0) {
                // One-shot spawn (if timeEnd == timeStart or interval is 0)
                if (spawnCooldown == 0) {
                    spawn(camera);
                    spawnCooldown = -1f; // Mark as spawned
                }
                return;
            }

            spawnCooldown -= delta;
            if (spawnCooldown <= 0) {
                spawn(camera);
                spawnCooldown = config.getSpawnInterval();
            }
        }

        void spawn(final Camera camera) {
            final SpawnStrategy strategy = strategies.get(config.getPattern());
            if (strategy == null) {
                Gdx.app.error("WaveManager", "Unknown pattern: " + config.getPattern());
                return;
            }

            final Array<Vector2> positions = strategy.calculatePositions(camera, config.getSpawnCount());
            for (int i = 0; i < positions.size; i++) {
                final Vector2 pos = positions.get(i);
                entityFactory.createEnemy(config.getEnemyType(), pos.x, pos.y);
                GamePools.free(pos);
            }
            positions.clear();
        }
    }
}
