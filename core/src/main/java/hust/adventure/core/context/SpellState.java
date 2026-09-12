package hust.adventure.core.context;

/**
 * Encapsulates active spell status effect states and timers. Part of the SRP refactoring of ProgressContext.
 */
public class SpellState {
    private float enemyTimeScale = 1.0f;
    private boolean lightsOut = false;
    private float showEnemiesTimer = 0f;
    private boolean enemyBlinkVisible = true; // Used for blink effect in Lab Phase 3

    /**
     * Gets the current enemy time scale multiplier.
     *
     * @return multiplier value, e.g., 0f (stun), 0.3f (slow), or 1.0f (normal).
     */
    public float getEnemyTimeScale() {
        return enemyTimeScale;
    }

    /**
     * Sets the enemy time scale multiplier.
     *
     * @param enemyTimeScale time scaling factor.
     */
    public void setEnemyTimeScale(float enemyTimeScale) {
        this.enemyTimeScale = enemyTimeScale;
    }

    /**
     * Checks if lights out (darkness effect) is active.
     *
     * @return true if lights are out.
     */
    public boolean isLightsOut() {
        return lightsOut;
    }

    /**
     * Sets whether lights out is active.
     *
     * @param lightsOut true to turn off lights.
     */
    public void setLightsOut(boolean lightsOut) {
        this.lightsOut = lightsOut;
    }

    /**
     * Gets the remaining duration for the radar/show enemies spell.
     *
     * @return remaining duration in seconds.
     */
    public float getShowEnemiesTimer() {
        return showEnemiesTimer;
    }

    /**
     * Sets the remaining duration for the radar/show enemies spell.
     *
     * @param showEnemiesTimer duration in seconds.
     */
    public void setShowEnemiesTimer(float showEnemiesTimer) {
        this.showEnemiesTimer = showEnemiesTimer;
    }

    /**
     * Checks if enemies are currently visible during blink phase.
     *
     * @return true if visible.
     */
    public boolean isEnemyBlinkVisible() {
        return enemyBlinkVisible;
    }

    /**
     * Sets whether enemies are currently visible during blink phase.
     *
     * @param enemyBlinkVisible true to make them visible.
     */
    public void setEnemyBlinkVisible(boolean enemyBlinkVisible) {
        this.enemyBlinkVisible = enemyBlinkVisible;
    }

    /**
     * Resets all spell timers and states to their default values.
     */
    public void reset() {
        enemyTimeScale = 1.0f;
        lightsOut = false;
        showEnemiesTimer = 0f;
        enemyBlinkVisible = true;
    }
}
