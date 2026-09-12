package hust.adventure.core.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.GdxRuntimeException;
import hust.adventure.wave.WaveEntry;
import hust.adventure.wave.WaveConfigCatalog;

/**
 * Loads and manages wave configurations grouped by levelId.
 * Decouples wave data loading from LevelBehavior and WaveManager.
 */
public class WaveDataLoader {
    private final WaveConfigCatalog catalog = new WaveConfigCatalog();

    /**
     * Constructs the WaveDataLoader and triggers config loading.
     *
     * @param configsJsonPath the relative path to the waves configuration file
     */
    public WaveDataLoader(final String configsJsonPath) {
        loadConfigs(configsJsonPath);
    }

    private void loadConfigs(final String path) {
        try {
            final Json json = new Json();
            final JsonReader reader = new JsonReader();
            final JsonValue root = reader.parse(Gdx.files.internal(path));

            for (JsonValue levelEntry = root.child; levelEntry != null; levelEntry = levelEntry.next) {
                final String levelId = levelEntry.name();
                final Array<WaveEntry> waves = new Array<>();
                for (JsonValue waveVal = levelEntry.child; waveVal != null; waveVal = waveVal.next) {
                    final WaveEntry entry = json.readValue(WaveEntry.class, waveVal);
                    if (entry != null) {
                        waves.add(entry);
                    }
                }
                catalog.addWaves(levelId, waves);
            }
        } catch (final SerializationException | GdxRuntimeException e) {
            Gdx.app.error("WaveDataLoader", "Failed to load wave configurations from: " + path, e);
            throw new GdxRuntimeException("Failed to load wave configurations from: " + path, e);
        }
    }

    /**
     * Retrieves the wave entries associated with a specific level ID.
     * If no waves are configured for the level, returns an empty array.
     *
     * @param levelId the unique level identifier
     * @return the array of WaveEntry configurations, never null
     */
    public Array<WaveEntry> getWaves(final String levelId) {
        if (levelId == null) {
            return new Array<>();
        }
        final Array<WaveEntry> waves = catalog.getWaves(levelId);
        return waves != null ? waves : new Array<>();
    }
}
