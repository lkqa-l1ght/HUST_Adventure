package hust.adventure.items.weapons;

import com.badlogic.gdx.utils.Array;

/**
 * Data Object representing the metadata and configuration of a weapon.
 */
public class WeaponConfig {
    private String id;
    private String name;
    private String description;
    private float baseDamage;
    private float cooldown;
    private float area;
    private int maxLevel = 5;
    private Array<String> levelDescriptions;
    private Array<WeaponLevelConfig> levels;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(final float baseDamage) {
        this.baseDamage = baseDamage;
    }

    public float getCooldown() {
        return cooldown;
    }

    public void setCooldown(final float cooldown) {
        this.cooldown = cooldown;
    }

    public float getArea() {
        return area;
    }

    public void setArea(final float area) {
        this.area = area;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(final int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public Array<String> getLevelDescriptions() {
        return levelDescriptions;
    }

    public void setLevelDescriptions(final Array<String> levelDescriptions) {
        this.levelDescriptions = levelDescriptions;
    }

    public Array<WeaponLevelConfig> getLevels() {
        return levels;
    }

    public void setLevels(final Array<WeaponLevelConfig> levels) {
        this.levels = levels;
    }
}
