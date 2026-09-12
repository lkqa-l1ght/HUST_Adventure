package hust.adventure.wave;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Interface for enemy spawning formations.
 */
public interface SpawnStrategy {
    /**
     * Calculates spawn positions based on camera view and count.
     * Implementations should obtain Vector2 instances from GamePools.
     */
    Array<Vector2> calculatePositions(Camera camera, int spawnCount);
}
