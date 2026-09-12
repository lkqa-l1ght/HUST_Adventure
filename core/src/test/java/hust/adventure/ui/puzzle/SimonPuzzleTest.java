package hust.adventure.ui.puzzle;

import hust.adventure.core.assets.AssetPaths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimonPuzzleTest {

    @Test
    @DisplayName("SimonPuzzle initializes tone audio paths using AssetPaths constants")
    void simonPuzzleUsesCentralizedAudioConstants() {
        final SimonPuzzle puzzle = new SimonPuzzle();
        final String[] tones = puzzle.getTones();

        assertNotNull(tones);
        assertEquals(4, tones.length);
        assertEquals(AssetPaths.SFX_SIMON_TONE_0, tones[0]);
        assertEquals(AssetPaths.SFX_SIMON_TONE_1, tones[1]);
        assertEquals(AssetPaths.SFX_SIMON_TONE_2, tones[2]);
        assertEquals(AssetPaths.SFX_SIMON_TONE_3, tones[3]);

        assertArrayEquals(new String[] {
                AssetPaths.SFX_SIMON_TONE_0,
                AssetPaths.SFX_SIMON_TONE_1,
                AssetPaths.SFX_SIMON_TONE_2,
                AssetPaths.SFX_SIMON_TONE_3
        }, tones);
    }

    @Test
    @DisplayName("SimonPuzzle resets properly to initial state")
    void simonPuzzleResetState() {
        final SimonPuzzle puzzle = new SimonPuzzle();
        puzzle.reset();

        assertFalse(puzzle.isSolved());
    }
}
