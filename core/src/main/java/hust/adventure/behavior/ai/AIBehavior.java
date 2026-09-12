package hust.adventure.behavior.ai;

import hust.adventure.behavior.movement.MovementBehavior;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * Interface for AI strategies (Strategy Pattern).
 */
public interface AIBehavior extends MovementBehavior {
    void execute(Enemy enemy, float delta, Player player, EntityManager entityManager);

    @Override
    default void update(MapObject entity, float delta) {
        Enemy enemy = (Enemy) entity;
        execute(enemy, delta, enemy.getPlayer(), enemy.getEntityManager());
    }
}
