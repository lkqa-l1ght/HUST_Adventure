package hust.adventure.behavior.ai;

import com.badlogic.gdx.math.Vector2;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * CPU-optimized behavior for swarm enemies. Moves directly towards the player. Enemies pass through walls and each
 * other to optimize CPU.
 */
public class SimpleSwarmBehavior implements AIBehavior {
    private static final Vector2 tmpVector = new Vector2();

    public SimpleSwarmBehavior() {
    }

    @Override
    public void execute(final Enemy enemy, final float delta, final Player player, final EntityManager entityManager) {
        if (player == null || enemy == null || entityManager == null) {
            return;
        }

        // Calculate direction to player
        tmpVector.set(player.getX() - enemy.getX(), player.getY() - enemy.getY());
        final float distance = tmpVector.len();

        if (distance > 0) {
            // Normalize and scale by enemy's speed – di chuyển trực tiếp, xuyên tường
            tmpVector.nor().scl(enemy.getSpeed() * delta);
            enemy.setX(enemy.getX() + tmpVector.x);
            enemy.setY(enemy.getY() + tmpVector.y);
        }
    }

}
