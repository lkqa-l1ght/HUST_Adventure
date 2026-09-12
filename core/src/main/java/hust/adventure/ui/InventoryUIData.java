package hust.adventure.ui;

import java.util.List;

/**
 * Data Transfer Object containing a list of items to render in the inventory screen.
 */
public class InventoryUIData {
    private final List<InventoryItemData> items;

    public InventoryUIData(final List<InventoryItemData> items) {
        this.items = items;
    }

    public List<InventoryItemData> getItems() {
        return items;
    }
}
