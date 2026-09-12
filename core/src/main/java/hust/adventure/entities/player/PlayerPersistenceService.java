package hust.adventure.entities.player;

import hust.adventure.core.context.GameProgressContext;
import hust.adventure.items.gear.Gear;
import hust.adventure.items.gear.GearFactory;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponFactory;

import java.util.Map;

/**
 * Service to handle saving and restoring Player stats, weapons, and gears to/from the global ProgressContext.
 */
public class PlayerPersistenceService {
    private final GameProgressContext progressContext;
    private final GearFactory gearFactory;
    private final WeaponFactory weaponFactory;

    public PlayerPersistenceService(final GameProgressContext progressContext, final GearFactory gearFactory,
            final WeaponFactory weaponFactory) {
        this.progressContext = progressContext;
        this.gearFactory = gearFactory;
        this.weaponFactory = weaponFactory;
    }

    /**
     * Saves the player's active weapons and gears levels to the ProgressContext.
     *
     * @param player The player entity instance.
     */
    public void save(final Player player) {
        if (progressContext == null || player == null) {
            return;
        }

        progressContext.getWeaponLevels().clear();
        for (final BaseWeapon weapon : player.getWeaponManager().getWeapons()) {
            progressContext.getWeaponLevels().put(weapon.getId().toLowerCase(), weapon.getLevel());
        }

        progressContext.getGearLevels().clear();
        if (player.getGearManager() != null) {
            for (final Gear gear : player.getGearManager().getGears()) {
                progressContext.getGearLevels().put(gear.getId().toLowerCase(), gear.getLevel());
            }
        }
    }

    /**
     * Restores the player's weapons and gears from the ProgressContext.
     *
     * @param player The player entity instance.
     */
    public void restore(final Player player) {
        if (progressContext == null || player == null) {
            return;
        }

        final Map<String, Integer> savedWeapons = progressContext.getWeaponLevels();
        final Map<String, Integer> savedGears = progressContext.getGearLevels();

        // Restore weapons
        if (savedWeapons.isEmpty()) {
            // Initialize with default weapon for a new game
            if (weaponFactory != null) {
                final BaseWeapon defaultWeapon = weaponFactory.createWeapon("bun_dau", player);
                defaultWeapon.equip(player);
            }
            savedWeapons.put("bun_dau", 1);
        } else {
            for (final Map.Entry<String, Integer> entry : savedWeapons.entrySet()) {
                final String weaponId = entry.getKey();
                final int targetLevel = entry.getValue();
                if (weaponFactory != null) {
                    final BaseWeapon weapon = weaponFactory.createWeapon(weaponId, player);
                    for (int i = 1; i < targetLevel; i++) {
                        weapon.upgrade(0f, 0f);
                    }
                    weapon.equip(player);
                }
            }
        }

        // Restore gears
        if (player.getGearManager() != null) {
            for (final Map.Entry<String, Integer> entry : savedGears.entrySet()) {
                final String gearId = entry.getKey();
                final int targetLevel = entry.getValue();
                final Gear gear = gearFactory != null ? gearFactory.createGear(gearId) : new Gear(gearId, gearId, "", gearFactory);
                for (int i = 1; i < targetLevel; i++) {
                    gear.upgrade();
                }
                gear.equip(player);

                // Re-apply cumulative equip effects for restored levels > 1
                if (gearFactory != null) {
                    for (int i = 1; i < targetLevel; i++) {
                        gearFactory.applyEquipEffect(gearId, player);
                    }
                }
            }
        }
    }
}
