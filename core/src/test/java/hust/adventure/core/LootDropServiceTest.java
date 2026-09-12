package hust.adventure.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;
import hust.adventure.items.consumable.Consumable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

/**
 * Unit tests for LootDropService ensuring correct spawning logic based on event triggers.
 */
public class LootDropServiceTest {
    private EntityFactory entityFactory;
    private GameAssetManager assetManager;
    private ItemManager itemManager;
    private LootDropService lootDropService;

    private static class TestConsumable extends Item implements Consumable {
        public TestConsumable(String id) {
            super(id, "Test Consumable", "Description", "sprite.png");
        }

        @Override
        public void consume(hust.adventure.entities.base.Character consumer) {
        }
    }

    @BeforeEach
    public void setUp() {
        EventDispatcher.resetInstance();
        entityFactory = mock(EntityFactory.class);
        assetManager = mock(GameAssetManager.class);
        itemManager = new ItemManager();
        lootDropService = new LootDropService(entityFactory, assetManager, itemManager);

        // Register a consumable item in the registry
        itemManager.register(new TestConsumable("test_potion"));
    }

    @Test
    public void testNormalEnemyDiedSpawnsExpGem() {
        Enemy mockEnemy = mock(Enemy.class);
        when(mockEnemy.isBoss()).thenReturn(false);
        when(mockEnemy.getX()).thenReturn(100f);
        when(mockEnemy.getY()).thenReturn(200f);

        try (MockedStatic<MathUtils> mockedMathUtils = mockStatic(MathUtils.class)) {
            mockedMathUtils.when(MathUtils::random).thenReturn(0.5f);

            EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ENTITY_DIED, mockEnemy));

            verify(entityFactory, times(1)).createExpGem(100f, 200f, 10f);
            verify(entityFactory, never()).createItemDrop(anyFloat(), anyFloat(), any(), any());
        }
    }

    @Test
    public void testNormalEnemyDiedWithConsumableDrop() {
        Enemy mockEnemy = mock(Enemy.class);
        when(mockEnemy.isBoss()).thenReturn(false);
        when(mockEnemy.getX()).thenReturn(100f);
        when(mockEnemy.getY()).thenReturn(200f);

        try (MockedStatic<MathUtils> mockedMathUtils = mockStatic(MathUtils.class)) {
            mockedMathUtils.when(MathUtils::random).thenReturn(0.1f);

            EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ENTITY_DIED, mockEnemy));

            verify(entityFactory, times(1)).createExpGem(100f, 200f, 10f);
            verify(entityFactory, times(1)).createItemDrop(eq(100f), eq(200f), any(Item.class), eq(Color.WHITE));
        }
    }

    @Test
    public void testBossEnemyDiedDropsNothing() {
        Enemy mockBoss = mock(Enemy.class);
        when(mockBoss.isBoss()).thenReturn(true);
        when(mockBoss.getX()).thenReturn(300f);
        when(mockBoss.getY()).thenReturn(400f);

        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ENTITY_DIED, mockBoss));

        verify(entityFactory, never()).createExpGem(anyFloat(), anyFloat(), anyFloat());
        verify(entityFactory, never()).createItemDrop(anyFloat(), anyFloat(), any(), any());
    }

    @Test
    public void testNonEnemyDiedDropsNothing() {
        MapObject mockObject = mock(MapObject.class);
        when(mockObject.getX()).thenReturn(100f);
        when(mockObject.getY()).thenReturn(200f);

        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ENTITY_DIED, mockObject));

        verify(entityFactory, never()).createExpGem(anyFloat(), anyFloat(), anyFloat());
        verify(entityFactory, never()).createItemDrop(anyFloat(), anyFloat(), any(), any());
    }
}
