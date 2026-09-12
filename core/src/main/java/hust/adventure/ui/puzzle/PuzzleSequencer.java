package hust.adventure.ui.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.screens.PlayScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates and sequences multiple PuzzleGame instances.
 */
public class PuzzleSequencer implements Disposable {
    private static final float TRANSITION_DURATION = 1.5f;

    public enum SequencerState {
        WAITING,
        ACTIVE,
        TRANSITION_DELAY,
        COMPLETE
    }

    private final List<PuzzleGame> puzzles;
    private int activeIndex;
    private SequencerState state;
    private float transitionTimer;
    private PlayScreen context;

    // Zero-allocation UI rendering buffers
    private final StringBuilder textBuilder;
    private final Color dimColor = new Color(0f, 0f, 0f, 0.5f);

    public PuzzleSequencer() {
        this.puzzles = new ArrayList<>();
        this.activeIndex = 0;
        this.state = SequencerState.WAITING;
        this.transitionTimer = 0f;
        this.textBuilder = new StringBuilder();
    }

    /**
     * Adds a puzzle to the sequence list.
     *
     * @param puzzle The puzzle to add.
     */
    public void addPuzzle(final PuzzleGame puzzle) {
        if (puzzle != null) {
            puzzles.add(puzzle);
        }
    }

    /**
     * Initializes all registered puzzles.
     *
     * @param ctx The play screen context.
     */
    public void init(final PlayScreen ctx) {
        this.context = ctx;
        for (final PuzzleGame puzzle : puzzles) {
            puzzle.init(ctx);
        }
        if (!puzzles.isEmpty()) {
            this.state = SequencerState.ACTIVE;
            this.activeIndex = 0;
            puzzles.get(activeIndex).reset();
        } else {
            this.state = SequencerState.COMPLETE;
        }
    }

    public void update(final float delta) {
        if (state == SequencerState.ACTIVE) {
            if (activeIndex < puzzles.size()) {
                final PuzzleGame currentPuzzle = puzzles.get(activeIndex);
                currentPuzzle.update(delta);
                if (currentPuzzle.isSolved()) {
                    EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.PUZZLE_SOLVED, null));
                    if (activeIndex == puzzles.size() - 1) {
                        state = SequencerState.COMPLETE;
                    } else {
                        state = SequencerState.TRANSITION_DELAY;
                        transitionTimer = TRANSITION_DURATION;
                    }
                }
            }
        } else if (state == SequencerState.TRANSITION_DELAY) {
            transitionTimer -= delta;
            if (transitionTimer <= 0) {
                activeIndex++;
                if (activeIndex < puzzles.size()) {
                    puzzles.get(activeIndex).reset();
                    state = SequencerState.ACTIVE;
                } else {
                    state = SequencerState.COMPLETE;
                }
            }
        }
    }

    private void drawBackground(final ShapeRenderer shapeRenderer, final SpriteBatch batch, final PuzzleGame puzzle) {
        final String bgPath = puzzle.getBackgroundPath();
        Texture bgTex = null;
        if (bgPath != null && !bgPath.isEmpty() && context != null && context.getGame() != null) {
            final com.badlogic.gdx.assets.AssetManager am = context.getGame().getAssetManager().getManager();
            if (am.isLoaded(bgPath)) {
                bgTex = context.getGame().getAssetManager().getTexture(bgPath);
            }
        }

        if (bgTex != null) {
            batch.begin();
            batch.draw(bgTex, 0, 0, 800, 600);
            batch.end();
        } else {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(0.9f, 0.9f, 0.9f, 1f)); // xám trắng neutral
            shapeRenderer.rect(0, 0, 800, 600);
            shapeRenderer.end();
        }

        // Draw semi-transparent dimming layer over the background
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(dimColor);
        shapeRenderer.rect(0, 0, 800, 600);
        shapeRenderer.end();
        com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    public void render(final ShapeRenderer shapeRenderer, final SpriteBatch batch) {
        if (state == SequencerState.ACTIVE || state == SequencerState.TRANSITION_DELAY) {
            if (activeIndex < puzzles.size()) {
                final PuzzleGame currentPuzzle = puzzles.get(activeIndex);
                drawBackground(shapeRenderer, batch, currentPuzzle);
                currentPuzzle.render(shapeRenderer, batch);
            }
        }

        if (state == SequencerState.TRANSITION_DELAY) {
            // Render dark overlay using ShapeRenderer
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, 0.6f);
            shapeRenderer.rect(0, 0, 800, 600);
            shapeRenderer.end();

            // Render overlay transition text
            batch.begin();
            final BitmapFont font = context.getFont();
            font.setColor(Color.YELLOW);
            
            textBuilder.setLength(0);
            textBuilder.append("VƯỢT ẢI THÀNH CÔNG! Chuẩn bị sang ải tiếp theo...");
            
            font.draw(batch, textBuilder, 150f, 300f);
            font.setColor(Color.WHITE);
            batch.end();
        }
    }

    public boolean isAllSolved() {
        return state == SequencerState.COMPLETE;
    }

    @Override
    public void dispose() {
        for (final PuzzleGame puzzle : puzzles) {
            puzzle.dispose();
        }
        puzzles.clear();
    }
}
