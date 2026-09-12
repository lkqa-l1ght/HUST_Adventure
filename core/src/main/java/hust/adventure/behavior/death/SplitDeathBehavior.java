package hust.adventure.behavior.death;

import hust.adventure.entities.enemies.Enemy;

/**
 * Implementation of DeathBehavior that handles spawning child split enemies upon parent death.
 */
public class SplitDeathBehavior implements DeathBehavior {
    private final String splitType;
    private final int splitCount;
    private final float splitOffset;

    /**
     * Constructs a SplitDeathBehavior.
     *
     * @param splitType   the enemy type identifier to spawn
     * @param splitCount  the number of split children to spawn
     * @param splitOffset the horizontal offset applied to the children
     */
    public SplitDeathBehavior(final String splitType, final int splitCount, final float splitOffset) {
        this.splitType = splitType;
        this.splitCount = splitCount;
        this.splitOffset = splitOffset;
    }

    @Override
    public void onDestroy(final Enemy enemy) {
        if (enemy == null || enemy.isSplit() || enemy.getFactory() == null) {
            return;
        }
        if (splitType == null || splitType.trim().isEmpty()) {
            return;
        }

        for (int i = 0; i < splitCount; i++) {
            final float offsetVal = (i == 0) ? -splitOffset : splitOffset;
            final Enemy child = (Enemy) enemy.getFactory().createEnemy(splitType, enemy.getX() + offsetVal,
                    enemy.getY());
            if (child != null) {
                child.setSplit(true);
            }
        }
    }
}
