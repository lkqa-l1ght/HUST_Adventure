package hust.adventure.entities;

import com.badlogic.gdx.math.Rectangle;
import hust.adventure.collision.Collider;
import hust.adventure.collision.CollisionLayer;
import hust.adventure.entities.base.Collidable;

/**
 * Static wall entity for collisions.
 */
public class WallEntity implements Collidable {
    private final Rectangle bounds;
    private final Collider collider;
    private final float x;
    private final float y;

    public WallEntity(Rectangle rect) {
        this.bounds = new Rectangle(rect);
        this.x = rect.x + rect.width / 2f;
        this.y = rect.y + rect.height / 2f;
        this.collider = new Collider(this, CollisionLayer.WALL, Collider.Shape.RECTANGLE);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getWidth() {
        return bounds.width;
    }

    @Override
    public float getHeight() {
        return bounds.height;
    }

    @Override
    public float getHitboxWidth() {
        return bounds.width;
    }

    @Override
    public float getHitboxHeight() {
        return bounds.height;
    }

    @Override
    public Collider getCollider() {
        return collider;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
