package hust.adventure.items.consumable;

/**
 * Configuration data object representing a single effect for a consumable item.
 */
public class EffectConfig {
    private String effect;
    private float value;
    private float duration;
    private float multiplier;

    /**
     * Gets the type of the effect.
     *
     * @return the effect type identifier
     */
    public String getEffect() {
        return effect;
    }

    /**
     * Sets the type of the effect.
     *
     * @param effect the effect type identifier
     */
    public void setEffect(final String effect) {
        this.effect = effect;
    }

    /**
     * Gets the numerical value of the effect.
     *
     * @return the value of the effect
     */
    public float getValue() {
        return value;
    }

    /**
     * Sets the numerical value of the effect.
     *
     * @param value the value of the effect
     */
    public void setValue(final float value) {
        this.value = value;
    }

    /**
     * Gets the duration of the effect in seconds.
     *
     * @return the duration of the effect
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the effect in seconds.
     *
     * @param duration the duration of the effect
     */
    public void setDuration(final float duration) {
        this.duration = duration;
    }

    /**
     * Gets the multiplier value of the effect.
     *
     * @return the multiplier of the effect
     */
    public float getMultiplier() {
        return multiplier;
    }

    /**
     * Sets the multiplier value of the effect.
     *
     * @param multiplier the multiplier of the effect
     */
    public void setMultiplier(final float multiplier) {
        this.multiplier = multiplier;
    }
}
