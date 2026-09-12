package hust.adventure.behavior.attack;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * Polymorphic interface representing combat/attack logic to execute during the update loop.
 */
public interface AttackBehavior {
    /**
     * Executes the attack logic for the enemy.
     *
     * @param enemy         the executing enemy
     * @param delta         time since last frame in seconds
     * @param player        the player target
     * @param entityManager the global entity manager
     */
    void execute(final Enemy enemy, final float delta, final Player player, final EntityManager entityManager);
}
