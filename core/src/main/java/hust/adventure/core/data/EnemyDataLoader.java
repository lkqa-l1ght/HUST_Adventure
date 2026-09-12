package hust.adventure.core.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.GdxRuntimeException;

import hust.adventure.entities.enemies.EnemyConfig;
import hust.adventure.entities.enemies.EnemyConfigCatalog;

/**
 * Manages the loading and retrieval of game data configurations (e.g. enemy definitions). Decouples JSON parsing and
 * data management from entity and factory classes (SRP).
 */
public class EnemyDataLoader {
    private final ObjectMap<String, EnemyConfig> enemyConfigs = new ObjectMap<>();

    /**
     * Initializes the manager and loads the configurations from assets.
     *
     * @param configsJsonPath the relative path to the configuration file
     */
    public EnemyDataLoader(final String configsJsonPath) {
        loadConfigs(configsJsonPath);
    }

    private void loadConfigs(final String path) {
        try {
            final Json json = new Json();
            final EnemyConfigCatalog catalog = json.fromJson(EnemyConfigCatalog.class, Gdx.files.internal(path));
            if (catalog != null && catalog.getEnemies() != null) {
                for (final EnemyConfig config : catalog.getEnemies()) {
                    enemyConfigs.put(config.getType().toLowerCase(), config);
                }
            }
        } catch (final SerializationException | GdxRuntimeException e) {
            Gdx.app.error("EnemyDataManager", "Failed to load enemy configurations from: " + path, e);
            throw new GdxRuntimeException("Failed to load enemy configurations from: " + path, e);
        }
    }

    /**
     * Retrieves the configuration for a specific enemy type.
     *
     * @param type the identifier of the enemy type
     * @return the corresponding EnemyConfig, or null if not found
     */
    public EnemyConfig getEnemyConfig(final String type) {
        if (type == null) {
            return null;
        }
        return enemyConfigs.get(type.toLowerCase());
    }

    /**
     * Retrieves all loaded enemy types.
     *
     * @return an array of all enemy types
     */
    public Array<String> getAllEnemyTypes() {
        final Array<String> types = new Array<>();
        for (final ObjectMap.Entry<String, EnemyConfig> entry : enemyConfigs.entries()) {
            types.add(entry.key);
        }
        return types;
    }

    /**
     * Retrieves all loaded enemy configurations.
     *
     * @return an iterable collection of all EnemyConfig
     */
    public Iterable<EnemyConfig> getAllConfigs() {
        return enemyConfigs.values();
    }
}
