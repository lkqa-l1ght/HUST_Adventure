package hust.adventure.items.weapons;

import com.badlogic.gdx.utils.ObjectMap;

import hust.adventure.core.data.WeaponDataLoader;
import hust.adventure.entities.player.Player;
import hust.adventure.items.weapons.impl.BunDauWeapon;
import hust.adventure.items.weapons.impl.GarlicAuraWeapon;
import hust.adventure.items.weapons.impl.MagicWandWeapon;
import hust.adventure.items.weapons.impl.WhipWeapon;

/**
 * Factory for creating weapons using a data-driven approach and registry pattern.
 */
public class WeaponFactory {
    private final WeaponDataLoader weaponDataManager;
    private final ObjectMap<String, WeaponProvider> providers;

    /**
     * Functional interface for weapon creation.
     */
    public interface WeaponProvider {
        BaseWeapon create(Player player, WeaponConfig config);
    }

    public WeaponFactory(final WeaponDataLoader weaponDataManager) {
        if (weaponDataManager == null) {
            throw new IllegalArgumentException("WeaponDataManager cannot be null");
        }
        this.weaponDataManager = weaponDataManager;
        this.providers = new ObjectMap<>();
        registerDefaultProviders();
    }

    private void registerDefaultProviders() {
        providers.put("whip", WhipWeapon::new);
        providers.put("magic_wand", MagicWandWeapon::new);
        providers.put("garlic", GarlicAuraWeapon::new);
        providers.put("bun_dau", BunDauWeapon::new);
    }

    public BaseWeapon createWeapon(final String id, final Player player) {
        if (id == null) {
            throw new IllegalArgumentException("Weapon ID cannot be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        final WeaponConfig config = weaponDataManager.getConfig(id);
        if (config == null) {
            throw new IllegalArgumentException("No configuration found for weapon ID: " + id);
        }

        final WeaponProvider provider = providers.get(id);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown weapon id or missing provider: " + id);
        }

        return provider.create(player, config);
    }
}
