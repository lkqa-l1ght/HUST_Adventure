package hust.adventure.core.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;

import static org.junit.jupiter.api.Assertions.*;

public class ProgressContextTest {
    private ProgressContext context;
    private ItemManager itemManager;

    @BeforeEach
    public void setUp() {
        itemManager = new ItemManager();
        context = new ProgressContext(itemManager);
    }

    @Test
    public void testGetCurrentStageKeyItemIdDelegatesToMapDirector() {
        assertEquals("note", context.getCurrentStageKeyItemId());

        context.getMapDirector().onDeadlinesCleared();
        context.getMapDirector().onKeyItemCollected("note");
        context.getMapDirector().advanceToNextMap();

        assertEquals("lecture_notes", context.getCurrentStageKeyItemId());

        context.setMapDirector(null);
        assertNull(context.getCurrentStageKeyItemId());
    }

    @Test
    public void testCheckpointInitialization() {
        assertFalse(context.hasCheckpoint());

        LevelConfig configOutside = new LevelConfig("FINAL_OUTSIDE", "Outside", "outside.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configOutside);

        assertTrue(context.hasCheckpoint());
        assertEquals("FINAL_OUTSIDE", context.getCurrentLevelConfig().getLevelId());
    }

    @Test
    public void testCheckpointSaveAndRestore() {
        LevelConfig configOutside = new LevelConfig("FINAL_OUTSIDE", "Outside", "outside.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configOutside);

        // Modify values after saving checkpoint
        context.setHp(150f);
        context.setLevel(5);
        context.setHasNao(true);
        context.getWeaponLevels().put("bun_dau", 3);
        context.getGearLevels().put("speed", 2);

        // Verify changes are active
        assertEquals(150f, context.getHp(), 0.01f);
        assertEquals(5, context.getLevel());
        assertTrue(context.isHasNao());
        assertEquals(3, context.getWeaponLevels().get("bun_dau"));
        assertEquals(2, context.getGearLevels().get("speed"));

        // Restore checkpoint
        context.restoreCheckpoint();

        // Should revert to starting values (HP=300, level=1, hasNao=false, weapons/gears empty)
        assertEquals(300f, context.getHp(), 0.01f);
        assertEquals(1, context.getLevel());
        assertFalse(context.isHasNao());
        assertTrue(context.getWeaponLevels().isEmpty());
        assertTrue(context.getGearLevels().isEmpty());
    }

    @Test
    public void testCheckpointInventoryCopy() {
        Item item = new Item("coffee", "Coffee", "Energy drink", "coffee_sprite");
        itemManager.register(item);

        LevelConfig configOutside = new LevelConfig("FINAL_OUTSIDE", "Outside", "outside.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configOutside);

        // Add item to inventory after checkpoint
        context.getGlobalInventory().addItem("coffee", 2);
        assertTrue(context.getGlobalInventory().hasItem(item, 2));

        // Restore checkpoint -> inventory should be empty again
        context.restoreCheckpoint();
        assertFalse(context.getGlobalInventory().hasItem(item, 1));
    }

    @Test
    public void testTransitionToDifferentLevelCreatesNewCheckpoint() {
        LevelConfig configOutside = new LevelConfig("FINAL_OUTSIDE", "Outside", "outside.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configOutside);

        // Acquire some progression in level 1
        context.setHp(200f);
        context.setLevel(2);
        context.getWeaponLevels().put("bun_dau", 2);

        // Transition to library config -> different level ID
        LevelConfig configLibrary = new LevelConfig("LIBRARY", "Library", "library.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configLibrary);

        // Changing level config should snapshotted the entrance state of LIBRARY (HP=200, Level=2, bun_dau=2)
        // Let's modify progression inside library
        context.setHp(50f);
        context.setLevel(4);
        context.getWeaponLevels().put("bun_dau", 4);

        // Restore checkpoint -> should revert to entrance state of LIBRARY
        context.restoreCheckpoint();
        assertEquals(200f, context.getHp(), 0.01f);
        assertEquals(2, context.getLevel());
        assertEquals(2, context.getWeaponLevels().get("bun_dau"));
    }

    @Test
    public void testReenteringSameLevelDoesNotOverwriteCheckpoint() {
        LevelConfig configOutside = new LevelConfig("FINAL_OUTSIDE", "Outside", "outside.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configOutside);

        // Modify state
        context.setHp(200f);

        // Set same config again (e.g. restart loadMap call)
        context.setCurrentLevelConfig(configOutside);

        // Modify state more
        context.setHp(50f);

        // Restore -> should revert to original checkpoint (HP=300), not the mid-level config update (HP=200)
        context.restoreCheckpoint();
        assertEquals(300f, context.getHp(), 0.01f);
    }

    @Test
    public void testResetClearsCheckpointsAndInventory() {
        LevelConfig configOutside = new LevelConfig("FINAL_OUTSIDE", "Outside", "outside.tmx", 0f, 0f, 1f, "bgm", null, false);
        context.setCurrentLevelConfig(configOutside);

        Item item = new Item("coffee", "Coffee", "Energy drink", "coffee_sprite");
        itemManager.register(item);
        context.getGlobalInventory().addItem("coffee", 1);

        assertTrue(context.hasCheckpoint());
        assertTrue(context.getGlobalInventory().hasItem(item, 1));

        context.reset();

        assertFalse(context.hasCheckpoint());
        assertFalse(context.getGlobalInventory().hasItem(item, 1));
    }
}
