package hust.adventure.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;
import hust.adventure.items.consumable.Consumable;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class LootDropService implements EventListener, Disposable {
    private static final float DEFAULT_EXP_GEM_VALUE = 10f;

    private final EntityFactory entityFactory;
    private final GameAssetManager assetManager;
    private final ItemManager itemManager;

    public LootDropService(EntityFactory entityFactory, GameAssetManager assetManager, ItemManager itemManager) {
        this.entityFactory = entityFactory;
        this.assetManager = assetManager;
        this.itemManager = itemManager;
        EventDispatcher.getInstance().addListener(EventType.ENTITY_DIED, this);
    }

    @Override
    public void onEvent(GameEvent<?> event) {
        if (event.getType() == EventType.ENTITY_DIED) {
            MapObject deadEntity = (MapObject) event.getData();

            if (deadEntity instanceof Enemy) {
                // Determine drop
                if (((Enemy) deadEntity).isBoss()) {
                    // Boss drops nothing
                } else {
                    entityFactory.createExpGem(deadEntity.getX(), deadEntity.getY(), DEFAULT_EXP_GEM_VALUE);

                    // 30% chance to drop a random consumable
                    if (MathUtils.random() < 0.3f) {
                        final Array<Item> consumables = new Array<>();
                        for (final Item item : itemManager.getAllItems()) {
                            if (item instanceof Consumable) {
                                consumables.add(item);
                            }
                        }
                        if (consumables.size > 0) {
                            final Item chosen = consumables.random();
                            entityFactory.createItemDrop(deadEntity.getX(), deadEntity.getY(), chosen, Color.WHITE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        EventDispatcher.getInstance().removeListener(EventType.ENTITY_DIED, this);
    }
}
