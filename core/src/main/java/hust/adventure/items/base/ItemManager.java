package hust.adventure.items.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry and manager for all item definitions.
 */
public final class ItemManager {
    private final Map<String, Item> items;

    public ItemManager() {
        items = new HashMap<>();
    }

    /**
     * Registers an item definition.
     *
     * @param item the item to register
     */
    public void register(final Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.put(item.getId(), item);
    }

    /**
     * Retrieves an item definition by its ID.
     *
     * @param id the item ID
     * @return the registered Item, or null if not found
     */
    public Item getItem(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return items.get(id);
    }

    /**
     * Retrieves all registered items.
     *
     * @return a collection of all registered Item definitions
     */
    public java.util.Collection<Item> getAllItems() {
        return items.values();
    }
}
