package hust.adventure.items;

import hust.adventure.entities.player.Player;

/**
 * Capability interface for items that can be equipped and unequipped by the player.
 */
public interface Equipable {
    /**
     * Applies the equipment state and logic to the player.
     *
     * @param player The player entity instance.
     */
    void equip(final Player player);

    /**
     * Reverses the equipment state and logic from the player.
     *
     * @param player The player entity instance.
     */
    void unequip(final Player player);
}
