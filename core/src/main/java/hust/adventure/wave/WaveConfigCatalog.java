package hust.adventure.wave;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Catalog DTO mapping levelId to an Array of WaveEntry objects.
 */
public class WaveConfigCatalog {
    private final ObjectMap<String, Array<WaveEntry>> catalog = new ObjectMap<>();

    /**
     * Retrieves the internal catalog map.
     *
     * @return the catalog map
     */
    public ObjectMap<String, Array<WaveEntry>> getCatalog() {
        return catalog;
    }

    /**
     * Gets the waves for a specific level ID.
     *
     * @param levelId the unique level identifier
     * @return the array of wave entries, or null if none defined
     */
    public Array<WaveEntry> getWaves(final String levelId) {
        if (levelId == null) {
            return null;
        }
        return catalog.get(levelId);
    }

    /**
     * Associates waves with a specific level ID.
     *
     * @param levelId the unique level identifier
     * @param waves   the wave entries to assign
     */
    public void addWaves(final String levelId, final Array<WaveEntry> waves) {
        if (levelId != null && waves != null) {
            catalog.put(levelId, waves);
        }
    }
}
