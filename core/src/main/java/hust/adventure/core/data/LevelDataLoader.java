package hust.adventure.core.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Manages the loading and retrieval of game level configurations. Decouples level configurations from hardcoded Java
 * files to assets/configs/levels.json.
 */
public class LevelDataLoader {
    private final ObjectMap<String, LevelConfig> levelConfigs = new ObjectMap<>();
    private final Array<LevelConfig> allConfigs = new Array<>();

    /**
     * Initializes the manager and loads the configurations from assets.
     *
     * @param configsJsonPath the relative path to the configuration file
     */
    public LevelDataLoader(final String configsJsonPath) {
        loadConfigs(configsJsonPath);
    }

    private void loadConfigs(final String path) {
        try {
            final Json json = new Json();
            final LevelConfigCatalog catalog = json.fromJson(LevelConfigCatalog.class, Gdx.files.internal(path));
            if (catalog != null && catalog.getLevels() != null) {
                for (final LevelConfig config : catalog.getLevels()) {
                    levelConfigs.put(config.getLevelId().toUpperCase(), config);
                    allConfigs.add(config);
                }
            }
        } catch (final SerializationException | GdxRuntimeException e) {
            Gdx.app.error("LevelDataManager", "Failed to load level configurations from: " + path, e);
            throw new GdxRuntimeException("Failed to load level configurations from: " + path, e);
        }
    }

    /**
     * Retrieves the configuration for a specific level ID.
     *
     * @param levelId the Level ID string
     * @return the corresponding LevelConfig, or null if not found
     */
    public LevelConfig getLevelConfig(final String levelId) {
        if (levelId == null) {
            return null;
        }
        return levelConfigs.get(levelId.toUpperCase());
    }

    /**
     * Retrieves the configuration for a specific map path (case-insensitive).
     *
     * @param mapPath the path to the map (e.g. "tang1.tmx")
     * @return the corresponding LevelConfig, or null if not found
     */
    public LevelConfig getLevelConfigByMapPath(final String mapPath) {
        if (mapPath == null) {
            return null;
        }
        for (final LevelConfig config : allConfigs) {
            if (mapPath.equalsIgnoreCase(config.getMapPath())) {
                return config;
            }
        }
        return null;
    }

    /**
     * Retrieves all loaded level configurations.
     *
     * @return an array of all level configurations
     */
    public Array<LevelConfig> getAllConfigs() {
        return allConfigs;
    }
}
