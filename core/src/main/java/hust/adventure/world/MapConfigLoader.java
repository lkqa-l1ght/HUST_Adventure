package hust.adventure.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loader class to read map layer configurations from JSON.
 */
public final class MapConfigLoader {
    private static final String DEFAULT_PATH = "configs/map_config.json";

    private MapConfigLoader() {
        // Prevent instantiation
    }

    /**
     * Loads MapConfig from the default path.
     *
     * @return the parsed MapConfig object
     */
    public static MapConfig load() {
        return load(DEFAULT_PATH);
    }

    /**
     * Loads MapConfig from a specified JSON file path.
     *
     * @param path relative path to the configuration file
     * @return the parsed MapConfig object
     */
    public static MapConfig load(final String path) {
        try {
            final Json json = new Json();
            final MapConfig config = json.fromJson(MapConfig.class, Gdx.files.internal(path));
            if (config == null) {
                throw new GdxRuntimeException("Loaded config was null from path: " + path);
            }
            return config;
        } catch (final SerializationException | GdxRuntimeException e) {
            Gdx.app.error("MapConfigLoader", "Failed to load map configurations from: " + path, e);
            throw new GdxRuntimeException("Failed to load map configurations from: " + path, e);
        }
    }
}
