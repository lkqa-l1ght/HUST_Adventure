package hust.adventure.core.assets;

/**
 * Centralized registry of common asset path constants across UI, maps, and character subsystems.
 */
public final class AssetPaths {

    public static final String UI_TEXT_BOX = "ui/text_box.png";
    public static final String UI_BACKGROUND = "ui/background.png";

    public static final String MAP_LIBRARY_BG = "map/Library1.jpg";
    public static final String MAP_BOSS_ROOM_BG = "map/Boss Room.jpg";

    public static final String CHARACTER_ATLAS = "character/atlas.png";
    public static final String CHARACTER_BULLET = "character/bullet.png";

    public static final String MUSIC_WIN_MENU = "audio/music/win_menu.mp3";

    // Sound Effects - UI
    public static final String SFX_UI_CLICK = "audio/sfx/ui_click.wav";

    // Sound Effects - Weapons
    public static final String SFX_WEAPON_BUN_DAU = "audio/sfx/player/Player Firing Bun Dau.wav";
    public static final String SFX_WEAPON_MAGIC_WAND = "audio/sfx/player/Player Firing Magic Wand.mp3";
    public static final String SFX_WEAPON_GARLIC = "audio/sfx/player/GarlicPulseAura.mp3";
    public static final String SFX_WEAPON_WHIP = "audio/sfx/player/Player Firing Whip.mp3";

    // Sound Effects - Puzzles & Boss
    public static final String SFX_ANSWER_CORRECT = "audio/sfx/puzzle_boss/answer_correct.mp3";
    public static final String SFX_ANSWER_WRONG = "audio/sfx/puzzle_boss/answer_wrong.mp3";
    public static final String SFX_PUZZLE_WRONG = "audio/sfx/puzzle_boss/puzzle_wrong.mp3";
    public static final String SFX_PUZZLE_FAILED = "audio/sfx/puzzle_boss/puzzle_failed.mp3";
    public static final String SFX_PUZZLE_SOLVED = "audio/sfx/puzzle_boss/puzzle_solved.mp3";
    public static final String SFX_CARD_FLIP = "audio/sfx/puzzle_boss/card_flip.mp3";
    public static final String SFX_DIALOGUE_NEXT = "audio/sfx/puzzle_boss/dialogue_next.mp3";
    public static final String SFX_PAPER_SPAWN = "audio/sfx/puzzle_boss/paper_spawn.mp3";
    public static final String SFX_SIMON_TONE_0 = "audio/sfx/puzzle_boss/simon_tone_0.wav";
    public static final String SFX_SIMON_TONE_1 = "audio/sfx/puzzle_boss/simon_tone_1.wav";
    public static final String SFX_SIMON_TONE_2 = "audio/sfx/puzzle_boss/simon_tone_2.wav";
    public static final String SFX_SIMON_TONE_3 = "audio/sfx/puzzle_boss/simon_tone_3.wav";

    private AssetPaths() {
        // Prevent instantiation of constant utility class
    }
}
