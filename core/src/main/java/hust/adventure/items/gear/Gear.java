package hust.adventure.items.gear;

import hust.adventure.items.base.Item;
import hust.adventure.items.Equipable;
import hust.adventure.entities.player.Player;
import com.badlogic.gdx.Gdx;

/**
 * Represents a passive Gear item in the player's inventory that provides stat boosts. Supports levels 1 through 5.
 */
public class Gear extends Item implements Equipable {
    private int level;
    private final GearFactory gearFactory;

    public Gear(final String id, final String name, final String description, final GearFactory gearFactory) {
        super(id, name, description, null);
        this.level = 1;
        this.gearFactory = gearFactory;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Gear level must be between 1 and 5");
        }
        this.level = level;
    }

    public void upgrade() {
        if (level < 5) {
            level++;
        }
    }

    @Override
    public void equip(final Player player) {
        if (player != null) {
            if (player.getGearManager() != null) {
                player.getGearManager().addGear(this);
            }
            if (gearFactory != null) {
                gearFactory.applyEquipEffect(getId(), player);
            }
        }
    }

    @Override
    public void unequip(final Player player) {
        if (player != null) {
            if (player.getGearManager() != null) {
                player.getGearManager().getGears().removeValue(this, true);
            }
            if (gearFactory != null) {
                final GearConfig config = gearFactory.getDataManager().getConfig(getId());
                if (config != null && config.getOnEquipEffect() != null) {
                    final String effectType = config.getOnEquipEffect().toLowerCase();
                    final float val = config.getOnEquipValue();
                    switch (effectType) {
                        case "increase_max_hp":
                            player.increaseMaxHp(-val);
                            break;
                        case "add_power":
                            player.addPowerMultiplier(-val);
                            break;
                        case "reduce_cooldown":
                            // reduce_cooldown applies -val in equip, so in unequip we apply val
                            player.addCooldownMultiplier(val);
                            break;
                        case "add_speed":
                            player.addSpeedMultiplier(-val);
                            break;
                        case "add_area":
                            player.addAreaMultiplier(-val);
                            break;
                        case "add_magnet":
                            player.addMagnetMultiplier(-val);
                            break;
                        default:
                            Gdx.app.error("Gear", "Unknown gear unequip effect type: " + effectType);
                            break;
                    }
                }
            }
        }
    }
}
