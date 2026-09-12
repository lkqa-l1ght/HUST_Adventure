package hust.adventure.behavior.ai;

import com.badlogic.gdx.math.MathUtils;

import hust.adventure.collision.CollisionManager;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * AI movement behavior that makes the enemy wander to random target positions within the map boundaries.
 */
public class WanderAIBehavior implements AIBehavior {
    private float targetX;
    private float targetY;
    private boolean hasTarget = false;

    public WanderAIBehavior() {
    }

    @Override
    public void execute(final Enemy enemy, final float delta, final Player player, final EntityManager entityManager) {
        if (enemy == null || enemy.isDead()) {
            return;
        }

        if (!hasTarget) {
            pickNewTarget(enemy);
        }

        final float dx = targetX - enemy.getX();
        final float dy = targetY - enemy.getY();
        final float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < 10f) {
            pickNewTarget(enemy);
        } else {
            enemy.setX(enemy.getX() + (dx / dist) * enemy.getSpeed() * delta);
            enemy.setY(enemy.getY() + (dy / dist) * enemy.getSpeed() * delta);
        }
    }

    private void pickNewTarget(final Enemy enemy) {
        final CollisionManager cm = enemy.getCollisionManager();
        if (cm != null && !cm.isInfinite() && cm.getMapWidth() > 0 && cm.getMapHeight() > 0) {
            final float marginX = enemy.getWidth();
            final float marginY = enemy.getHeight();
            targetX = MathUtils.random(marginX, cm.getMapWidth() - marginX);
            targetY = MathUtils.random(marginY, cm.getMapHeight() - marginY);
        } else {
            // Fallback for infinite maps or missing boundaries: wander around current position
            final float radius = 300f;
            targetX = enemy.getX() + MathUtils.random(-radius, radius);
            targetY = enemy.getY() + MathUtils.random(-radius, radius);
        }
        hasTarget = true;
    }
}
