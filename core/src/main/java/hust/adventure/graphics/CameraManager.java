package hust.adventure.graphics;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.entities.base.MapObject;

/**
 * System for managing the game camera, including following targets and clamping to map bounds.
 */
public class CameraManager {
    private OrthographicCamera camera;
    private MapObject target;
    private float mapWidth, mapHeight;
    private float lerp = 0.1f;
    private boolean isInfinite;

    public CameraManager(float viewportWidth, float viewportHeight) {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
    }

    public void setTarget(MapObject target) {
        this.target = target;
    }

    public void setMapBounds(float width, float height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public void setInfinite(boolean infinite) {
        this.isInfinite = infinite;
    }

    public void setZoom(float zoom) {
        camera.zoom = zoom;
    }

    public void update() {
        if (target != null) {
            float targetX = target.getX();
            float targetY = target.getY();

            // Smooth follow
            camera.position.x = MathUtils.lerp(camera.position.x, targetX, lerp);
            camera.position.y = MathUtils.lerp(camera.position.y, targetY, lerp);

            // Clamp to map bounds
            if (!isInfinite) {
                float halfWidth = (camera.viewportWidth * camera.zoom) / 2f;
                float halfHeight = (camera.viewportHeight * camera.zoom) / 2f;

                if (mapWidth > camera.viewportWidth * camera.zoom) {
                    camera.position.x = MathUtils.clamp(camera.position.x, halfWidth, mapWidth - halfWidth);
                }

                if (mapHeight > camera.viewportHeight * camera.zoom) {
                    camera.position.y = MathUtils.clamp(camera.position.y, halfHeight, mapHeight - halfHeight);
                }
            }
        }
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
