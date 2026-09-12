package hust.adventure.core.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;

import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;

import hust.adventure.core.data.EnemyDataLoader;
import hust.adventure.core.data.ItemDataLoader;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.core.data.LevelDataLoader;
import hust.adventure.entities.enemies.EnemyConfig;
import hust.adventure.items.base.ItemConfig;

public class GameAssetManager {
    private final AssetManager manager;
    private Texture whitePixel;

    public GameAssetManager() {
        this.manager = new AssetManager();
        manager.setLoader(TiledMap.class, new TmxMapLoader());
    }

    public void loadAllAssets(final LevelDataLoader levelDataLoader) {
        loadAllAssets(levelDataLoader, null, null);
    }

    public void loadAllAssets(final LevelDataLoader levelDataLoader,
                              final ItemDataLoader itemDataLoader,
                              final EnemyDataLoader enemyDataLoader) {
        // UI and shared background textures
        manager.load(AssetPaths.UI_BACKGROUND, Texture.class);
        manager.load(AssetPaths.UI_TEXT_BOX, Texture.class);
        manager.load(AssetPaths.MAP_BOSS_ROOM_BG, Texture.class);
        manager.load(AssetPaths.MAP_LIBRARY_BG, Texture.class);
        manager.load(AssetPaths.CHARACTER_ATLAS, Texture.class);
        manager.load(AssetPaths.CHARACTER_BULLET, Texture.class);

        // Load items dynamically from ItemDataLoader (Single Source of Truth)
        if (itemDataLoader != null) {
            for (final ItemConfig config : itemDataLoader.getAllConfigs()) {
                if (config.getSpritePath() != null && !config.getSpritePath().isEmpty()) {
                    manager.load(config.getSpritePath(), Texture.class);
                }
            }
        }

        // Load enemies dynamically from EnemyDataLoader (Single Source of Truth)
        if (enemyDataLoader != null) {
            for (final EnemyConfig config : enemyDataLoader.getAllConfigs()) {
                if (config.getSpritePath() != null && !config.getSpritePath().isEmpty()) {
                    manager.load(config.getSpritePath(), Texture.class);
                }
                if (config.getAnimationFrames() != null) {
                    for (final String frame : config.getAnimationFrames()) {
                        if (frame != null && !frame.isEmpty()) {
                            manager.load(frame, Texture.class);
                        }
                    }
                }
            }
        }

        // Load card matching game textures
        manager.load("puzzle/cards/card_back.png", Texture.class);
        for (int i = 1; i <= 18; i++) {
            final String facePath = "puzzle/cards/card_face_" + (i < 10 ? "0" + i : i) + ".png";
            manager.load(facePath, Texture.class);
        }

        // Load maps dynamically from level configurations (Single Source of Truth)
        if (levelDataLoader != null) {
            for (final LevelConfig config : levelDataLoader.getAllConfigs()) {
                if (config.getMapPath() != null && !config.getMapPath().isEmpty()) {
                    manager.load(config.getMapPath(), TiledMap.class);
                }
            }
        }

        // Phong_doc textures
        for (int i = 19; i <= 29; i++)
            manager.load("Phong_doc/" + i + ".png", Texture.class);
        for (int i = 47; i <= 51; i++)
            manager.load("Phong_doc/" + i + ".png", Texture.class);
        manager.load("Phong_doc/1.png", Texture.class);

        // Sound effects
        manager.load("audio/sfx/ui_click.wav", Sound.class);
        manager.load("audio/sfx/level_up.wav", Sound.class);
        manager.load("audio/sfx/item_use.wav", Sound.class);

        // New SFX
        manager.load("audio/sfx/enemy/enemy_die.mp3", Sound.class);
        manager.load("audio/sfx/enemy/enemy_hit.mp3", Sound.class);
        manager.load("audio/sfx/enemy/enemy_spawn.mp3", Sound.class);
        manager.load("audio/sfx/enemy/time_alarm.mp3", Sound.class);

        manager.load("audio/sfx/interact/chest_open.mp3", Sound.class);
        manager.load("audio/sfx/interact/pick_up_gem.mp3", Sound.class);
        manager.load("audio/sfx/interact/pickup_item.mp3", Sound.class);

        manager.load("audio/sfx/player/GarlicPulseAura.mp3", Sound.class);
        manager.load("audio/sfx/player/Player Firing Bun Dau.wav", Sound.class);
        manager.load("audio/sfx/player/Player Firing Magic Wand.mp3", Sound.class);
        manager.load("audio/sfx/player/Player Firing Whip.mp3", Sound.class);
        manager.load("audio/sfx/player/Player Hurt.mp3", Sound.class);
        manager.load("audio/sfx/player/Use Consumable (Coffee).mp3", Sound.class);
        manager.load("audio/sfx/player/Use Consumable (Khô gà).mp3", Sound.class);

        manager.load("audio/sfx/puzzle_boss/answer_correct.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/answer_wrong.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/book_drag.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/book_snap.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/dialogue_next.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/paper_spawn.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/puzzle_failed.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/puzzle_solved.mp3", Sound.class);
        manager.load("audio/sfx/puzzle_boss/puzzle_wrong.mp3", Sound.class);

        // Load new puzzle SFX
        manager.load("audio/sfx/puzzle_boss/simon_tone_0.wav", Sound.class);
        manager.load("audio/sfx/puzzle_boss/simon_tone_1.wav", Sound.class);
        manager.load("audio/sfx/puzzle_boss/simon_tone_2.wav", Sound.class);
        manager.load("audio/sfx/puzzle_boss/simon_tone_3.wav", Sound.class);
        manager.load("audio/sfx/puzzle_boss/card_flip.mp3", Sound.class);

        // Music tracks
        manager.load("audio/music/menu.mp3", Music.class);
        manager.load("audio/music/game_over.mp3", Music.class);
        manager.load("audio/music/win_menu.mp3", Music.class);

        // Load level-specific BGM tracks dynamically from level configurations
        if (levelDataLoader != null) {
            for (final LevelConfig config : levelDataLoader.getAllConfigs()) {
                if (config.getBgmPath() != null && !config.getBgmPath().isEmpty()) {
                    manager.load(config.getBgmPath(), Music.class);
                }
            }
        }
    }

    public boolean update() {
        return manager.update();
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public Texture getTexture(String name) {
        return manager.get(name, Texture.class);
    }

    public TiledMap getTiledMap(String name) {
        return manager.get(name, TiledMap.class);
    }

    public Sound getSound(String name) {
        return manager.get(name, Sound.class);
    }

    public Music getMusic(String name) {
        return manager.get(name, Music.class);
    }

    public Texture getWhitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    public void dispose() {
        manager.dispose();
        if (whitePixel != null) {
            whitePixel.dispose();
        }
    }

    public AssetManager getManager() {
        return manager;
    }
}
