package hust.adventure.utils;

import com.badlogic.gdx.utils.PoolManager;
import hust.adventure.entities.ExpGem;
import hust.adventure.entities.Projectile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.DefaultPool;

/**
 * Central registry for object pools using PoolManager. Only high-frequency objects are pooled to balance performance
 * and complexity.
 */
public class GamePools {
    private static final PoolManager manager = new PoolManager();

    static {
        // Register essential high-frequency objects
        manager.addPool(Projectile::new);
        manager.addPool(ExpGem::new);
        manager.addPool(Vector2::new);
    }

    /**
     * Dynamically registers a pool using a Supplier.
     */
    public static void registerPool(final DefaultPool.PoolSupplier<?> supplier) {
        manager.addPool(supplier);
    }

    /**
     * Obtains an object from the pool.
     */
    public static <T> T obtain(Class<T> type) {
        return manager.obtain(type);
    }

    /**
     * Frees an object back to the pool.
     */
    public static void free(Object object) {
        if (object != null) {
            manager.free(object);
        }
    }

    private GamePools() {
        // Utility class
    }
}
