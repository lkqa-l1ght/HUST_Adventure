package hust.adventure.behavior.attack;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * Attack behavior that periodically fires projectiles towards the player.
 */
public class ShootingBehavior implements AttackBehavior {
    private final float fireInterval;
    private final float projectileSpeed;
    private final float projectileDamage;
    private float fireTimer = 0f;

    /**
     * Constructs a ShootingBehavior.
     *
     * @param fireInterval     the time interval between shots in seconds
     * @param projectileSpeed  the velocity/speed of fired projectiles
     * @param projectileDamage the damage dealt by fired projectiles
     */
    public ShootingBehavior(final float fireInterval, final float projectileSpeed, final float projectileDamage) {
        this.fireInterval = fireInterval;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
    }

    @Override
    public void execute(final Enemy enemy, final float delta, final Player player, final EntityManager entityManager) {
        if (enemy == null || player == null || enemy.getFactory() == null || enemy.isDead()) {
            return;
        }

        final float dx = player.getX() - enemy.getX();
        final float dy = player.getY() - enemy.getY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        fireTimer += delta;
        if (fireTimer >= fireInterval) {
            fireTimer = 0f;
            if (dist > 0f) {
                final float vx = (dx / dist) * projectileSpeed;
                final float vy = (dy / dist) * projectileSpeed;
                enemy.getFactory().createProjectile(enemy.getX(), enemy.getY(), vx, vy, projectileDamage,
                        enemy.getColor(), false);
            }
        }
    }
}
