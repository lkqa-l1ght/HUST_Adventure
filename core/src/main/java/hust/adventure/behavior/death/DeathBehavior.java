package hust.adventure.behavior.death;

import hust.adventure.entities.enemies.Enemy;

/**
 * Polymorphic interface representing custom logic to execute upon enemy destruction.
 */
public interface DeathBehavior {
    /**
     * Executes logic when the owner enemy is destroyed.
     *
     * @param enemy the enemy being destroyed
     */
    void onDestroy(final Enemy enemy);
}
