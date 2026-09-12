package hust.adventure.items.gear;

import com.badlogic.gdx.utils.Array;

/**
 * Data configuration class representing the properties of a gear definition.
 */
public class GearConfig {
    private String id;
    private String name;
    private String description;
    private Array<String> levelDescriptions;
    private int maxLevel = 5;
    private String onEquipEffect;
    private float onEquipValue;

    /**
     * Gets the unique identifier of the gear.
     *
     * @return the gear ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the gear.
     *
     * @param id the gear ID
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the display name of the gear.
     *
     * @return the gear name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the gear.
     *
     * @param name the gear name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the general description of the gear.
     *
     * @return the gear description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the general description of the gear.
     *
     * @param description the gear description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Gets the list of level-specific descriptions.
     *
     * @return the array of level descriptions
     */
    public Array<String> getLevelDescriptions() {
        return levelDescriptions;
    }

    /**
     * Sets the list of level-specific descriptions.
     *
     * @param levelDescriptions the array of level descriptions
     */
    public void setLevelDescriptions(final Array<String> levelDescriptions) {
        this.levelDescriptions = levelDescriptions;
    }

    /**
     * Gets the maximum level of this gear.
     *
     * @return the maximum level
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Sets the maximum level of this gear.
     *
     * @param maxLevel the maximum level
     */
    public void setMaxLevel(final int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * Gets the identifier of the immediate equip effect.
     *
     * @return the effect name, or null if none
     */
    public String getOnEquipEffect() {
        return onEquipEffect;
    }

    /**
     * Sets the identifier of the immediate equip effect.
     *
     * @param onEquipEffect the effect name
     */
    public void setOnEquipEffect(final String onEquipEffect) {
        this.onEquipEffect = onEquipEffect;
    }

    /**
     * Gets the numerical value associated with the equip effect.
     *
     * @return the effect value
     */
    public float getOnEquipValue() {
        return onEquipValue;
    }

    /**
     * Sets the numerical value associated with the equip effect.
     *
     * @param onEquipValue the effect value
     */
    public void setOnEquipValue(final float onEquipValue) {
        this.onEquipValue = onEquipValue;
    }
}
