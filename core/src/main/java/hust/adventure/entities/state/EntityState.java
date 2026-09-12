package hust.adventure.entities.state;

import hust.adventure.entities.base.MapObject;

/**
 * Interface for GameEntity states (State Pattern).
 */
public interface EntityState {
    EntityState IDLE = new IdleState();
    EntityState MOVING = new MovingState();
    EntityState DEAD = new DeadState();

    void enter(MapObject entity);
    void update(MapObject entity, float delta);
    void exit(MapObject entity);
    
    // For compatibility with old enum checks if needed, or just use instanceof
    String getStateName();
}
