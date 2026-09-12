package hust.adventure.items.consumable;

import hust.adventure.entities.base.Character;

/**
 * Interface for items that can be consumed by an actor.
 */
public interface Consumable {
    /**
     * Consumes the item, applying its effects to the consumer.
     * 
     * @param consumer The actor consuming the item.
     */
    void consume(Character consumer);
}
