package hust.adventure.core.assets;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import hust.adventure.core.data.EnemyDataLoader;
import hust.adventure.core.data.ItemDataLoader;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.core.data.LevelDataLoader;
import hust.adventure.entities.enemies.EnemyConfig;
import hust.adventure.items.base.ItemConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameAssetManagerTest {

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
    }

    @Test
    public void testAssetPathsConstants() {
        assertEquals("ui/text_box.png", AssetPaths.UI_TEXT_BOX);
        assertEquals("ui/background.png", AssetPaths.UI_BACKGROUND);
        assertEquals("map/Library1.jpg", AssetPaths.MAP_LIBRARY_BG);
        assertEquals("map/Boss Room.jpg", AssetPaths.MAP_BOSS_ROOM_BG);
        assertEquals("character/bullet.png", AssetPaths.CHARACTER_BULLET);
        assertEquals("audio/sfx/ui_click.wav", AssetPaths.SFX_UI_CLICK);
        assertEquals("audio/sfx/puzzle_boss/answer_correct.mp3", AssetPaths.SFX_ANSWER_CORRECT);
        assertEquals("audio/sfx/puzzle_boss/answer_wrong.mp3", AssetPaths.SFX_ANSWER_WRONG);
        assertEquals("audio/sfx/puzzle_boss/puzzle_wrong.mp3", AssetPaths.SFX_PUZZLE_WRONG);
        assertEquals("audio/sfx/puzzle_boss/puzzle_failed.mp3", AssetPaths.SFX_PUZZLE_FAILED);
        assertEquals("audio/sfx/puzzle_boss/puzzle_solved.mp3", AssetPaths.SFX_PUZZLE_SOLVED);
        assertEquals("audio/sfx/puzzle_boss/card_flip.mp3", AssetPaths.SFX_CARD_FLIP);
        assertEquals("audio/sfx/puzzle_boss/simon_tone_0.wav", AssetPaths.SFX_SIMON_TONE_0);
        assertEquals("audio/sfx/puzzle_boss/simon_tone_1.wav", AssetPaths.SFX_SIMON_TONE_1);
        assertEquals("audio/sfx/puzzle_boss/simon_tone_2.wav", AssetPaths.SFX_SIMON_TONE_2);
        assertEquals("audio/sfx/puzzle_boss/simon_tone_3.wav", AssetPaths.SFX_SIMON_TONE_3);
    }

    @Test
    public void testDynamicAssetLoadingFromDataLoaders() {
        final GameAssetManager assetManager = new GameAssetManager();

        final LevelDataLoader mockLevelLoader = mock(LevelDataLoader.class);
        final ItemDataLoader mockItemLoader = mock(ItemDataLoader.class);
        final EnemyDataLoader mockEnemyLoader = mock(EnemyDataLoader.class);

        // Mock Level
        final LevelConfig levelConfig = new LevelConfig();
        levelConfig.setMapPath("map/tang1.tmx");
        final Array<LevelConfig> levelConfigs = new Array<>();
        levelConfigs.add(levelConfig);
        when(mockLevelLoader.getAllConfigs()).thenReturn(levelConfigs);

        // Mock Item
        final ItemConfig itemConfig = new ItemConfig();
        itemConfig.setSpritePath("items/brain.png");
        when(mockItemLoader.getAllConfigs()).thenReturn(Collections.singletonList(itemConfig));

        // Mock Enemy
        final EnemyConfig enemyConfig = new EnemyConfig();
        enemyConfig.setSpritePath("character/enemy/syntax_error.png");
        enemyConfig.setAnimationFrames(new String[] { "character/enemy/library_boss/sprite_0000.png" });
        when(mockEnemyLoader.getAllConfigs()).thenReturn(Collections.singletonList(enemyConfig));

        assertDoesNotThrow(() -> {
            assetManager.loadAllAssets(mockLevelLoader, mockItemLoader, mockEnemyLoader);
        });
    }
}
