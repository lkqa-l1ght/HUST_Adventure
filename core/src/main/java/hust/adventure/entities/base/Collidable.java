package hust.adventure.entities.base;

import com.badlogic.gdx.math.Rectangle;

import hust.adventure.collision.Collider;

/**
 * Interface for entities that can collide.
 */
public interface Collidable {
    Rectangle getBounds();
    float getX();
    float getY();
    float getWidth();
    float getHeight();
    float getHitboxWidth();
    float getHitboxHeight();
    Collider getCollider();
    boolean isDestroyed();
}
