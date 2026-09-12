package hust.adventure.entities.enemies;

import com.badlogic.gdx.utils.Array;

/**
 * Data mapping helper representing the list of enemy configurations.
 * Used by libGDX Json deserialization to parse the configs/enemies.json file.
 */
public class EnemyConfigCatalog {
    private Array<EnemyConfig> enemies;

    /**
     * Gets the loaded array of enemy configurations.
     *
     * @return array of EnemyConfig
     */
    public Array<EnemyConfig> getEnemies() {
        return enemies;
    }

    /**
     * Sets the array of enemy configurations.
     *
     * @param enemies array of EnemyConfig
     */
    public void setEnemies(final Array<EnemyConfig> enemies) {
        this.enemies = enemies;
    }
}
