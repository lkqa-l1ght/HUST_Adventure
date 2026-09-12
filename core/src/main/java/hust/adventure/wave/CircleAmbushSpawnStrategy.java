package hust.adventure.wave;
 
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import hust.adventure.utils.GamePools;
 
/**
 * Strategy that spawns enemies in a circle around the camera.
 */
public class CircleAmbushSpawnStrategy implements SpawnStrategy {
    private static final float RADIUS_OFFSET = 150f;
    private final Array<Vector2> positions = new Array<>();
 
    @Override
    public Array<Vector2> calculatePositions(Camera camera, int spawnCount) {
        positions.clear();
        if (spawnCount <= 0) return positions;
 
        float zoom = (camera instanceof OrthographicCamera) ? ((OrthographicCamera) camera).zoom : 1.0f;
        float w = camera.viewportWidth * zoom;
        float h = camera.viewportHeight * zoom;
        float radius = Math.max(w, h) / 2 + RADIUS_OFFSET;
        
        float cx = camera.position.x;
        float cy = camera.position.y;
 
        float angleStep = 360f / spawnCount;
        float startAngle = MathUtils.random(360f);
 
        for (int i = 0; i < spawnCount; i++) {
            float angle = startAngle + i * angleStep;
            Vector2 pos = GamePools.obtain(Vector2.class);
            pos.set(cx + radius * MathUtils.cosDeg(angle), cy + radius * MathUtils.sinDeg(angle));
            positions.add(pos);
        }
        return positions;
    }
}
