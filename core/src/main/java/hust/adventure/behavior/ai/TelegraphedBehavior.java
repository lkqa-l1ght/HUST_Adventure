package hust.adventure.behavior.ai;

/**
 * Interface representing movement behaviors that telegraph their action.
 * Exposes telegraph state for rendering overlays without coupling to the Enemy model.
 */
public interface TelegraphedBehavior extends AIBehavior {
    /**
     * Checks if the behavior is currently in the telegraph phase.
     *
     * @return true if telegraphing, false otherwise
     */
    boolean isTelegraphed();

    /**
     * Gets the remaining time or elapsed time of the telegraph phase in seconds.
     *
     * @return telegraph duration value
     */
    float getTelegraphTimer();

    /**
     * Gets the X coordinate of the warning line endpoint.
     *
     * @return target X coordinate
     */
    float getTelegraphTargetX();

    /**
     * Gets the Y coordinate of the warning line endpoint.
     *
     * @return target Y coordinate
     */
    float getTelegraphTargetY();
}
