package hust.adventure.wave;
 
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import hust.adventure.utils.GamePools;
 
/**
 * Strategy that spawns enemies at the edges of the camera view.
 */
public class RandomEdgeSpawnStrategy implements SpawnStrategy {
    private static final float MARGIN = 100f;
    private final Array<Vector2> positions = new Array<>();
 
    @Override
    public Array<Vector2> calculatePositions(Camera camera, int spawnCount) {
        positions.clear();
        if (spawnCount <= 0) return positions;
        
        float zoom = (camera instanceof OrthographicCamera) ? ((OrthographicCamera) camera).zoom : 1.0f;
        float w = camera.viewportWidth * zoom;
        float h = camera.viewportHeight * zoom;
 
        float cx = camera.position.x;
        float cy = camera.position.y;
 
        float left = cx - w / 2 - MARGIN;
        float right = cx + w / 2 + MARGIN;
        float bottom = cy - h / 2 - MARGIN;
        float top = cy + h / 2 + MARGIN;
 
        for (int i = 0; i < spawnCount; i++) {
            Vector2 pos = GamePools.obtain(Vector2.class);
            int side = MathUtils.random(3); // 0: Top, 1: Bottom, 2: Left, 3: Right
            
            switch (side) {
                case 0: // Top
                    pos.set(MathUtils.random(left, right), top);
                    break;
                case 1: // Bottom
                    pos.set(MathUtils.random(left, right), bottom);
                    break;
                case 2: // Left
                    pos.set(left, MathUtils.random(bottom, top));
                    break;
                case 3: // Right
                    pos.set(right, MathUtils.random(bottom, top));
                    break;
            }
            positions.add(pos);
        }
        return positions;
    }
}
