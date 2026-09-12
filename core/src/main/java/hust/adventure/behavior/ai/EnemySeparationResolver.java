package hust.adventure.behavior.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.enemies.Enemy;

/**
 * Stateless utility class holding spatial separation and circle depenetration algorithms for enemies to prevent
 * overlapping/stacking.
 */
public final class EnemySeparationResolver {
    private static final float SEPARATION_RADIUS = 8f;
    private static final float SEPARATION_RADIUS_SQ = SEPARATION_RADIUS * SEPARATION_RADIUS;
    private static final float SEPARATION_WEIGHT = 0.25f;
    private static final float PUSH_FACTOR = 0.125f;

    // Temporary vectors to avoid garbage collection allocations during gameplay updates
    private static final Vector2 tmpSeparation = new Vector2();
    private static final Vector2 tmpRepulsion = new Vector2();

    /**
     * Private constructor to prevent instantiation of this static utility class.
     */
    private EnemySeparationResolver() {
    }

    /**
     * Resolves separation steering and circle depenetration for the given enemy against other active enemies.
     *
     * @param self          the enemy entity to resolve separation for
     * @param entityManager the manager containing the list of active entities
     */
    public static void resolve(final Enemy self, final EntityManager entityManager) {
        if (entityManager == null || self == null || self.isDestroyed()) {
            return;
        }

        final Array<MapObject> entities = entityManager.getEntities();
        if (entities == null || entities.size <= 1) {
            return;
        }

        // Reset temporary accumulation vector
        tmpSeparation.setZero();

        // 1. Separation Steering Pass
        // PERF: Using index-based loop to avoid iterator allocations
        for (int i = 0; i < entities.size; i++) {
            final MapObject entity = entities.get(i);
            if (!(entity instanceof Enemy) || entity == self || entity.isDestroyed()) {
                continue;
            }

            final Enemy neighbor = (Enemy) entity;
            final float dx = self.getX() - neighbor.getX();
            final float dy = self.getY() - neighbor.getY();
            // PERF: Using squared distance check to avoid Math.sqrt in the hot path
            final float distSq = dx * dx + dy * dy;

            if (distSq < SEPARATION_RADIUS_SQ && distSq > 0) {
                final float dist = (float) Math.sqrt(distSq);
                // Compute normalized repulsion vector pointing away from neighbor
                tmpRepulsion.set(dx / dist, dy / dist);
                tmpSeparation.add(tmpRepulsion);
            }
        }

        // Apply separation nudge
        if (!tmpSeparation.isZero()) {
            tmpSeparation.scl(SEPARATION_WEIGHT);
            self.setX(self.getX() + tmpSeparation.x);
            self.setY(self.getY() + tmpSeparation.y);
        }

        // 2. Circle Depenetration Pass (Absolute Overlap Resolution)
        // PERF: Using index-based loop to avoid iterator allocations
        for (int i = 0; i < entities.size; i++) {
            final MapObject entity = entities.get(i);
            if (!(entity instanceof Enemy) || entity == self || entity.isDestroyed()) {
                continue;
            }

            final Enemy neighbor = (Enemy) entity;
            final float dx = self.getX() - neighbor.getX();
            final float dy = self.getY() - neighbor.getY();
            final float distSq = dx * dx + dy * dy;

            // Compute minimum distance based on hitbox widths
            final float minDist = (self.getHitboxWidth() + neighbor.getHitboxWidth()) * 0.5f;
            if (minDist <= 0) {
                continue;
            }

            final float minDistSq = minDist * minDist;

            if (distSq < minDistSq) {
                if (distSq > 0) {
                    final float actualDist = (float) Math.sqrt(distSq);
                    final float overlap = minDist - actualDist;
                    // Push self away along the repulsion axis
                    final float pushX = (dx / actualDist) * overlap * PUSH_FACTOR;
                    final float pushY = (dy / actualDist) * overlap * PUSH_FACTOR;
                    self.setX(self.getX() + pushX);
                    self.setY(self.getY() + pushY);
                } else {
                    // Exact overlap (distance = 0): apply a tiny deterministic offset using hashcode to break symmetry
                    if (self.hashCode() > neighbor.hashCode()) {
                        self.setX(self.getX() + 1f);
                    } else {
                        self.setX(self.getX() - 1f);
                    }
                }
            }
        }
    }
}
