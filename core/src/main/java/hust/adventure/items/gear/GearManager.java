package hust.adventure.items.gear;

import com.badlogic.gdx.utils.Array;

/**
 * Manages the collection of passive Gear items for the player.
 */
public class GearManager {
    private final Array<Gear> gears;

    public GearManager() {
        this.gears = new Array<>();
    }

    /**
     * Gets all currently equipped gears.
     * 
     * @return Array of Gear items.
     */
    public Array<Gear> getGears() {
        return gears;
    }

    /**
     * Finds an equipped gear by its ID.
     * 
     * @param id The unique identifier of the gear.
     * @return The Gear object, or null if not equipped.
     */
    public Gear getGear(final String id) {
        if (id == null) {
            return null;
        }
        for (final Gear gear : gears) {
            if (gear.getId().equalsIgnoreCase(id)) {
                return gear;
            }
        }
        return null;
    }

    /**
     * Equips a new gear item.
     * 
     * @param gear The gear to equip.
     */
    public void addGear(final Gear gear) {
        if (gear == null) {
            throw new IllegalArgumentException("Gear cannot be null");
        }
        if (getGear(gear.getId()) == null) {
            gears.add(gear);
        }
    }
}
