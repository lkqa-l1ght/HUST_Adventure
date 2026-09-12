package hust.adventure.entities.state;

import hust.adventure.entities.base.MapObject;

public class IdleState implements EntityState {
    @Override public void enter(MapObject entity) {}
    @Override public void update(MapObject entity, float delta) {}
    @Override public void exit(MapObject entity) {}
    @Override public String getStateName() { return "IDLE"; }
}
