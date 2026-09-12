package hust.adventure.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Data Object for a background map chunk.
 */
public class MapChunk {
    private final TextureRegion textureRegion;

    public MapChunk(final TextureRegion textureRegion) {
        if (textureRegion == null) {
            throw new IllegalArgumentException("TextureRegion cannot be null");
        }
        this.textureRegion = textureRegion;
    }

    public TextureRegion getTextureRegion() {
        return textureRegion;
    }
}
