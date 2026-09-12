package hust.adventure.items.consumable;

/**
 * Configuration data object representing floating text triggered by item consumption.
 */
public class FloatingTextConfig {
    private String text;
    private String colorHex;
    private float duration;
    private float offsetX;
    private float startVy;

    /**
     * Gets the display text.
     *
     * @return the display text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the display text.
     *
     * @param text the display text
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Gets the color in hexadecimal format.
     *
     * @return the color hex string
     */
    public String getColorHex() {
        return colorHex;
    }

    /**
     * Sets the color in hexadecimal format.
     *
     * @param colorHex the color hex string
     */
    public void setColorHex(final String colorHex) {
        this.colorHex = colorHex;
    }

    /**
     * Gets the duration of the text display in seconds.
     *
     * @return the display duration
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the text display in seconds.
     *
     * @param duration the display duration
     */
    public void setDuration(final float duration) {
        this.duration = duration;
    }

    /**
     * Gets the horizontal offset from the actor center.
     *
     * @return the horizontal offset
     */
    public float getOffsetX() {
        return offsetX;
    }

    /**
     * Sets the horizontal offset from the actor center.
     *
     * @param offsetX the horizontal offset
     */
    public void setOffsetX(final float offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * Gets the initial vertical velocity of the text.
     *
     * @return the initial vertical velocity
     */
    public float getStartVy() {
        return startVy;
    }

    /**
     * Sets the initial vertical velocity of the text.
     *
     * @param startVy the initial vertical velocity
     */
    public void setStartVy(final float startVy) {
        this.startVy = startVy;
    }
}
