package hust.adventure.ui.puzzle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import hust.adventure.screens.PlayScreen;

/**
 * Common interface for all puzzle mini-games.
 */
public interface PuzzleGame {
    /**
     * Returns the relative path to the background texture for this puzzle.
     *
     * @return texture path string
     */
    String getBackgroundPath();

    /**
     * Initializes the puzzle with the PlayScreen context.
     *
     * @param context the active play screen
     */
    void init(final PlayScreen context);

    /**
     * Updates puzzle logic, animations, and timers.
     *
     * @param delta frame time in seconds
     */
    void update(final float delta);

    /**
     * Renders puzzle UI elements and interaction prompts.
     *
     * @param shapeRenderer the shape renderer
     * @param batch the sprite batch
     */
    void render(final ShapeRenderer shapeRenderer, final SpriteBatch batch);

    /**
     * Returns whether the puzzle has been successfully solved.
     *
     * @return true if solved
     */
    boolean isSolved();

    /**
     * Resets puzzle state for replay.
     */
    void reset();

    /**
     * Disposes resources held by the puzzle.
     */
    void dispose();
}
