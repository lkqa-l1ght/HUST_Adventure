package hust.adventure.behavior.attack;

import com.badlogic.gdx.math.MathUtils;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * Attack behavior that periodically fires projectiles in a radial pattern (circle) and rotates the angle of the circle
 * slightly in each successive wave.
 */
public class RadialRotatingShootingBehavior implements AttackBehavior {
    private final float fireInterval;
    private final float projectileSpeed;
    private final float projectileDamage;
    private float fireTimer = 0f;
    private float currentAngleOffset = 0f;
    private int wavesShot = 0;

    private static final int PROJECTILE_COUNT = 12;
    private static final float ANGLE_STEP = 10f * MathUtils.degreesToRadians;
    private static final int BURST_COUNT = 4;
    private static final float BURST_COOLDOWN = 2.0f;

    /**
     * Constructs a RadialRotatingShootingBehavior.
     *
     * @param fireInterval     the time interval between radial shots within a burst in seconds
     * @param projectileSpeed  the velocity/speed of fired projectiles
     * @param projectileDamage the damage dealt by fired projectiles
     */
    public RadialRotatingShootingBehavior(final float fireInterval, final float projectileSpeed,
            final float projectileDamage) {
        this.fireInterval = fireInterval;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
    }

    @Override
    public void execute(final Enemy enemy, final float delta, final Player player, final EntityManager entityManager) {
        if (enemy == null || player == null || enemy.getFactory() == null || enemy.isDead()) {
            return;
        }

        fireTimer += delta;
        final float currentInterval = (wavesShot >= BURST_COUNT) ? BURST_COOLDOWN : fireInterval;

        if (fireTimer >= currentInterval) {
            fireTimer = 0f;
            if (wavesShot >= BURST_COUNT) {
                wavesShot = 0;
            }

            for (int i = 0; i < PROJECTILE_COUNT; i++) {
                final float angle = currentAngleOffset + (i * 2f * MathUtils.PI / PROJECTILE_COUNT);
                final float vx = MathUtils.cos(angle) * projectileSpeed;
                final float vy = MathUtils.sin(angle) * projectileSpeed;

                enemy.getFactory().createProjectile(enemy.getX(), enemy.getY(), vx, vy, projectileDamage,
                        enemy.getColor(), false);
            }

            currentAngleOffset += ANGLE_STEP;
            if (currentAngleOffset >= 2f * MathUtils.PI) {
                currentAngleOffset -= 2f * MathUtils.PI;
            }
            wavesShot++;
        }
    }
}
