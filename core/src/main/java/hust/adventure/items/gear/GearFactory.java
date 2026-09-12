package hust.adventure.items.gear;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;

import hust.adventure.core.data.GearDataLoader;
import hust.adventure.entities.player.Player;

/**
 * Factory class for creating Gear instances and applying their immediate equip/upgrade side-effects. Uses a
 * registry-based provider pattern for mapping equip effects.
 */
public class GearFactory {
    private final GearDataLoader dataManager;
    private final ObjectMap<String, GearEffectApplier> effectAppliers = new ObjectMap<>();

    /**
     * Initializes the factory with the given data manager and registers default effect appliers.
     *
     * @param dataManager the GearDataManager to retrieve gear configurations from
     */
    public GearFactory(final GearDataLoader dataManager) {
        if (dataManager == null) {
            throw new IllegalArgumentException("GearDataManager cannot be null");
        }
        this.dataManager = dataManager;
        registerDefaultAppliers();
    }

    public GearDataLoader getDataManager() {
        return dataManager;
    }

    private void registerDefaultAppliers() {
        effectAppliers.put("increase_max_hp", (config, player) -> player.increaseMaxHp(config.getOnEquipValue()));
        effectAppliers.put("add_power", (config, player) -> player.addPowerMultiplier(config.getOnEquipValue()));
        effectAppliers.put("reduce_cooldown", (config, player) -> player.addCooldownMultiplier(-config.getOnEquipValue()));
        effectAppliers.put("add_speed", (config, player) -> player.addSpeedMultiplier(config.getOnEquipValue()));
        effectAppliers.put("add_area", (config, player) -> player.addAreaMultiplier(config.getOnEquipValue()));
        effectAppliers.put("add_magnet", (config, player) -> player.addMagnetMultiplier(config.getOnEquipValue()));
    }

    /**
     * Creates a new runtime Gear instance using configuration metadata.
     *
     * @param gearId the unique identifier of the gear
     * @return the created Gear instance
     */
    public Gear createGear(final String gearId) {
        if (gearId == null) {
            throw new IllegalArgumentException("Gear ID cannot be null");
        }
        final GearConfig config = dataManager.getConfig(gearId);
        if (config == null) {
            Gdx.app.error("GearFactory", "Failed to create gear: configuration not found for ID " + gearId);
            return new Gear(gearId, gearId, "", this);
        }
        return new Gear(config.getId(), config.getName(), config.getDescription(), this);
    }

    /**
     * Applies the immediate side-effect of a gear to the player when equipped or upgraded.
     *
     * @param gearId the unique identifier of the gear
     * @param player the player entity to apply the effect to
     */
    public void applyEquipEffect(final String gearId, final Player player) {
        if (gearId == null || player == null) {
            return;
        }
        final GearConfig config = dataManager.getConfig(gearId);
        if (config == null || config.getOnEquipEffect() == null) {
            return;
        }

        final String effectType = config.getOnEquipEffect().toLowerCase();
        final GearEffectApplier applier = effectAppliers.get(effectType);
        if (applier != null) {
            applier.apply(config, player);
        } else {
            Gdx.app.error("GearFactory", "Unknown gear equip effect type: " + effectType);
        }
    }

    /**
     * Functional interface representing a behavior to apply a gear's immediate effect on equip.
     */
    public interface GearEffectApplier {
        /**
         * Applies the effect logic.
         *
         * @param config the configuration model of the gear
         * @param player the player entity to affect
         */
        void apply(GearConfig config, Player player);
    }
}
