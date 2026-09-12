package hust.adventure.progression;

import java.util.EnumSet;
import java.util.Set;

/**
 * Implementation of the MapDirector state machine.
 * Completely decoupled from LibGDX graphics pipelines.
 */
public class MapDirectorImpl implements MapDirector {
    private CampusMap currentStage;
    private float stageElapsedTime;
    private boolean deadlinesCleared;
    private boolean challengesCleared;
    private boolean bossDefeated;
    private final Set<CampusMap> stagesWithKeyItemCollected = EnumSet.noneOf(CampusMap.class);

    /**
     * Constructs a new MapDirector initialized at Map 1 (Outside).
     */
    public MapDirectorImpl() {
        resetRun();
    }

    @Override
    public CampusMap getCurrentStage() {
        return currentStage;
    }

    @Override
    public String getCurrentStageKeyItemId() {
        return currentStage != null ? currentStage.getRequiredKeyItemId() : null;
    }

    @Override
    public float getStageElapsedTime() {
        return stageElapsedTime;
    }

    @Override
    public void update(final float delta) {
        if (delta > 0) {
            stageElapsedTime += delta;
        }
    }

    @Override
    public boolean canTransition() {
        if (currentStage == null) {
            return false;
        }

        return switch (currentStage) {
            case MAP_1_OUTSIDE, MAP_2_FLOOR_1 -> deadlinesCleared && hasCurrentStageKeyItem();
            case MAP_3_LIBRARY -> challengesCleared && bossDefeated && hasCurrentStageKeyItem();
            case MAP_4_LAB -> deadlinesCleared && hasCurrentStageKeyItem();
            case FINAL_MAP_BOSS -> bossDefeated;
        };
    }

    private boolean hasCurrentStageKeyItem() {
        return stagesWithKeyItemCollected.contains(currentStage);
    }

    @Override
    public boolean isPortalActive() {
        return canTransition();
    }

    @Override
    public boolean isAutoAttackAllowed() {
        if (currentStage == null) {
            return true;
        }
        return switch (currentStage) {
            case MAP_2_FLOOR_1 -> false; // Floor 1 is a peaceful lecture hall phase
            case MAP_3_LIBRARY -> challengesCleared; // Suppressed during academic challenges
            default -> true;
        };
    }

    @Override
    public void onDeadlinesCleared() {
        this.deadlinesCleared = true;
    }

    @Override
    public void onKeyItemCollected(final String keyItemId) {
        if (keyItemId != null && currentStage != null) {
            final String requiredId = currentStage.getRequiredKeyItemId();
            if (requiredId != null && requiredId.equalsIgnoreCase(keyItemId.trim())) {
                stagesWithKeyItemCollected.add(currentStage);
            }
        }
    }

    @Override
    public void onAcademicChallengesCleared() {
        this.challengesCleared = true;
    }

    @Override
    public void onBossDefeated() {
        this.bossDefeated = true;
    }

    @Override
    public CampusMap advanceToNextMap() {
        if (!canTransition()) {
            throw new IllegalStateException("Cannot advance stage: gating requirements not met for " + currentStage);
        }
        final CampusMap next = currentStage.getNextStage();
        if (next == null) {
            throw new IllegalStateException("Cannot advance beyond final stage: " + currentStage);
        }
        setStage(next);
        return currentStage;
    }

    private void setStage(final CampusMap stage) {
        this.currentStage = stage;
        this.stageElapsedTime = 0f;
        this.deadlinesCleared = false;
        this.challengesCleared = false;
        this.bossDefeated = false;
    }

    @Override
    public void resetRun() {
        this.stagesWithKeyItemCollected.clear();
        setStage(CampusMap.MAP_1_OUTSIDE);
    }
}
