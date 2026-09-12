package hust.adventure.entities.factory;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import hust.adventure.collision.CollisionManager;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.core.context.PlayerStats;
import hust.adventure.core.data.EnemyDataLoader;
import hust.adventure.entities.Candle;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.ExpGem;
import hust.adventure.entities.ItemDrop;
import hust.adventure.entities.Projectile;
import hust.adventure.entities.StaticObject;
import hust.adventure.entities.base.LightProvider;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.enemies.EnemyConfig;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.player.PlayerPersistenceService;
import hust.adventure.items.base.Item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EntityFactoryImpl entity construction, configuration parsing,
 * coordinate boundary clamping, and object pooling lifecycle hooks.
 */
public class EntityFactoryImplTest {

    private GameAssetManager assetManager;
    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private EnemyDataLoader enemyDataManager;
    private GameProgressContext progressContext;
    private PlayerPersistenceService persistenceService;

    private EntityFactoryImpl entityFactory;

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);

        assetManager = mock(GameAssetManager.class);
        entityManager = mock(EntityManager.class);
        collisionManager = mock(CollisionManager.class);
        enemyDataManager = mock(EnemyDataLoader.class);
        progressContext = mock(GameProgressContext.class);
        persistenceService = mock(PlayerPersistenceService.class);

        // Mock textures for constructor cache
        when(assetManager.getTexture(anyString())).thenReturn(mock(Texture.class));
        when(collisionManager.canMove(any(), anyFloat(), anyFloat())).thenReturn(true);

        entityFactory = new EntityFactoryImpl(
                assetManager,
                entityManager,
                collisionManager,
                enemyDataManager,
                progressContext,
                persistenceService
        );
    }

    @Test
    public void testConstructorNullCheck() {
        assertThrows(NullPointerException.class, () -> new EntityFactoryImpl(null, entityManager, collisionManager, enemyDataManager, progressContext, persistenceService));
        assertThrows(NullPointerException.class, () -> new EntityFactoryImpl(assetManager, null, collisionManager, enemyDataManager, progressContext, persistenceService));
        assertThrows(NullPointerException.class, () -> new EntityFactoryImpl(assetManager, entityManager, null, enemyDataManager, progressContext, persistenceService));
        assertThrows(NullPointerException.class, () -> new EntityFactoryImpl(assetManager, entityManager, collisionManager, null, progressContext, persistenceService));
        assertThrows(NullPointerException.class, () -> new EntityFactoryImpl(assetManager, entityManager, collisionManager, enemyDataManager, null, persistenceService));
        assertThrows(NullPointerException.class, () -> new EntityFactoryImpl(assetManager, entityManager, collisionManager, enemyDataManager, progressContext, null));
    }

    @Test
    public void testCreateEnemyUnknownThrows() {
        when(enemyDataManager.getEnemyConfig("unknown")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> entityFactory.createEnemy("unknown", 100f, 100f));
    }

    @Test
    public void testCreateEnemySuccess() {
        EnemyConfig config = mock(EnemyConfig.class);
        when(config.getType()).thenReturn("bug");
        when(config.getName()).thenReturn("BugEnemy");
        when(config.getMaxHp()).thenReturn(20f);
        when(config.getWidth()).thenReturn(32f);
        when(config.getHeight()).thenReturn(32f);
        when(config.getSpeed()).thenReturn(50f);
        when(config.getColor()).thenReturn(Color.RED);
        when(config.getContactDamage()).thenReturn(5f);

        when(enemyDataManager.getEnemyConfig("bug")).thenReturn(config);
        
        Player mockPlayer = mock(Player.class);
        when(progressContext.getPlayer()).thenReturn(mockPlayer);

        when(collisionManager.getMapWidth()).thenReturn(800f);
        when(collisionManager.getMapHeight()).thenReturn(600f);
        when(collisionManager.isInfinite()).thenReturn(false);

        MapObject created = entityFactory.createEnemy("bug", 100f, 150f);

        assertNotNull(created);
        assertTrue(created instanceof Enemy);
        Enemy enemy = (Enemy) created;
        assertEquals("bug", enemy.getId());
        assertEquals("BugEnemy", enemy.getName());
        assertEquals(20f, enemy.getMaxHp());
        assertEquals(100f, enemy.getX());
        assertEquals(150f, enemy.getY());

        verify(entityManager).addEntity(enemy);
    }

    @Test
    public void testCreateProjectile() {
        when(assetManager.getTexture(AssetPaths.CHARACTER_BULLET)).thenReturn(mock(Texture.class));

        Projectile p = entityFactory.createProjectile(10f, 20f, 1f, 2f, 15f, Color.BLUE, true);

        assertNotNull(p);
        assertEquals(10f, p.getX());
        assertEquals(20f, p.getY());
        verify(entityManager).addEntity(p);

        entityFactory.freeEntity(p);
    }

    @Test
    public void testCreateItemDrop() {
        Item mockItem = mock(Item.class);
        when(mockItem.getSpritePath()).thenReturn("items/gear.png");
        when(assetManager.getTexture("items/gear.png")).thenReturn(mock(Texture.class));

        ItemDrop drop = entityFactory.createItemDrop(50f, 60f, mockItem, Color.YELLOW);

        assertNotNull(drop);
        assertEquals(50f, drop.getX());
        assertEquals(60f, drop.getY());
        assertSame(mockItem, drop.getItem());
        verify(entityManager).addEntity(drop);
    }

    @Test
    public void testCreateExpGem() throws Exception {
        ExpGem gem = entityFactory.createExpGem(30f, 40f, 10f);

        assertNotNull(gem);
        assertEquals(30f, gem.getX());
        assertEquals(40f, gem.getY());

        java.lang.reflect.Field amountField = ExpGem.class.getDeclaredField("amount");
        amountField.setAccessible(true);
        float amount = (float) amountField.get(gem);
        assertEquals(10f, amount);

        verify(entityManager).addEntity(gem);

        entityFactory.freeEntity(gem);
    }

    @Test
    public void testCreateFloatingBook() {
        LightProvider lightProvider = mock(LightProvider.class);
        MapObject book = entityFactory.createFloatingBook(80f, 90f, lightProvider);

        assertNotNull(book);
        assertEquals(80f, book.getX());
        assertEquals(90f, book.getY());
        verify(entityManager).addEntity(book);
    }

    @Test
    public void testCreateCandle() {
        when(assetManager.getTexture("Phong_doc/1.png")).thenReturn(mock(Texture.class));
        LightProvider lightProvider = mock(LightProvider.class);

        MapObject candle = entityFactory.createCandle(100f, 110f, lightProvider);

        assertNotNull(candle);
        assertTrue(candle instanceof Candle);
        assertEquals(100f, candle.getX());
        assertEquals(110f, candle.getY());
        verify(entityManager).addEntity(candle);
    }

    @Test
    public void testCreateStaticNPC() {
        MapObject npc = entityFactory.createStaticNPC(200f, 220f, "HustGuy", Color.WHITE);

        assertNotNull(npc);
        assertTrue(npc instanceof StaticObject);
        assertEquals(200f, npc.getX());
        assertEquals(220f, npc.getY());
        assertEquals("HustGuy", npc.getName());
        verify(entityManager).addEntity(npc);
    }

    @Test
    public void testCreateEnemyCollisionReposition() {
        EnemyConfig config = mock(EnemyConfig.class);
        when(config.getType()).thenReturn("bug");
        when(config.getName()).thenReturn("BugEnemy");
        when(config.getMaxHp()).thenReturn(20f);
        when(config.getWidth()).thenReturn(32f);
        when(config.getHeight()).thenReturn(32f);
        when(config.getSpeed()).thenReturn(50f);
        when(config.getColor()).thenReturn(Color.RED);
        when(config.getContactDamage()).thenReturn(5f);

        when(enemyDataManager.getEnemyConfig("bug")).thenReturn(config);

        Player mockPlayer = mock(Player.class);
        when(progressContext.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getX()).thenReturn(500f);
        when(mockPlayer.getY()).thenReturn(500f);

        when(collisionManager.getMapWidth()).thenReturn(800f);
        when(collisionManager.getMapHeight()).thenReturn(600f);
        when(collisionManager.isInfinite()).thenReturn(false);

        // Make canMove return false for initial spawn at 100f, 150f, and concentric search
        when(collisionManager.canMove(any(), anyFloat(), anyFloat())).thenReturn(false);

        // Let's make a point along the vector to player valid (spawn at 100, 150, player at 500, 500)
        float dx = 400f;
        float dy = 350f;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        float dirX = dx / dist;
        float dirY = dy / dist;
        float expectedX = 100f + dirX * 32f;
        float expectedY = 150f + dirY * 32f;

        when(collisionManager.canMove(any(), eq(expectedX), eq(expectedY))).thenReturn(true);

        MapObject created = entityFactory.createEnemy("bug", 100f, 150f);

        assertNotNull(created);
        assertTrue(created instanceof Enemy);
        Enemy enemy = (Enemy) created;
        assertEquals(expectedX, enemy.getX(), 0.1f);
        assertEquals(expectedY, enemy.getY(), 0.1f);
    }
}
