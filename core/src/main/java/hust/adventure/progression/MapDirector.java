package hust.adventure.progression;

/**
 * Deep domain module directing campus run progression, wave tracking, and key item gating.
 */
public interface MapDirector {
    /**
     * Gets the currently active campus stage.
     *
     * @return the active CampusMap enum.
     */
    CampusMap getCurrentStage();

    /**
     * Gets the required key item identifier for the current active stage.
     *
     * @return the key item identifier, or null if no key item is required.
     */
    String getCurrentStageKeyItemId();

    /**
     * Gets the elapsed simulation time in seconds within the current stage.
     *
     * @return elapsed time in seconds.
     */
    float getStageElapsedTime();

    /**
     * Advances the simulation time and updates wave/deadline progression.
     *
     * @param delta elapsed frame time in seconds.
     */
    void update(float delta);

    /**
     * Checks if all stage requirements (Deadlines, Key Items, Bosses) are met to transition.
     *
     * @return true if transitioning is permitted, false otherwise.
     */
    boolean canTransition();

    /**
     * Checks if the exit portal is energized.
     *
     * @return true if the portal is active and accessible, false otherwise.
     */
    boolean isPortalActive();

    /**
     * Indicates whether combat auto-attacks are permitted (suppressed in Floor 1 and Library puzzle phases).
     *
     * @return true if auto-attacks should fire, false if silenced.
     */
    boolean isAutoAttackAllowed();

    /**
     * Registers that all Deadlines for the current stage have been survived and cleared.
     */
    void onDeadlinesCleared();

    /**
     * Registers the acquisition of a Key Item by ID.
     *
     * @param keyItemId the item identifier matching items.json.
     */
    void onKeyItemCollected(String keyItemId);

    /**
     * Registers that all Academic Challenges in the current stage are completed.
     */
    void onAcademicChallengesCleared();

    /**
     * Registers that the stage boss/guardian has been defeated.
     */
    void onBossDefeated();

    /**
     * Advances the run to the next sequential stage according to ADR-0001.
     *
     * @return the new active CampusMap stage.
     * @throws IllegalStateException if canTransition() returns false.
     */
    CampusMap advanceToNextMap();

    /**
     * Resets run state back to Map 1 upon Academic Suspension.
     */
    void resetRun();
}
