package hust.adventure.events;
 
/**
 * Data for MAP_TRANSITION event.
 */
public class MapTransitionData {
    private final String targetMap;
    private final float spawnX;
    private final float spawnY;
 
    public MapTransitionData(String targetMap, float spawnX, float spawnY) {
        if (targetMap == null) throw new NullPointerException("targetMap cannot be null");
        this.targetMap = targetMap;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }
 
    public String getTargetMap() {
        return targetMap;
    }
 
    public float getSpawnX() {
        return spawnX;
    }
 
    public float getSpawnY() {
        return spawnY;
    }
}
