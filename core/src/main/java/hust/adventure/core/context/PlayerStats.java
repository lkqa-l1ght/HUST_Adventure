package hust.adventure.core.context;

import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;

/**
 * Encapsulates the core progression and combat statistics of the Player. Part of the SRP refactoring of
 * ProgressContext.
 */
public class PlayerStats {
    private float hp = 300f;
    private float maxHp = 300f;
    private float stamina = 100f;
    private float maxStamina = 100f;
    private float morale = 100f;
    private int level = 1;
    private float exp = 0;
    private float expToNextLevel = 10f;
    private float damageMultiplier = 1.0f;

    /**
     * Gets the current hit points (HP) of the player.
     *
     * @return current HP.
     */
    public float getHp() {
        return hp;
    }

    /**
     * Sets the current hit points (HP) of the player.
     *
     * @param hp new HP.
     */
    public void setHp(float hp) {
        float oldHp = this.hp;
        this.hp = Math.max(0, Math.min(this.maxHp, hp));
        checkDeath(oldHp);
    }

    /**
     * Gets the maximum hit points (HP) of the player.
     *
     * @return max HP.
     */
    public float getMaxHp() {
        return maxHp;
    }

    /**
     * Sets the maximum hit points (HP) of the player.
     *
     * @param maxHp new max HP.
     */
    public void setMaxHp(float maxHp) {
        this.maxHp = maxHp;
        float oldHp = this.hp;
        this.hp = Math.min(this.hp, this.maxHp);
        checkDeath(oldHp);
    }

    private void checkDeath(final float oldHp) {
        if (oldHp > 0 && this.hp <= 0) {
            EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.PLAYER_DIED, null));
            EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ACADEMIC_SUSPENSION, null));
        }
    }

    /**
     * Gets the current stamina of the player.
     *
     * @return current stamina.
     */
    public float getStamina() {
        return stamina;
    }

    /**
     * Sets the current stamina of the player.
     *
     * @param stamina new stamina.
     */
    public void setStamina(float stamina) {
        this.stamina = Math.max(0, Math.min(this.maxStamina, stamina));
    }

    /**
     * Gets the maximum stamina of the player.
     *
     * @return max stamina.
     */
    public float getMaxStamina() {
        return maxStamina;
    }

    /**
     * Sets the maximum stamina of the player.
     *
     * @param maxStamina new max stamina.
     */
    public void setMaxStamina(float maxStamina) {
        this.maxStamina = maxStamina;
        this.stamina = Math.min(this.stamina, this.maxStamina);
    }

    /**
     * Gets the current morale level of the player.
     *
     * @return current morale.
     */
    public float getMorale() {
        return morale;
    }

    /**
     * Sets the current morale level of the player.
     *
     * @param morale new morale.
     */
    public void setMorale(float morale) {
        this.morale = morale;
    }

    /**
     * Gets the current level of the player.
     *
     * @return current level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the current level of the player.
     *
     * @param level new level.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Gets the current experience points (EXP) of the player.
     *
     * @return current EXP.
     */
    public float getExp() {
        return exp;
    }

    /**
     * Sets the current experience points (EXP) of the player.
     *
     * @param exp new EXP.
     */
    public void setExp(float exp) {
        this.exp = exp;
    }

    /**
     * Gets the experience points needed to level up.
     *
     * @return EXP target.
     */
    public float getExpToNextLevel() {
        return expToNextLevel;
    }

    /**
     * Sets the experience points needed to level up.
     *
     * @param expToNextLevel new EXP target.
     */
    public void setExpToNextLevel(float expToNextLevel) {
        this.expToNextLevel = expToNextLevel;
    }

    /**
     * Gets the global damage multiplier for all player weapons.
     *
     * @return damage multiplier factor.
     */
    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Sets the global damage multiplier for all player weapons.
     *
     * @param damageMultiplier new multiplier.
     */
    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    /**
     * Resets player statistics to default new-game values.
     */
    public void reset() {
        hp = 300f;
        maxHp = 300f;
        stamina = 100f;
        maxStamina = 100f;
        morale = 100f;
        level = 1;
        exp = 0;
        expToNextLevel = 10f;
        damageMultiplier = 1.0f;
    }
}
