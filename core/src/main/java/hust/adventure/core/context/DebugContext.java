package hust.adventure.core.context;

/**
 * Encapsulates the debug configuration and options for the game. Part of the SRP refactoring of ProgressContext.
 */
public class DebugContext {
    private boolean showDebug = false;
    private boolean showHitbox = false;
    private boolean godMode = false;
    private boolean fastRun = false;

    /**
     * Checks if debug information display is enabled.
     *
     * @return true if debug display is enabled.
     */
    public boolean isShowDebug() {
        return showDebug;
    }

    /**
     * Sets whether debug information display should be enabled.
     *
     * @param showDebug true to show debug info.
     */
    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    /**
     * Checks if hitboxes rendering is enabled.
     *
     * @return true if hitboxes are visible.
     */
    public boolean isShowHitbox() {
        return showHitbox;
    }

    /**
     * Sets whether hitboxes rendering should be enabled.
     *
     * @param showHitbox true to show hitboxes.
     */
    public void setShowHitbox(boolean showHitbox) {
        this.showHitbox = showHitbox;
    }

    /**
     * Checks if god mode is active (invulnerability).
     *
     * @return true if player is invulnerable.
     */
    public boolean isGodMode() {
        return godMode;
    }

    /**
     * Sets whether god mode should be active.
     *
     * @param godMode true to activate invulnerability.
     */
    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    /**
     * Checks if fast run mode is enabled.
     *
     * @return true if speed multiplier is applied.
     */
    public boolean isFastRun() {
        return fastRun;
    }

    /**
     * Sets whether fast run mode should be enabled.
     *
     * @param fastRun true to enable fast run.
     */
    public void setFastRun(boolean fastRun) {
        this.fastRun = fastRun;
    }

    /**
     * Resets all debug flags to their default state (false).
     */
    public void reset() {
        showDebug = false;
        showHitbox = false;
        godMode = false;
        fastRun = false;
    }
}
