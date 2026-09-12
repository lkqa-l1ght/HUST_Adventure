package hust.adventure.items.base;

/**
 * Base data configuration class representing common properties of any item definition.
 */
public class ItemConfig {
    private String id;
    private String name;
    private String description;
    private String type;
    private String spritePath;

    /**
     * Gets the sprite/icon asset path of the item.
     *
     * @return the sprite path
     */
    public String getSpritePath() {
        return spritePath;
    }

    /**
     * Sets the sprite/icon asset path of the item.
     *
     * @param spritePath the sprite path
     */
    public void setSpritePath(final String spritePath) {
        this.spritePath = spritePath;
    }

    /**
     * Gets the unique identifier of the item.
     *
     * @return the item ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the item.
     *
     * @param id the item ID
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the display name of the item.
     *
     * @return the item name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the item.
     *
     * @param name the item name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the description text of the item.
     *
     * @return the item description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description text of the item.
     *
     * @param description the item description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Gets the type indicator of the item (e.g. "base" or "consumable").
     *
     * @return the item type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type indicator of the item (e.g. "base" or "consumable").
     *
     * @param type the item type
     */
    public void setType(final String type) {
        this.type = type;
    }
}
