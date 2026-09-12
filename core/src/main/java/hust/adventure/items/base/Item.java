package hust.adventure.items.base;

import hust.adventure.entities.base.GameObject;

/**
 * Abstract class for all logical items in the game. Inherits from GameObject to reuse identity attributes (ID, Name,
 * spritePath) and introduces a description field.
 */
public class Item extends GameObject {
    protected String description;

    public Item() {
        super();
    }

    public Item(final String id, final String name, final String description, final String spritePath) {
        super(id, name, spritePath);
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.description = description;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Item))
            return false;
        final Item item = (Item) o;
        return java.util.Objects.equals(getId(), item.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(getId());
    }

    @Override
    public String toString() {
        return getName() + " (" + getId() + ")";
    }
}
