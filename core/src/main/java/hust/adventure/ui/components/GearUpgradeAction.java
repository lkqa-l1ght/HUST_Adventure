package hust.adventure.ui.components;

import hust.adventure.entities.player.Player;
import hust.adventure.items.gear.Gear;
import hust.adventure.items.gear.GearFactory;

/**
 * Action representing a choice to unlock a new gear or upgrade an existing one.
 */
public class GearUpgradeAction implements UpgradeAction {
    private final GearFactory gearFactory;

    private final String gearId;
    private final String name;
    private final String description;
    private final boolean isUnlock;

    public GearUpgradeAction(final String gearId, final String name, final String description, final boolean isUnlock, final GearFactory gearFactory) {
        if (gearId == null) {
            throw new IllegalArgumentException("Gear ID cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        this.gearId = gearId;
        this.name = name;
        this.description = description;
        this.isUnlock = isUnlock;
        this.gearFactory = gearFactory;
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
            if (gearFactory != null) {
                final Gear gear = gearFactory.createGear(gearId);
                gear.equip(player);
            }
        } else {
            final Gear gear = player.getGearManager().getGear(gearId);
            if (gear != null) {
                gear.upgrade();
                gear.equip(player);
            }
        }
    }
}
