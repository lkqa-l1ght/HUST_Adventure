package hust.adventure.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.graphics.CameraManager;

public class InfiniteMapRenderer {
    private final CameraManager cameraManager;
    private final MapChunk baseChunk;
    private final float chunkWidth;
    private final float chunkHeight;
    private int visibleChunksX;
    private int visibleChunksY;

    public InfiniteMapRenderer(final CameraManager cameraManager, final MapChunk baseChunk) {
        if (cameraManager == null) {
            throw new IllegalArgumentException("CameraManager cannot be null");
        }
        if (baseChunk == null) {
            throw new IllegalArgumentException("MapChunk cannot be null");
        }
        this.cameraManager = cameraManager;
        this.baseChunk = baseChunk;
        
        this.chunkWidth = baseChunk.getTextureRegion().getRegionWidth();
        this.chunkHeight = baseChunk.getTextureRegion().getRegionHeight();
        
        calculateVisibleChunks();
    }

    private void calculateVisibleChunks() {
        final OrthographicCamera camera = cameraManager.getCamera();
        final float viewWidth = camera.viewportWidth * camera.zoom;
        final float viewHeight = camera.viewportHeight * camera.zoom;
        
        this.visibleChunksX = (int) Math.ceil(viewWidth / chunkWidth) + 2;
        this.visibleChunksY = (int) Math.ceil(viewHeight / chunkHeight) + 2;
    }

    public void update(final float delta) {
        calculateVisibleChunks();
    }

    public void draw(final SpriteBatch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("SpriteBatch cannot be null");
        }
        
        final OrthographicCamera camera = cameraManager.getCamera();
        final float camX = camera.position.x;
        final float camY = camera.position.y;

        final int centerCol = MathUtils.floor(camX / chunkWidth);
        final int centerRow = MathUtils.floor(camY / chunkHeight);

        final int startCol = centerCol - (visibleChunksX / 2);
        final int endCol = centerCol + (visibleChunksX / 2);
        
        final int startRow = centerRow - (visibleChunksY / 2);
        final int endRow = centerRow + (visibleChunksY / 2);

        for (int col = startCol; col <= endCol; col++) {
            for (int row = startRow; row <= endRow; row++) {
                final float drawX = col * chunkWidth;
                final float drawY = row * chunkHeight;
                batch.draw(baseChunk.getTextureRegion(), drawX, drawY, chunkWidth, chunkHeight);
            }
        }
    }
}
