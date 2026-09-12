package hust.adventure.wave;

/**
 * Data structure for a single wave entry configuration.
 */
public class WaveEntry {
    private float timeStart;
    private float timeEnd;
    private String enemyType = "";
    private float spawnInterval;
    private int spawnCount;
    private String pattern = "";

    /**
     * Gets the starting time of the wave in seconds.
     *
     * @return the starting time
     */
    public float getTimeStart() {
        return timeStart;
    }

    /**
     * Gets the ending time of the wave in seconds.
     *
     * @return the ending time
     */
    public float getTimeEnd() {
        return timeEnd;
    }

    /**
     * Gets the identifier of the enemy type to spawn.
     *
     * @return the enemy type identifier
     */
    public String getEnemyType() {
        return enemyType != null ? enemyType : "";
    }

    /**
     * Gets the spawn interval in seconds.
     *
     * @return the spawn interval
     */
    public float getSpawnInterval() {
        return spawnInterval;
    }

    /**
     * Gets the number of enemies to spawn per interval.
     *
     * @return the spawn count
     */
    public int getSpawnCount() {
        return spawnCount;
    }

    /**
     * Gets the spawn pattern identifier (e.g. RANDOM_EDGE).
     *
     * @return the spawn pattern
     */
    public String getPattern() {
        return pattern != null ? pattern : "";
    }
}
