package hust.adventure.behavior;

import hust.adventure.behavior.ai.AIBehavior;
import hust.adventure.behavior.attack.AttackBehavior;
import hust.adventure.behavior.death.DeathBehavior;

/**
 * Container class holding movement, attack, and death behaviors of an enemy.
 */
public class EnemyBehaviors {
    private final AIBehavior movementBehavior;
    private final AttackBehavior attackBehavior;
    private final DeathBehavior deathBehavior;

    /**
     * Constructs a container of enemy behaviors.
     *
     * @param movementBehavior the behavior for movement AI
     * @param attackBehavior   the behavior for combat/attacking
     * @param deathBehavior    the behavior executed on destruction
     */
    public EnemyBehaviors(final AIBehavior movementBehavior, final AttackBehavior attackBehavior,
            final DeathBehavior deathBehavior) {
        this.movementBehavior = movementBehavior;
        this.attackBehavior = attackBehavior;
        this.deathBehavior = deathBehavior;
    }

    /**
     * Gets the movement/AI behavior.
     *
     * @return the movement behavior
     */
    public AIBehavior getMovementBehavior() {
        return movementBehavior;
    }

    /**
     * Gets the attack behavior.
     *
     * @return the attack behavior
     */
    public AttackBehavior getAttackBehavior() {
        return attackBehavior;
    }

    /**
     * Gets the death behavior.
     *
     * @return the death behavior
     */
    public DeathBehavior getDeathBehavior() {
        return deathBehavior;
    }
}
