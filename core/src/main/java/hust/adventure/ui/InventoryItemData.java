package hust.adventure.ui;

/**
 * Data Transfer Object for a single item in the inventory screen.
 */
public class InventoryItemData {
    private final String id;
    private final String name;
    private final String description;
    private final String spritePath;
    private final int count;

    public InventoryItemData(final String id, final String name, final String description, final String spritePath, final int count) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.spritePath = spritePath;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public int getCount() {
        return count;
    }
}
