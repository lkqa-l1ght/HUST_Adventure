package hust.adventure.entities.state;

import hust.adventure.entities.base.MapObject;

public class DeadState implements EntityState {
    @Override public void enter(MapObject entity) {
        entity.destroy();
    }
    @Override public void update(MapObject entity, float delta) {}
    @Override public void exit(MapObject entity) {}
    @Override public String getStateName() { return "DEAD"; }
}
