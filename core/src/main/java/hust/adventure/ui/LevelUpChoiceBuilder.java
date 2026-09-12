package hust.adventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import hust.adventure.core.data.GearDataLoader;
import hust.adventure.core.data.WeaponDataLoader;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.items.weapons.WeaponFactory;
import hust.adventure.items.gear.GearFactory;
import hust.adventure.entities.player.Player;
import hust.adventure.items.gear.Gear;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.ui.components.DamageIncreaseAction;
import hust.adventure.ui.components.GearUpgradeAction;
import hust.adventure.ui.components.HealAction;
import hust.adventure.ui.components.UpgradeAction;
import hust.adventure.ui.components.WeaponUpgradeAction;
import lombok.Builder;

/**
 * Builder class that generates random upgrade choices for a player when leveling up. Evaluates the player's current
 * weapons and gears to determine possible upgrades.
 */
public class LevelUpChoiceBuilder {
    private final GearDataLoader gearDataManager;
    private final WeaponDataLoader weaponDataManager;
    private final GameProgressContext progressContext;
    private final WeaponFactory weaponFactory;
    private final GearFactory gearFactory;

    /**
     * Constructs a new LevelUpChoiceBuilder with dependencies.
     */
    @Builder
    public LevelUpChoiceBuilder(final GearDataLoader gearDataManager, final WeaponDataLoader weaponDataManager,
            final GameProgressContext progressContext,
            final WeaponFactory weaponFactory, final GearFactory gearFactory) {
        this.gearDataManager = gearDataManager;
        this.weaponDataManager = weaponDataManager;
        this.progressContext = progressContext;
        this.weaponFactory = weaponFactory;
        this.gearFactory = gearFactory;
    }

    /**
     * Gathers all eligible weapon and gear upgrade actions, shuffles them, and returns up to 3 upgrade choices for the
     * player.
     *
     * @param player The player who leveled up and is choosing an upgrade.
     * @return An Array of UpgradeAction choices.
     */
    public Array<UpgradeAction> getLevelUpChoices(final Player player) {
        if (player == null) {
            return new Array<>();
        }
        final Array<UpgradeAction> possibleChoices = new Array<>();

        // 1. Gather Weapon Choices
        if (weaponDataManager == null) {
            Gdx.app.error("LevelUpChoiceBuilder", "WeaponDataManager is null!");
        }
        final Array<String> weaponIds = weaponDataManager != null ? weaponDataManager.getAllWeaponIds() : new Array<>();
        for (final String id : weaponIds) {
            BaseWeapon weapon = null;
            for (final BaseWeapon w : player.getWeaponManager().getWeapons()) {
                if (w.getId().equalsIgnoreCase(id)) {
                    weapon = w;
                    break;
                }
            }

            if (weapon == null) {
                possibleChoices.add(new WeaponUpgradeAction(id, weaponDataManager.getWeaponName(id),
                        weaponDataManager.getWeaponLevelDescription(id, 1), true, weaponFactory));
            } else if (weapon.getLevel() < 5) {
                final int nextLevel = weapon.getLevel() + 1;
                possibleChoices
                        .add(new WeaponUpgradeAction(id, weaponDataManager.getWeaponName(id) + " (Cấp " + nextLevel + ")",
                                weaponDataManager.getWeaponLevelDescription(id, nextLevel), false, weaponFactory));
            }
        }

        // 2. Gather Gear Choices
        if (gearDataManager == null) {
            Gdx.app.error("LevelUpChoiceBuilder", "GearDataManager is null!");
        }
        final Array<String> gearIds = gearDataManager != null ? gearDataManager.getAllGearIds() : new Array<>();
        for (final String id : gearIds) {
            final Gear gear = player.getGearManager().getGear(id);
            if (gear == null) {
                possibleChoices.add(new GearUpgradeAction(id, gearDataManager.getGearName(id),
                        gearDataManager.getGearLevelDescription(id, 1), true, gearFactory));
            } else if (gear.getLevel() < 5) {
                final int nextLevel = gear.getLevel() + 1;
                possibleChoices
                        .add(new GearUpgradeAction(id, gearDataManager.getGearName(id) + " (Cấp " + nextLevel + ")",
                                gearDataManager.getGearLevelDescription(id, nextLevel), false, gearFactory));
            }
        }

        // Fallbacks if nothing is available
        if (possibleChoices.size == 0) {
            possibleChoices.add(new HealAction());
            possibleChoices.add(new DamageIncreaseAction(progressContext));
        }

        possibleChoices.shuffle();
        final Array<UpgradeAction> finalChoices = new Array<>();
        for (int i = 0; i < Math.min(3, possibleChoices.size); i++) {
            finalChoices.add(possibleChoices.get(i));
        }
        return finalChoices;
    }
}
