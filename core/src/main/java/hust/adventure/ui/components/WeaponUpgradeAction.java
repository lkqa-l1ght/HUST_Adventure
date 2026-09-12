package hust.adventure.ui.components;

import hust.adventure.entities.player.Player;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponFactory;

/**
 * Action representing a choice to unlock a new weapon or upgrade an existing one.
 */
public class WeaponUpgradeAction implements UpgradeAction {
    private final String weaponId;
    private final String name;
    private final String description;
    private final boolean isUnlock;
    private final WeaponFactory weaponFactory;

    public WeaponUpgradeAction(final String weaponId, final String name, final String description,
            final boolean isUnlock, final WeaponFactory weaponFactory) {
        if (weaponId == null) {
            throw new IllegalArgumentException("Weapon ID cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.weaponId = weaponId;
        this.name = name;
        this.description = description;
        this.isUnlock = isUnlock;
        this.weaponFactory = weaponFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(final Player player) {
        if (player == null) {
            return;
        }
        if (isUnlock) {
            if (weaponFactory != null) {
                player.getWeaponManager().addWeapon(weaponFactory.createWeapon(weaponId, player));
            }
        } else {
            for (final BaseWeapon w : player.getWeaponManager().getWeapons()) {
                if (w.getId().equalsIgnoreCase(weaponId)) {
                    w.upgrade(0f, 0f);
                    break;
                }
            }
        }
    }
}
