package hust.adventure.ui.puzzle;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import hust.adventure.HustGame;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.events.EventDispatcher;
import hust.adventure.screens.PlayScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpeedMathPuzzleTest {
    private PlayScreen mockContext;

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
        mockContext = mock(PlayScreen.class);

        final HustGame mockGame = mock(HustGame.class);
        final GameAssetManager mockAssetManager = mock(GameAssetManager.class);
        final Texture mockTexture = mock(Texture.class);

        when(mockContext.getGame()).thenReturn(mockGame);
        when(mockGame.getAssetManager()).thenReturn(mockAssetManager);
        when(mockAssetManager.getTexture(AssetPaths.UI_TEXT_BOX)).thenReturn(mockTexture);

        EventDispatcher.resetInstance(); // Reset Singleton event dispatcher state
    }

    @Test
    public void testRoundTimeLimitsCalculation() {
        final SpeedMathPuzzle puzzle = new SpeedMathPuzzle();
        puzzle.init(mockContext);

        final float[] roundTimeLimits = puzzle.getRoundTimeLimits();

        // Check Round 1 and Round 7 (index 6) time limits
        assertEquals(20.0f, roundTimeLimits[0], 0.001f);
        assertEquals(10.0f, roundTimeLimits[6], 0.001f);
    }

    @Test
    public void testQuestionGenerationBounds() {
        final SpeedMathPuzzle puzzle = new SpeedMathPuzzle();
        puzzle.init(mockContext);

        // Test Tier 1 (Round 1)
        puzzle.setCurrentRound(1);
        puzzle.generateQuestion();
        final int a1 = puzzle.getOperandA();
        final int b1 = puzzle.getOperandB();
        final int ans1 = puzzle.getCorrectAnswer();

        assertTrue(a1 >= 10 && a1 <= 99);
        assertTrue(b1 >= 10 && b1 <= 99);
        assertEquals(a1 + b1, ans1);

        // Test Tier 2 (Round 6)
        puzzle.setCurrentRound(6);
        puzzle.generateQuestion();
        final int a6 = puzzle.getOperandA();
        final int b6 = puzzle.getOperandB();
        final int ans6 = puzzle.getCorrectAnswer();

        assertTrue(a6 >= 10 && a6 <= 99);
        assertTrue(b6 >= 10 && b6 <= 99);
        assertEquals(a6 + b6, ans6);
    }
}
