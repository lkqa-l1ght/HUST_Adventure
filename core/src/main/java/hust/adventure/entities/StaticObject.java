package hust.adventure.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import hust.adventure.entities.base.MapObject;

/**
 * Generic static entity representing decorative map objects and static NPCs.
 * Handles both visual sprite objects and invisible logical boundaries.
 */
public class StaticObject extends MapObject {
    private final TextureRegion textureRegion;

    /**
     * Constructs a StaticObject with a visual sprite.
     *
     * @param x             bottom-left x coordinate
     * @param y             bottom-left y coordinate
     * @param width         width of the object
     * @param height        height of the object
     * @param textureRegion texture region of the tile sprite, or null if invisible
     */
    StaticObject(final float x, final float y, final float width, final float height, final TextureRegion textureRegion) {
        // MapObject treats position as center, so we offset bottom-left coordinates.
        super(x + width / 2f, y + height / 2f, width, height);
        this.textureRegion = textureRegion;
    }

    /**
     * Constructs a StaticObject with no direct visual sprite (e.g. static NPC).
     *
     * @param x             center x coordinate
     * @param y             center y coordinate
     * @param width         width of the object
     * @param height        height of the object
     */
    StaticObject(final float x, final float y, final float width, final float height) {
        super(x, y, width, height);
        this.textureRegion = null;
    }

    /**
     * Factory method to construct a StaticObject using bottom-left coordinates and a visual sprite.
     *
     * @param x             bottom-left x coordinate
     * @param y             bottom-left y coordinate
     * @param width         width of the object
     * @param height        height of the object
     * @param textureRegion texture region of the tile sprite, or null if invisible
     * @return a new StaticObject instance
     */
    public static StaticObject fromBottomLeft(final float x, final float y, final float width, final float height, final TextureRegion textureRegion) {
        return new StaticObject(x, y, width, height, textureRegion);
    }

    /**
     * Factory method to construct a StaticObject using center coordinates and no direct visual sprite.
     *
     * @param x             center x coordinate
     * @param y             center y coordinate
     * @param width         width of the object
     * @param height        height of the object
     * @return a new StaticObject instance
     */
    public static StaticObject fromCenter(final float x, final float y, final float width, final float height) {
        return new StaticObject(x, y, width, height);
    }

    @Override
    public void update(float delta) {
        // Static objects do not update
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (textureRegion != null) {
            if (rotation != 0f) {
                batch.draw(textureRegion, 
                           x - width / 2f, y - height / 2f, 
                           0f, 0f, 
                           width, height, 
                           1f, 1f, 
                           -rotation);
            } else {
                batch.draw(textureRegion, x - width / 2f, y - height / 2f, width, height);
            }
        }
    }
}
