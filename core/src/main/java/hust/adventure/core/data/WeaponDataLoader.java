package hust.adventure.core.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

import hust.adventure.items.weapons.WeaponConfig;
import hust.adventure.items.weapons.WeaponConfigCatalog;

/**
 * Responsible for loading the JSON and storing configuration models for weapons.
 */
public class WeaponDataLoader {
    private final ObjectMap<String, WeaponConfig> configs;

    public WeaponDataLoader(final String configFilePath) {
        configs = new ObjectMap<>();
        loadConfigs(configFilePath);
    }

    private void loadConfigs(final String filePath) {
        final Json json = new Json();
        final WeaponConfigCatalog catalog = json.fromJson(WeaponConfigCatalog.class, Gdx.files.internal(filePath));
        if (catalog != null && catalog.getWeapons() != null) {
            for (final WeaponConfig config : catalog.getWeapons()) {
                configs.put(config.getId(), config);
            }
        }
    }

    public WeaponConfig getConfig(final String id) {
        return configs.get(id);
    }

    public Array<String> getAllWeaponIds() {
        final Array<String> ids = new Array<>();
        for (final ObjectMap.Entry<String, WeaponConfig> entry : configs.entries()) {
            ids.add(entry.key);
        }
        return ids;
    }

    public String getWeaponName(final String id) {
        final WeaponConfig config = getConfig(id);
        return config != null ? config.getName() : id;
    }

    public String getWeaponLevelDescription(final String id, final int level) {
        final WeaponConfig config = getConfig(id);
        if (config == null || config.getLevelDescriptions() == null) {
            return "";
        }
        final int index = level - 1;
        if (index >= 0 && index < config.getLevelDescriptions().size) {
            return config.getLevelDescriptions().get(index);
        }
        return "";
    }
}
