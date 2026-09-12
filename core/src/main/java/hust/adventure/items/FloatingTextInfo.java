package hust.adventure.items;

import com.badlogic.gdx.graphics.Color;

/**
 * Data class representing a floating text notification triggered by item usage or effects.
 */
public class FloatingTextInfo {
    private final String text;
    private final Color color;
    private final float duration;
    private final float offsetX;
    private final float startVy;

    public FloatingTextInfo(final String text, final Color color, final float duration, final float offsetX,
            final float startVy) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null");
        }
        this.text = text;
        this.color = color;
        this.duration = duration;
        this.offsetX = offsetX;
        this.startVy = startVy;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    public float getDuration() {
        return duration;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getStartVy() {
        return startVy;
    }
}
