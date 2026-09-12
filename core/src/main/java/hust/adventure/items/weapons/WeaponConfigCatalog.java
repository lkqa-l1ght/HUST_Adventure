package hust.adventure.items.weapons;

import com.badlogic.gdx.utils.Array;

/**
 * Catalog helper wrapping a list of WeaponConfigs for GDX JSON parsing.
 */
public class WeaponConfigCatalog {
    private Array<WeaponConfig> weapons;

    public Array<WeaponConfig> getWeapons() {
        return weapons;
    }

    public void setWeapons(final Array<WeaponConfig> weapons) {
        this.weapons = weapons;
    }
}
