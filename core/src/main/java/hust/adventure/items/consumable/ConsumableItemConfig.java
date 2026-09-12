package hust.adventure.items.consumable;

import com.badlogic.gdx.utils.Array;

import hust.adventure.items.base.ItemConfig;

/**
 * Configuration class representing a consumable item, extending base item configurations.
 */
public class ConsumableItemConfig extends ItemConfig {
    private Array<EffectConfig> effects;
    private Array<FloatingTextConfig> floatingTexts;

    /**
     * Gets the list of effects triggered on consumption.
     *
     * @return the array of effect configurations
     */
    public Array<EffectConfig> getEffects() {
        return effects;
    }

    /**
     * Sets the list of effects triggered on consumption.
     *
     * @param effects the array of effect configurations
     */
    public void setEffects(final Array<EffectConfig> effects) {
        this.effects = effects;
    }

    /**
     * Gets the list of floating texts triggered on consumption.
     *
     * @return the array of floating text configurations
     */
    public Array<FloatingTextConfig> getFloatingTexts() {
        return floatingTexts;
    }

    /**
     * Sets the list of floating texts triggered on consumption.
     *
     * @param floatingTexts the array of floating text configurations
     */
    public void setFloatingTexts(final Array<FloatingTextConfig> floatingTexts) {
        this.floatingTexts = floatingTexts;
    }
}
