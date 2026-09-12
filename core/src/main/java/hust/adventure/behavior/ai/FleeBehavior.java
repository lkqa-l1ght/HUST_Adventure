package hust.adventure.behavior.ai;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

public class FleeBehavior implements AIBehavior {
    private final float safeDistance;

    public FleeBehavior(float safeDistance) {
        this.safeDistance = safeDistance;
    }

    @Override
    public void execute(Enemy enemy, float delta, Player player, EntityManager entityManager) {
        float dx = player.getX() - enemy.getX();
        float dy = player.getY() - enemy.getY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < safeDistance && dist > 0) {
            float nextX = enemy.getX() - (dx / dist) * enemy.getSpeed() * delta;
            float nextY = enemy.getY() - (dy / dist) * enemy.getSpeed() * delta;
            enemy.setX(nextX);
            enemy.setY(nextY);
        }
    }
}
