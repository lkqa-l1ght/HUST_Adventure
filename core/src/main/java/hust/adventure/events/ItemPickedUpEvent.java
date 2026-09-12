package hust.adventure.events;

import hust.adventure.entities.base.MapObject;
import hust.adventure.items.base.Item;

/**
 * Payload for the ITEM_PICKED_UP event.
 */
public class ItemPickedUpEvent {
    private final Item item;
    private final MapObject picker;

    public ItemPickedUpEvent(Item item, MapObject picker) {
        if (item == null)
            throw new NullPointerException("item cannot be null");
        if (picker == null)
            throw new NullPointerException("picker cannot be null");
        this.item = item;
        this.picker = picker;
    }

    public Item getItem() {
        return item;
    }

    public MapObject getPicker() {
        return picker;
    }
}
