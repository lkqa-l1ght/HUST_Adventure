package hust.adventure.items.weapons;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import hust.adventure.entities.player.Player;

/**
 * Manages the collection of weapons for the player.
 */
public class WeaponManager {
    private final Player player;
    private final Array<BaseWeapon> weapons;

    public WeaponManager(final Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        this.player = player;
        this.weapons = new Array<>();
    }

    /**
     * Updates all equipped weapons.
     * 
     * @param delta Time elapsed.
     */
    public void update(final float delta) {
        for (final BaseWeapon weapon : weapons) {
            weapon.updateTimer(delta);
        }
    }

    /**
     * Draws effects for all weapons.
     * 
     * @param batch The SpriteBatch to use.
     */
    public void draw(final SpriteBatch batch) {
        for (final BaseWeapon weapon : weapons) {
            weapon.draw(batch);
        }
    }

    /**
     * Adds a new weapon to the player.
     * 
     * @param weapon The weapon to add.
     */
    public void addWeapon(final BaseWeapon weapon) {
        if (weapon == null) {
            throw new IllegalArgumentException("Weapon cannot be null");
        }
        weapons.add(weapon);
    }

    public Array<BaseWeapon> getWeapons() {
        return weapons;
    }
}
