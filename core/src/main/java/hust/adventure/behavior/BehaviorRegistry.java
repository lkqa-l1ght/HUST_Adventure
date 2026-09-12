package hust.adventure.behavior;

import com.badlogic.gdx.utils.ObjectMap;

import hust.adventure.behavior.ai.AIBehavior;
import hust.adventure.behavior.ai.BouncingBehavior;
import hust.adventure.behavior.ai.ChaseBehavior;
import hust.adventure.behavior.ai.FleeBehavior;
import hust.adventure.behavior.ai.SimpleSwarmBehavior;
import hust.adventure.behavior.ai.TelegraphedChargeBehavior;
import hust.adventure.behavior.ai.WanderAIBehavior;
import hust.adventure.behavior.attack.AttackBehavior;
import hust.adventure.behavior.attack.RadialRotatingShootingBehavior;
import hust.adventure.behavior.attack.ShootingBehavior;
import hust.adventure.behavior.death.DeathBehavior;
import hust.adventure.behavior.death.SplitDeathBehavior;
import hust.adventure.entities.enemies.EnemyConfig;

/**
 * Unified registry mapping behavior names to suppliers for AIBehavior, AttackBehavior, and DeathBehavior, returning a
 * populated EnemyBehaviors container for a given EnemyConfig.
 */
public final class BehaviorRegistry {
    private static final ObjectMap<String, AIBehaviorProvider> movementProviders = new ObjectMap<>();
    private static final ObjectMap<String, AttackBehaviorProvider> attackProviders = new ObjectMap<>();
    private static final ObjectMap<String, DeathBehaviorProvider> deathProviders = new ObjectMap<>();

    static {
        // AI update/movement behaviors mapping
        movementProviders.put("chase", config -> new ChaseBehavior());
        movementProviders.put("flee", config -> new FleeBehavior(config.getSafeDistance()));
        movementProviders.put("bouncing", config -> new BouncingBehavior(config.getVx(), config.getVy(),
                config.getWorldWidth(), config.getWorldHeight()));
        movementProviders.put("simple_swarm", config -> new SimpleSwarmBehavior());
        movementProviders.put("wander", config -> new WanderAIBehavior());
        movementProviders.put("telegraphed_charge", config -> new TelegraphedChargeBehavior());

        // Combat/attack behaviors mapping
        attackProviders.put("shooting", config -> new ShootingBehavior(config.getFireInterval(),
                config.getProjectileSpeed(), config.getProjectileDamage()));
        attackProviders.put("radial_rotating_shooting",
                config -> new RadialRotatingShootingBehavior(config.getFireInterval(), config.getProjectileSpeed(),
                        config.getProjectileDamage()));

        // Death behaviors mapping
        deathProviders.put("split", config -> new SplitDeathBehavior(config.getSplitType(), config.getSplitCount(),
                config.getSplitOffset()));
    }

    private BehaviorRegistry() {
        // Prevent instantiation
    }

    /**
     * Creates and resolves the behaviors for an enemy based on its configuration.
     *
     * @param config the enemy configuration
     * @return the resolved EnemyBehaviors container
     */
    public static EnemyBehaviors create(final EnemyConfig config) {
        if (config == null) {
            return new EnemyBehaviors(null, null, null);
        }

        AIBehavior move = null;
        if (config.getMovementBehavior() != null) {
            final AIBehaviorProvider provider = movementProviders.get(config.getMovementBehavior().toLowerCase());
            if (provider != null) {
                move = provider.create(config);
            }
        }

        AttackBehavior attack = null;
        if (config.getAttackBehavior() != null) {
            final AttackBehaviorProvider provider = attackProviders.get(config.getAttackBehavior().toLowerCase());
            if (provider != null) {
                attack = provider.create(config);
            }
        }

        DeathBehavior death = null;
        if (config.getDeathBehavior() != null) {
            final DeathBehaviorProvider provider = deathProviders.get(config.getDeathBehavior().toLowerCase());
            if (provider != null) {
                death = provider.create(config);
            }
        }

        return new EnemyBehaviors(move, attack, death);
    }

    /**
     * Registers a new AIBehavior provider dynamically at runtime.
     *
     * @param name     the identifier of the behavior
     * @param provider the behavior supplier
     */
    public static void registerMovement(final String name, final AIBehaviorProvider provider) {
        if (name != null && provider != null) {
            movementProviders.put(name.toLowerCase(), provider);
        }
    }

    /**
     * Registers a new AttackBehavior provider dynamically at runtime.
     *
     * @param name     the identifier of the attack behavior
     * @param provider the behavior supplier
     */
    public static void registerAttack(final String name, final AttackBehaviorProvider provider) {
        if (name != null && provider != null) {
            attackProviders.put(name.toLowerCase(), provider);
        }
    }

    /**
     * Registers a new DeathBehavior provider dynamically at runtime.
     *
     * @param name     the identifier of the death behavior
     * @param provider the behavior supplier
     */
    public static void registerDeath(final String name, final DeathBehaviorProvider provider) {
        if (name != null && provider != null) {
            deathProviders.put(name.toLowerCase(), provider);
        }
    }

    /**
     * Functional interface supplying AIBehavior based on config.
     */
    @FunctionalInterface
    public interface AIBehaviorProvider {
        /**
         * Creates an AIBehavior instance.
         *
         * @param config the enemy configuration
         * @return the created AIBehavior
         */
        AIBehavior create(EnemyConfig config);
    }

    /**
     * Functional interface supplying AttackBehavior based on config.
     */
    @FunctionalInterface
    public interface AttackBehaviorProvider {
        /**
         * Creates an AttackBehavior instance.
         *
         * @param config the enemy configuration
         * @return the created AttackBehavior
         */
        AttackBehavior create(EnemyConfig config);
    }

    /**
     * Functional interface supplying DeathBehavior based on config.
     */
    @FunctionalInterface
    public interface DeathBehaviorProvider {
        /**
         * Creates a DeathBehavior instance.
         *
         * @param config the enemy configuration
         * @return the created DeathBehavior
         */
        DeathBehavior create(EnemyConfig config);
    }
}
