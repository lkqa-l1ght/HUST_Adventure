package hust.adventure.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.factory.EntityFactory;
import java.util.Comparator;

/**
 * Manages the lifecycle, updates, and rendering of all game entities. Implements Y-sorting for 2D top-down perspective.
 */
public class EntityManager implements Disposable {
    private final Array<MapObject> entities;
    private final Array<MapObject> pendingAdd;
    private final Comparator<MapObject> yComparator;

    public EntityManager() {
        this.entities = new Array<>();
        this.pendingAdd = new Array<>();
        this.yComparator = (e1, e2) -> {
            final float b1 = e1.getSortingY() - e1.getHeight() / 2f;
            final float b2 = e2.getSortingY() - e2.getHeight() / 2f;
            final int comp = Float.compare(b2, b1);
            if (comp != 0) {
                return comp;
            }
            final int compZ = Integer.compare(e1.getZIndex(), e2.getZIndex());
            if (compZ != 0) {
                return compZ;
            }
            return Integer.compare(e1.getSubZIndex(), e2.getSubZIndex());
        };
    }

    public void addEntity(final MapObject entity) {
        if (entity == null)
            throw new IllegalArgumentException("Entity cannot be null");
        pendingAdd.add(entity);
    }

    public void update(final float delta, final EntityFactory factory) {
        // Process pending additions
        if (pendingAdd.size > 0) {
            entities.addAll(pendingAdd);
            pendingAdd.clear();
        }

        // Update and cleanup destroyed entities
        for (int i = entities.size - 1; i >= 0; i--) {
            final MapObject entity = entities.get(i);
            entity.update(delta);

            if (entity.isDestroyed()) {
                entity.dispose();
                entities.removeIndex(i);

                // Return to pool if it's a BaseEntity and Poolable
                if (factory != null && entity instanceof Pool.Poolable) {
                    factory.freeEntity(entity);
                }
            }
        }
    }

    public void draw(final SpriteBatch batch) {
        // Y-sorting for depth perception
        entities.sort(yComparator);

        for (final MapObject entity : entities) {
            entity.draw(batch);
        }
    }

    @Override
    public void dispose() {
        for (final MapObject entity : entities) {
            entity.dispose();
        }
        entities.clear();
        pendingAdd.clear();
    }

    public boolean hasActiveEnemies() {
        for (int i = 0; i < entities.size; i++) {
            final MapObject e = entities.get(i);
            if (e instanceof Enemy && !e.isDestroyed()) {
                return true;
            }
        }
        for (int i = 0; i < pendingAdd.size; i++) {
            final MapObject e = pendingAdd.get(i);
            if (e instanceof Enemy && !e.isDestroyed()) {
                return true;
            }
        }
        return false;
    }

    public Array<MapObject> getEntities() {
        return entities;
    }
}
