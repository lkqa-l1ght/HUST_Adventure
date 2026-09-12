package hust.adventure.core.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.GdxRuntimeException;

import hust.adventure.items.gear.GearConfig;

/**
 * Data manager for loading and retrieving gear configurations from JSON.
 */
public class GearDataLoader {
    private final ObjectMap<String, GearConfig> gearConfigs = new ObjectMap<>();
    private final Array<String> gearIds = new Array<>();

    /**
     * Initializes the manager and loads the configurations from the specified path.
     *
     * @param configsJsonPath the relative path to the configuration file
     */
    public GearDataLoader(final String configsJsonPath) {
        loadConfigs(configsJsonPath);
    }

    private void loadConfigs(final String path) {
        try {
            final Json json = new Json();
            final JsonReader reader = new JsonReader();
            final JsonValue root = reader.parse(Gdx.files.internal(path));
            final JsonValue gearsArray = root.get("gears");

            if (gearsArray != null) {
                for (JsonValue entry = gearsArray.child; entry != null; entry = entry.next) {
                    final GearConfig config = json.readValue(GearConfig.class, entry);
                    if (config != null && config.getId() != null) {
                        final String idLower = config.getId().toLowerCase();
                        gearConfigs.put(idLower, config);
                        gearIds.add(config.getId());
                    }
                }
            }
        } catch (final SerializationException | GdxRuntimeException e) {
            Gdx.app.error("GearDataManager", "Failed to load gear configurations from: " + path, e);
            throw new GdxRuntimeException("Failed to load gear configurations from: " + path, e);
        }
    }

    /**
     * Retrieves the configuration for a specific gear ID.
     *
     * @param id the unique identifier of the gear
     * @return the corresponding GearConfig, or null if not found
     */
    public GearConfig getConfig(final String id) {
        if (id == null) {
            return null;
        }
        return gearConfigs.get(id.toLowerCase());
    }

    /**
     * Retrieves all loaded gear identifiers.
     *
     * @return an array of all gear IDs
     */
    public Array<String> getAllGearIds() {
        return gearIds;
    }

    /**
     * Retrieves the display name of a gear by its identifier.
     *
     * @param id the gear identifier
     * @return the display name, or the id itself if not found
     */
    public String getGearName(final String id) {
        if (id == null) {
            return "";
        }
        final GearConfig config = getConfig(id);
        return config != null ? config.getName() : id;
    }

    /**
     * Retrieves the level-specific description of a gear.
     *
     * @param id    the gear identifier
     * @param level the level (1-based index)
     * @return the level-specific description
     */
    public String getGearLevelDescription(final String id, final int level) {
        if (id == null) {
            return "";
        }
        final GearConfig config = getConfig(id);
        if (config == null) {
            return "";
        }

        final Array<String> descriptions = config.getLevelDescriptions();
        if (descriptions == null || descriptions.size == 0) {
            return config.getDescription();
        }

        final int index = level - 1;
        if (index < 0) {
            return descriptions.get(0);
        } else if (index >= descriptions.size) {
            return descriptions.get(descriptions.size - 1);
        } else {
            return descriptions.get(index);
        }
    }

    /**
     * Retrieves all loaded gear configurations.
     *
     * @return an iterable collection of all GearConfig
     */
    public Iterable<GearConfig> getAllConfigs() {
        return gearConfigs.values();
    }
}
