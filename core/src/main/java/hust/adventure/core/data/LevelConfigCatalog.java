package hust.adventure.core.data;

import com.badlogic.gdx.utils.Array;

/**
 * Data mapping helper representing the list of level configurations. Used by libGDX Json deserialization to parse the
 * configs/levels.json file.
 */
public class LevelConfigCatalog {
    private Array<LevelConfig> levels;

    /**
     * Gets the loaded array of level configurations.
     *
     * @return array of LevelConfig
     */
    public Array<LevelConfig> getLevels() {
        return levels;
    }

    /**
     * Sets the array of level configurations.
     *
     * @param levels array of LevelConfig
     */
    public void setLevels(final Array<LevelConfig> levels) {
        this.levels = levels;
    }
}
