package hust.adventure.entities.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import hust.adventure.collision.Collider;
import hust.adventure.entities.state.EntityState;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Base implementation of a game entity. Provides positioning, bounding box, and basic state management.
 */
public abstract class MapObject extends GameObject implements Collidable, Disposable {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float hitboxWidth;
    protected float hitboxHeight;
    protected Rectangle bounds;
    private boolean isDestroyed;
    private EntityState state;
    private Collider collider;
    private int zIndex = 0;
    private int subZIndex = 0;
    protected float sortingY = 0f;
    protected float rotation = 0f;
    protected String layerName;




    protected Animation<TextureRegion> animation;
    protected TextureRegion staticSprite;
    protected float stateTime = 0f;

    public MapObject() {
        super();
        this.bounds = new Rectangle();
        this.isDestroyed = false;
        this.state = EntityState.IDLE;
        this.hitboxWidth = 0f;
        this.hitboxHeight = 0f;
        this.sortingY = 0f;
    }

    public MapObject(final float x, final float y, final float width, final float height) {
        this(x, y, width, height, width, height);
    }

    public MapObject(final float x, final float y, final float width, final float height, final float hitboxWidth, final float hitboxHeight) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.bounds = new Rectangle(x - hitboxWidth / 2f, y - hitboxHeight / 2f, hitboxWidth, hitboxHeight);
        this.isDestroyed = false;
        this.state = EntityState.IDLE;
        this.sortingY = y;
    }

    public void update(float delta) {
        this.stateTime += delta;
    }

    public abstract void draw(SpriteBatch batch);

    public void drawHitbox(ShapeRenderer sr) {
        if (collider == null)
            return;

        sr.setColor(Color.RED);
        if (collider.getShape() == Collider.Shape.RECTANGLE) {
            sr.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        } else {
            sr.circle(x, y, collider.getRadius());
        }
    }

    public void destroy() {
        this.isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
    }

    public final float getX() {
        return x;
    }

    public void setX(final float x) {
        this.x = x;
        updateBounds();
    }

    public final float getY() {
        return y;
    }

    public void setY(final float y) {
        this.y = y;
        this.sortingY = y;
        updateBounds();
    }

    public final float getWidth() {
        return width;
    }

    public final float getHeight() {
        return height;
    }

    @Override
    public final Rectangle getBounds() {
        return bounds;
    }

    @Override
    public final float getHitboxWidth() {
        return hitboxWidth;
    }

    public final void setHitboxWidth(final float hitboxWidth) {
        this.hitboxWidth = hitboxWidth;
        updateBounds();
    }

    @Override
    public final float getHitboxHeight() {
        return hitboxHeight;
    }

    public final void setHitboxHeight(final float hitboxHeight) {
        this.hitboxHeight = hitboxHeight;
        updateBounds();
    }

    /**
     * Synchronize collision bounds with current position.
     */
    protected final void updateBounds() {
        this.bounds.set(x - hitboxWidth / 2f, y - hitboxHeight / 2f, hitboxWidth, hitboxHeight);
    }

    public final EntityState getState() {
        return state;
    }

    public void setState(final EntityState newState) {
        if (newState == null)
            throw new IllegalArgumentException("State cannot be null");
        if (this.state == newState) {
            return;
        }

        if (this.state != null) {
            this.state.exit(this);
        }
        this.state = newState;
        this.state.enter(this);
    }

    public final Collider getCollider() {
        return collider;
    }

    public void setCollider(final Collider collider) {
        this.collider = collider;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(final int zIndex) {
        this.zIndex = zIndex;
    }

    public int getSubZIndex() {
        return subZIndex;
    }

    public void setSubZIndex(final int subZIndex) {
        this.subZIndex = subZIndex;
    }

    public float getSortingY() {
        return sortingY;
    }

    public void setSortingY(final float sortingY) {
        this.sortingY = sortingY;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(final float rotation) {
        this.rotation = rotation;
    }


    @Override
    public void dispose() {
    }

    public Rectangle getMovementBounds(float x, float y, Rectangle out) {
        return out.set(x - hitboxWidth / 2f, y - hitboxHeight / 2f, hitboxWidth, hitboxHeight);
    }

    public void setAnimation(final Animation<TextureRegion> animation) {
        this.animation = animation;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public void setStaticSprite(final TextureRegion staticSprite) {
        this.staticSprite = staticSprite;
    }

    public TextureRegion getStaticSprite() {
        return staticSprite;
    }

    public boolean hasSprite() {
        return staticSprite != null || animation != null;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(final float stateTime) {
        this.stateTime = stateTime;
    }

    /**
     * Returns the fallback shape color for entities without sprite assets.
     * Subclasses override to indicate shape-only rendering.
     * Returns null by default (use sprite rendering via draw()).
     *
     * @return fallback color, or null if entity renders via draw()
     */
    public Color getShapeFallbackColor() {
        return null;
    }
}
