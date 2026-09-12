package hust.adventure.core.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.GdxRuntimeException;

import hust.adventure.events.EventListener;
import hust.adventure.events.GameEvent;
import hust.adventure.events.EntityDamagedEvent;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * Robust audio management system for Hust Adventure. Handles sound categories, pitch randomization, throttling, and
 * smooth music crossfading.
 */
public class AudioManager implements EventListener, Disposable {
    private final GameAssetManager assetManager;

    private float masterVolume = 1.0f;
    private float musicVolume = 0.4f; // Mix Hierarchy: background music is softer
    private float sfxVolume = 0.8f; // SFX is prominent
    private float uiVolume = 0.9f; // UI is distinct

    // Music State & Crossfading
    private Music currentMusic;
    private String currentMusicPath;
    private Music nextMusic;
    private String nextMusicPath;

    private enum FadeState {
        NONE, FADING_OUT, FADING_IN
    }

    private FadeState fadeState = FadeState.NONE;
    private float fadeTimer = 0f;
    private static final float FADE_DURATION = 0.5f; // half a second crossfade

    // Throttling / Limiting spam (Map of asset path -> last play time in millis)
    private final ObjectMap<String, Long> lastPlayTimes = new ObjectMap<>();
    private static final long SFX_COOLDOWN_MS = 80; // 80ms cooldown for identical sound effects

    public AudioManager(final GameAssetManager assetManager) {
        if (assetManager == null) {
            throw new IllegalArgumentException("AssetManager cannot be null");
        }
        this.assetManager = assetManager;
    }

    /**
     * Updates music volume fades. Should be called every frame on the GL Thread.
     * 
     * @param delta time since last frame in seconds.
     */
    public void update(float delta) {
        if (fadeState == FadeState.NONE) {
            return;
        }

        if (fadeState == FadeState.FADING_OUT) {
            fadeTimer += delta;
            float progress = Math.min(1f, fadeTimer / FADE_DURATION);
            if (currentMusic != null) {
                currentMusic.setVolume((1f - progress) * musicVolume * masterVolume);
            }
            if (progress >= 1f) {
                if (currentMusic != null) {
                    currentMusic.stop();
                }
                currentMusic = nextMusic;
                currentMusicPath = nextMusicPath;
                nextMusic = null;
                nextMusicPath = null;

                if (currentMusic != null) {
                    currentMusic.setVolume(0f);
                    currentMusic.setLooping(true);
                    currentMusic.play();
                    fadeState = FadeState.FADING_IN;
                    fadeTimer = 0f;
                } else {
                    fadeState = FadeState.NONE;
                }
            }
        } else if (fadeState == FadeState.FADING_IN) {
            fadeTimer += delta;
            float progress = Math.min(1f, fadeTimer / FADE_DURATION);
            if (currentMusic != null) {
                currentMusic.setVolume(progress * musicVolume * masterVolume);
            }
            if (progress >= 1f) {
                fadeState = FadeState.NONE;
            }
        }
    }

    public void playSound(final String path) {
        playSound(path, false);
    }

    public void playSound(final String path, boolean isUi) {
        if (path == null)
            return;

        // Sound Throttling: limit spamming of the same SFX
        long now = System.currentTimeMillis();
        Long lastPlay = lastPlayTimes.get(path);
        if (lastPlay != null && now - lastPlay < SFX_COOLDOWN_MS) {
            return; // Skip to avoid sonic spam
        }
        lastPlayTimes.put(path, now);

        try {
            Sound sound = assetManager.getSound(path);
            if (sound != null) {
                float volume = (isUi ? uiVolume : sfxVolume) * masterVolume;

                // Pitch Randomization to prevent fatigue, except for UI sounds
                float pitch = 1.0f;
                if (!isUi && isPitchRandomizable(path)) {
                    pitch = MathUtils.random(0.9f, 1.1f);
                }

                sound.play(volume, pitch, 0f);
            }
        } catch (GdxRuntimeException e) {
            Gdx.app.error("AudioManager", "Failed to play sound: " + path, e);
        }
    }

    private boolean isPitchRandomizable(String path) {
        if (path == null) return false;
        String lower = path.toLowerCase();
        return lower.contains("shoot") || lower.contains("whip") || lower.contains("magic")
                || lower.contains("hit") || lower.contains("pickup") || lower.contains("pick_up")
                || lower.contains("die") || lower.contains("hurt") || lower.contains("bun dau");
    }

    public void playMusic(final String path, boolean loop) {
        if (path == null)
            return;
        if (currentMusicPath != null && currentMusicPath.equals(path)) {
            // Already playing or switching to this music
            return;
        }
        if (nextMusicPath != null && nextMusicPath.equals(path)) {
            return;
        }

        try {
            Music loadedMusic = assetManager.getMusic(path);
            if (loadedMusic == null)
                return;

            // If no music is currently playing, start immediately
            if (currentMusic == null) {
                currentMusic = loadedMusic;
                currentMusicPath = path;
                currentMusic.setLooping(loop);
                currentMusic.setVolume(musicVolume * masterVolume);
                currentMusic.play();
                fadeState = FadeState.NONE;
            } else {
                // Otherwise start crossfade transition
                nextMusic = loadedMusic;
                nextMusicPath = path;
                fadeState = FadeState.FADING_OUT;
                fadeTimer = 0f;
            }
        } catch (GdxRuntimeException e) {
            Gdx.app.error("AudioManager", "Failed to play music: " + path, e);
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
            currentMusicPath = null;
        }
        nextMusic = null;
        nextMusicPath = null;
        fadeState = FadeState.NONE;
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying() && fadeState == FadeState.NONE) {
            currentMusic.play();
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = MathUtils.clamp(volume, 0f, 1f);
        if (currentMusic != null && fadeState == FadeState.NONE) {
            currentMusic.setVolume(musicVolume * masterVolume);
        }
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    public void setUiVolume(float volume) {
        this.uiVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = MathUtils.clamp(volume, 0f, 1f);
        if (currentMusic != null && fadeState == FadeState.NONE) {
            currentMusic.setVolume(musicVolume * masterVolume);
        }
    }

    @Override
    public void onEvent(final GameEvent<?> event) {
        switch (event.getType()) {
        case PLAY_SFX:
            if (event.getData() instanceof String) {
                String path = (String) event.getData();
                playSound(path, path != null && path.contains("ui"));
            }
            break;
        case PLAY_BGM:
            if (event.getData() instanceof String) {
                playMusic((String) event.getData(), true);
            }
            break;
        case LEVEL_UP:
            playSound("audio/sfx/level_up.wav", false);
            break;
        case EXP_GAINED:
            playSound("audio/sfx/interact/pick_up_gem.mp3", false);
            break;
        case ITEM_PICKED_UP:
            playSound("audio/sfx/interact/pickup_item.mp3", false);
            break;
        case ITEM_USED:
            if (event.getData() instanceof String) {
                String itemId = (String) event.getData();
                if (itemId != null && itemId.startsWith("coffee")) {
                    playSound("audio/sfx/player/Use Consumable (Coffee).mp3", false);
                } else if ("kho_ga".equals(itemId)) {
                    playSound("audio/sfx/player/Use Consumable (Khô gà).mp3", false);
                } else {
                    playSound("audio/sfx/item_use.wav", false);
                }
            } else {
                playSound("audio/sfx/item_use.wav", false);
            }
            break;
        case PUZZLE_SOLVED:
            playSound(AssetPaths.SFX_PUZZLE_SOLVED, false);
            break;
        case PUZZLE_FAILED:
            playSound(AssetPaths.SFX_PUZZLE_FAILED, false);
            break;
        case ENTITY_DAMAGED:
            if (event.getData() instanceof EntityDamagedEvent) {
                EntityDamagedEvent edEvent = (EntityDamagedEvent) event.getData();
                if (edEvent.getEntity() instanceof Player) {
                    playSound("audio/sfx/player/Player Hurt.mp3", false);
                } else if (edEvent.getEntity() instanceof Enemy) {
                    playSound("audio/sfx/enemy/enemy_hit.mp3", false);
                }
            }
            break;
        case ENTITY_DIED:
            if (event.getData() instanceof Player) {
                playSound("audio/sfx/game_over.mp3", false);
            } else if (event.getData() instanceof Enemy) {
                playSound("audio/sfx/enemy/enemy_die.mp3", false);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void dispose() {
        stopMusic();
    }
}
