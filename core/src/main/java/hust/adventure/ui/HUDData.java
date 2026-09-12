package hust.adventure.ui;

/**
 * Data Transfer Object containing all necessary data to render the main HUD status bars.
 */
public class HUDData {
    private float hp;
    private float maxHp;
    private float stamina;
    private float maxStamina;
    private float morale;
    private float exp;
    private float expToNextLevel;
    private int level;
    private float currentTime;

    public HUDData(float hp, float maxHp, float stamina, float maxStamina, float morale, float exp,
            float expToNextLevel, int level, float currentTime) {
        this.hp = hp;
        this.maxHp = maxHp;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.morale = morale;
        this.exp = exp;
        this.expToNextLevel = expToNextLevel;
        this.level = level;
        this.currentTime = currentTime;
    }

    public void set(float hp, float maxHp, float stamina, float maxStamina, float morale, float exp,
            float expToNextLevel, int level, float currentTime) {
        this.hp = hp;
        this.maxHp = maxHp;
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.morale = morale;
        this.exp = exp;
        this.expToNextLevel = expToNextLevel;
        this.level = level;
        this.currentTime = currentTime;
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public float getStamina() {
        return stamina;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public float getMorale() {
        return morale;
    }

    public float getExp() {
        return exp;
    }

    public float getExpToNextLevel() {
        return expToNextLevel;
    }

    public int getLevel() {
        return level;
    }

    public float getCurrentTime() {
        return currentTime;
    }
}
