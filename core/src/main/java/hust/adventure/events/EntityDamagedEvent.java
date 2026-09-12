package hust.adventure.events;
 
import hust.adventure.entities.base.MapObject;
 
public class EntityDamagedEvent {
    private final MapObject entity;
    private final float amount;
    private final boolean isCrit;
 
    public EntityDamagedEvent(MapObject entity, float amount, boolean isCrit) {
        if (entity == null) throw new NullPointerException("entity cannot be null");
        this.entity = entity;
        this.amount = amount;
        this.isCrit = isCrit;
    }
 
    public MapObject getEntity() {
        return entity;
    }
 
    public float getAmount() {
        return amount;
    }
 
    public boolean isCrit() {
        return isCrit;
    }
}
