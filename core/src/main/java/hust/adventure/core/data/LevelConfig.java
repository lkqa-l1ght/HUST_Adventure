package hust.adventure.core.data;

import com.badlogic.gdx.graphics.Color;

/**
 * Data object holding configuration for a specific game level. Can be loaded from JSON configs/levels.json or
 * constructed dynamically.
 */
public class LevelConfig {
    private String levelId;
    private String name;
    private String mapPath;
    private float spawnX;
    private float spawnY;
    private float zoom = 1.0f;
    private String bgmPath;
    private Color ambientColor = Color.BLACK;
    private boolean isInfinite;

    /**
     * Default constructor for libGDX Json deserialization.
     */
    public LevelConfig() {
    }

    /**
     * Complete constructor for dynamic instantiation.
     */
    public LevelConfig(String levelId, String name, String mapPath, float spawnX, float spawnY, float zoom,
            String bgmPath, Color ambientColor, boolean isInfinite) {
        this.levelId = levelId;
        this.name = name != null ? name : (levelId != null ? levelId : "");
        this.mapPath = mapPath;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.zoom = zoom;
        this.bgmPath = bgmPath;
        this.ambientColor = ambientColor != null ? ambientColor : Color.BLACK;
        this.isInfinite = isInfinite;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(final String levelId) {
        this.levelId = levelId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void setMapPath(final String mapPath) {
        this.mapPath = mapPath;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public void setSpawnX(final float spawnX) {
        this.spawnX = spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public void setSpawnY(final float spawnY) {
        this.spawnY = spawnY;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(final float zoom) {
        this.zoom = zoom;
    }

    public String getBgmPath() {
        return bgmPath;
    }

    public void setBgmPath(final String bgmPath) {
        this.bgmPath = bgmPath;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(final Color ambientColor) {
        this.ambientColor = ambientColor != null ? ambientColor : Color.BLACK;
    }

    public boolean isInfinite() {
        return isInfinite;
    }

    public void setInfinite(final boolean infinite) {
        this.isInfinite = infinite;
    }
}
