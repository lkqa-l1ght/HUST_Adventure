package hust.adventure.world;

/**
 * Data class representing a static lighting object parsed from the map.
 */
public class LightingObjectData {
    private final String name;
    private final float x;
    private final float y;

    /**
     * Constructs a LightingObjectData.
     *
     * @param name name of the lighting object
     * @param x    x coordinate
     * @param y    y coordinate
     */
    public LightingObjectData(final String name, final float x, final float y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
