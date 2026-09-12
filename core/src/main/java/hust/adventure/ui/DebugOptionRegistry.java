package hust.adventure.ui;

import hust.adventure.core.data.EnemyDataLoader;
import hust.adventure.core.data.GearDataLoader;
import hust.adventure.core.data.ItemDataLoader;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.core.data.LevelDataLoader;
import hust.adventure.core.data.WeaponDataLoader;
import hust.adventure.entities.enemies.EnemyConfig;
import hust.adventure.items.base.ItemConfig;
import hust.adventure.entities.player.Player;
import hust.adventure.items.weapons.BaseWeapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Registry containing dynamic lists of debug options for maps, items, and monsters.
 */
public class DebugOptionRegistry {
    private final EnemyDataLoader enemyDataManager;
    private final ItemDataLoader itemDataManager;
    private final WeaponDataLoader weaponDataManager;
    private final GearDataLoader gearDataManager;
    private final LevelDataLoader levelDataManager;

    /**
     * Constructs a new DebugOptionRegistry with dependencies.
     */
    public DebugOptionRegistry(final EnemyDataLoader enemyDataManager, final ItemDataLoader itemDataManager,
            final WeaponDataLoader weaponDataManager, final GearDataLoader gearDataManager,
            final LevelDataLoader levelDataManager) {
        this.enemyDataManager = enemyDataManager;
        this.itemDataManager = itemDataManager;
        this.weaponDataManager = weaponDataManager;
        this.gearDataManager = gearDataManager;
        this.levelDataManager = levelDataManager;
    }

    private DebugOption[] getMapOptions() {
        final List<DebugOption> list = new ArrayList<>();
        if (levelDataManager != null) {
            for (final LevelConfig config : levelDataManager.getAllConfigs()) {
                list.add(new DebugOption(config.getMapPath(), config.getName()));
            }
        }
        return list.toArray(new DebugOption[0]);
    }

    private DebugOption[] getItemOptions() {
        final List<DebugOption> list = new ArrayList<>();
        if (itemDataManager != null) {
            for (final ItemConfig config : itemDataManager.getAllConfigs()) {
                final String id = config.getId();
                if (weaponDataManager != null && weaponDataManager.getAllWeaponIds().contains(id, false)) {
                    continue; // Skip weapon dummy items from items.json
                }
                if (gearDataManager != null && gearDataManager.getAllGearIds().contains(id, false)) {
                    continue; // Skip gear dummy items from items.json
                }
                list.add(new DebugOption(id, config.getName()));
            }
        }
        return list.toArray(new DebugOption[0]);
    }

    private DebugOption[] getEquipOptions(final Player player) {
        final List<DebugOption> list = new ArrayList<>();

        if (weaponDataManager != null) {
            for (final String id : weaponDataManager.getAllWeaponIds()) {
                final String name = weaponDataManager.getWeaponName(id);
                boolean isEquipped = false;
                if (player != null && player.getWeaponManager() != null) {
                    for (final BaseWeapon w : player.getWeaponManager().getWeapons()) {
                        if (w.getId().equalsIgnoreCase(id)) {
                            isEquipped = true;
                            break;
                        }
                    }
                }
                final String displayName = isEquipped ? "[Equipped] " + name + " (Weapon)" : name + " (Weapon)";
                list.add(new DebugOption("weapon_" + id, displayName));
            }
        }

        if (gearDataManager != null) {
            for (final String id : gearDataManager.getAllGearIds()) {
                final String name = gearDataManager.getGearName(id);
                boolean isEquipped = false;
                if (player != null && player.getGearManager() != null) {
                    isEquipped = (player.getGearManager().getGear(id) != null);
                }
                final String displayName = isEquipped ? "[Equipped] " + name + " (Gear)" : name + " (Gear)";
                list.add(new DebugOption("gear_" + id, displayName));
            }
        }

        return list.toArray(new DebugOption[0]);
    }

    private DebugOption[] getMonsterOptions() {
        final List<DebugOption> list = new ArrayList<>();
        if (enemyDataManager != null) {
            for (final String type : enemyDataManager.getAllEnemyTypes()) {
                final EnemyConfig config = enemyDataManager.getEnemyConfig(type);
                final String displayName = config != null ? config.getName() : type;
                list.add(new DebugOption(type, displayName));
            }
        }
        return list.toArray(new DebugOption[0]);
    }

    /**
     * Gets the corresponding array of debug options for the given SelectionMode.
     *
     * @param mode   the selection mode
     * @param player the player instance
     * @return an array of DebugOption, or null if the mode is NONE or invalid
     */
    public DebugOption[] getOptions(final SelectionMode mode, final Player player) {
        if (mode == null) {
            return null;
        }
        switch (mode) {
        case MAP:
            return getMapOptions();
        case ITEM:
            return getItemOptions();
        case EQUIP:
            return getEquipOptions(player);
        case MONSTER:
            return getMonsterOptions();
        default:
            return null;
        }
    }

    /**
     * Finds the LevelConfig for a given map path.
     *
     * @param mapPath the map path to search for
     * @return an Optional containing the LevelConfig if found, or empty if not
     */
    public Optional<LevelConfig> getLevelConfigByMapPath(final String mapPath) {
        if (levelDataManager == null || mapPath == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(levelDataManager.getLevelConfigByMapPath(mapPath));
    }
}
