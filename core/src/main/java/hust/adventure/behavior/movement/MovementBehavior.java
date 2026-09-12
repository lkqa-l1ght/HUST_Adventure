package hust.adventure.behavior.movement;

import hust.adventure.entities.base.MapObject;

/**
 * Interface for movement strategies.
 */
public interface MovementBehavior {
    /**
     * Update the entity's position based on its movement strategy.
     * 
     * @param entity The entity to move.
     * @param delta  Time delta.
     */
    void update(MapObject entity, float delta);
}
