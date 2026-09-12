package hust.adventure.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for rendering the F3 detailed debug overlay, following the Zero Allocation pattern.
 */
public class DebugInfoUIData {
    private float x;
    private float y;
    private float speed;
    private float powerMultiplier;
    private float cooldownMultiplier;
    private float areaMultiplier;
    private float magnetMultiplier;
    private String stateName;
    private String mapName;
    private int fps;
    private int activeEntitiesCount;
    private long usedMemoryMB;
    private long totalMemoryMB;
    private final List<String> weapons = new ArrayList<>();
    private final List<String> gears = new ArrayList<>();

    public DebugInfoUIData(float x, float y, float speed, float powerMultiplier, float cooldownMultiplier,
                           float areaMultiplier, float magnetMultiplier, String stateName, String mapName,
                           int fps, int activeEntitiesCount, long usedMemoryMB, long totalMemoryMB,
                           List<String> weapons, List<String> gears) {
        set(x, y, speed, powerMultiplier, cooldownMultiplier, areaMultiplier, magnetMultiplier,
            stateName, mapName, fps, activeEntitiesCount, usedMemoryMB, totalMemoryMB, weapons, gears);
    }

    public void set(float x, float y, float speed, float powerMultiplier, float cooldownMultiplier,
                    float areaMultiplier, float magnetMultiplier, String stateName, String mapName,
                    int fps, int activeEntitiesCount, long usedMemoryMB, long totalMemoryMB,
                    List<String> weapons, List<String> gears) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.powerMultiplier = powerMultiplier;
        this.cooldownMultiplier = cooldownMultiplier;
        this.areaMultiplier = areaMultiplier;
        this.magnetMultiplier = magnetMultiplier;
        this.stateName = stateName != null ? stateName : "Unknown";
        this.mapName = mapName != null ? mapName : "Unknown";
        this.fps = fps;
        this.activeEntitiesCount = activeEntitiesCount;
        this.usedMemoryMB = usedMemoryMB;
        this.totalMemoryMB = totalMemoryMB;

        this.weapons.clear();
        if (weapons != null) {
            this.weapons.addAll(weapons);
        }

        this.gears.clear();
        if (gears != null) {
            this.gears.addAll(gears);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    public float getCooldownMultiplier() {
        return cooldownMultiplier;
    }

    public float getAreaMultiplier() {
        return areaMultiplier;
    }

    public float getMagnetMultiplier() {
        return magnetMultiplier;
    }

    public String getStateName() {
        return stateName;
    }

    public String getMapName() {
        return mapName;
    }

    public int getFps() {
        return fps;
    }

    public int getActiveEntitiesCount() {
        return activeEntitiesCount;
    }

    public long getUsedMemoryMB() {
        return usedMemoryMB;
    }

    public long getTotalMemoryMB() {
        return totalMemoryMB;
    }

    public List<String> getWeapons() {
        return weapons;
    }

    public List<String> getGears() {
        return gears;
    }
}
