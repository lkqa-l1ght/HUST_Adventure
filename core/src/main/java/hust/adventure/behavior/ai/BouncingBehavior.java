package hust.adventure.behavior.ai;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;
import hust.adventure.collision.CollisionManager;

public class BouncingBehavior implements AIBehavior {
    private float vx, vy;
    private final float worldWidth, worldHeight;

    public BouncingBehavior(float vx, float vy, float worldWidth, float worldHeight) {
        this.vx = vx;
        this.vy = vy;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    @Override
    public void execute(Enemy enemy, float delta, Player player, EntityManager entityManager) {
        CollisionManager cm = enemy.getCollisionManager();
        float currentMapW = (cm != null) ? cm.getMapWidth() : worldWidth;
        float currentMapH = (cm != null) ? cm.getMapHeight() : worldHeight;

        enemy.setX(enemy.getX() + vx * delta);
        enemy.setY(enemy.getY() + vy * delta);

        if (cm == null || !cm.isInfinite()) {
            if (enemy.getX() < 0) {
                enemy.setX(0);
                vx = -vx;
            } else if (enemy.getX() > currentMapW - enemy.getWidth()) {
                enemy.setX(currentMapW - enemy.getWidth());
                vx = -vx;
            }

            if (enemy.getY() < 0) {
                enemy.setY(0);
                vy = -vy;
            } else if (enemy.getY() > currentMapH - enemy.getHeight()) {
                enemy.setY(currentMapH - enemy.getHeight());
                vy = -vy;
            }
        }
    }
}
