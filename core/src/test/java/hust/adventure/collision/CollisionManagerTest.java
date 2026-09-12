package hust.adventure.collision;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.WallEntity;
import hust.adventure.entities.base.MapObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for CollisionManager spatial hash coordinate mapping, boundaries, and wall collisions.
 */
public class CollisionManagerTest {
    private EntityManager entityManager;
    private CollisionManager collisionManager;

    private static class TestMapObject extends MapObject {
        public TestMapObject(float x, float y, float width, float height) {
            super(x, y, width, height);
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        }
    }

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
        entityManager = mock(EntityManager.class);
        collisionManager = new CollisionManager(entityManager, 64f);
    }

    private TiledMap createMockMap(int width, int height, int tileW, int tileH) {
        TiledMap map = mock(TiledMap.class);
        MapProperties properties = new MapProperties();
        properties.put("width", width);
        properties.put("height", height);
        properties.put("tilewidth", tileW);
        properties.put("tileheight", tileH);
        Mockito.when(map.getProperties()).thenReturn(properties);
        return map;
    }

    @Test
    public void testHashCoordinateMapping() throws Exception {
        Method hashMethod = CollisionManager.class.getDeclaredMethod("hash", int.class, int.class);
        hashMethod.setAccessible(true);

        long h1 = (long) hashMethod.invoke(collisionManager, 5, -10);
        long expected = ((long) 5 << 32) | (-10 & 0xffffffffL);
        assertEquals(expected, h1);
    }

    @Test
    public void testBoundaryChecks() {
        collisionManager.setInfinite(false);
        TiledMap mockMap = createMockMap(100, 100, 32, 32);
        collisionManager.setMap(mockMap, new ArrayList<>());
        assertEquals(3200f, collisionManager.getMapWidth(), 0.01f);
        assertEquals(3200f, collisionManager.getMapHeight(), 0.01f);

        TestMapObject entity = new TestMapObject(100f, 100f, 32f, 32f);

        // Within boundaries
        assertTrue(collisionManager.canMove(entity, 100f, 100f));
        assertTrue(collisionManager.canMove(entity, 3180f, 3180f));

        // Outside boundaries
        assertFalse(collisionManager.canMove(entity, -10f, 100f));
        assertFalse(collisionManager.canMove(entity, 100f, 3210f));

        // Infinite mode bypasses boundaries
        collisionManager.setInfinite(true);
        assertTrue(collisionManager.canMove(entity, -10f, 100f));
        assertTrue(collisionManager.canMove(entity, 100f, 3210f));
    }

    @Test
    public void testWallCollisionCheck() {
        collisionManager.setInfinite(false);
        TiledMap mockMap = createMockMap(100, 100, 32, 32);
        Rectangle wallRect = new Rectangle(100f, 100f, 32f, 32f);
        WallEntity wall = new WallEntity(wallRect);

        List<WallEntity> walls = new ArrayList<>();
        walls.add(wall);
        collisionManager.setMap(mockMap, walls);

        TestMapObject entity = new TestMapObject(50f, 50f, 32f, 32f);

        // Move entity to overlapping position
        assertFalse(collisionManager.canMove(entity, 110f, 110f));

        // Move entity to a non-overlapping position
        assertTrue(collisionManager.canMove(entity, 50f, 50f));
    }
}
