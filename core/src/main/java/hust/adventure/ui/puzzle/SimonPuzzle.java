package hust.adventure.ui.puzzle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;
import hust.adventure.screens.PlayScreen;
import com.badlogic.gdx.graphics.GL20;

/**
 * Simon Game memory mini-game puzzle.
 */
public class SimonPuzzle extends BasePuzzleGame {
    private static final int MAX_ROUNDS = 8;

    public enum SimonState {
        IDLE, SHOWING_SEQUENCE, AWAITING_INPUT, EVALUATING, ROUND_WON, GAME_WON, FAILED_DELAY
    }

    private SimonState state;

    // Sequence details
    private final int[] sequence = new int[MAX_ROUNDS];
    private final int[] playerInput = new int[MAX_ROUNDS];
    private int currentRound;
    private int playbackIndex;
    private float playbackTimer;
    private boolean soundPlayedForStep;
    private float playbackDelayTimer;

    private int inputIndex;
    private int highlightButton;
    private float highlightTimer;

    // Buttons
    private final Rectangle[] buttons = new Rectangle[4];
    private final Color[] baseColors = new Color[4];
    private final Color[] highlightColors = new Color[4];
    private final String[] tones = new String[] {
            AssetPaths.SFX_SIMON_TONE_0,
            AssetPaths.SFX_SIMON_TONE_1,
            AssetPaths.SFX_SIMON_TONE_2,
            AssetPaths.SFX_SIMON_TONE_3
    };

    // Rendering assets & buffers
    private final StringBuilder textBuilder;
    private float stateTimer;

    private final Color bevelLight = new Color(1f, 1f, 1f, 0.3f);
    private final Color bevelDark = new Color(0f, 0f, 0f, 0.35f);
    private final Color slotColor = new Color(0.08f, 0.08f, 0.08f, 1f);
    private static final float BEVEL_T = 6f;
    private static final float SLOT_OFFSET = 6f;

    public SimonPuzzle() {
        this.state = SimonState.IDLE;
        this.isSolved = false;
        this.currentRound = 1;
        this.textBuilder = new StringBuilder();
    }

    @Override
    public void init(final PlayScreen ctx) {
        super.init(ctx);

        // Button boundaries: TL, TR, BL, BR
        buttons[0] = new Rectangle(240, 310, 150, 150); // Red
        buttons[1] = new Rectangle(410, 310, 150, 150); // Green
        buttons[2] = new Rectangle(240, 140, 150, 150); // Blue
        buttons[3] = new Rectangle(410, 140, 150, 150); // Yellow

        // Colors
        baseColors[0] = new Color(0.5f, 0f, 0f, 1f);
        baseColors[1] = new Color(0f, 0.5f, 0f, 1f);
        baseColors[2] = new Color(0f, 0f, 0.5f, 1f);
        baseColors[3] = new Color(0.5f, 0.5f, 0f, 1f);

        highlightColors[0] = Color.RED;
        highlightColors[1] = Color.GREEN;
        highlightColors[2] = Color.BLUE;
        highlightColors[3] = Color.YELLOW;

        reset();
    }

    @Override
    public void reset() {
        this.currentRound = 1;
        this.isSolved = false;
        this.showIntro = true;
        this.highlightButton = -1;
        this.highlightTimer = 0f;
        this.playbackIndex = 0;
        this.playbackTimer = 0f;
        this.inputIndex = 0;
        this.soundPlayedForStep = false;
        this.playbackDelayTimer = 1.0f; // Pause before sequence starts playing to avoid overlapping cutoff

        // Regenerate sequence
        for (int i = 0; i < MAX_ROUNDS; i++) {
            sequence[i] = MathUtils.random(0, 3);
            playerInput[i] = -1;
        }

        this.state = SimonState.SHOWING_SEQUENCE;
    }

    @Override
    public void update(final float delta) {
        if (isSolved) {
            return;
        }

        if (checkIntroClick()) {
            return;
        }
        if (showIntro) {
            return;
        }

        // Handle visual highlights timer
        if (highlightTimer > 0) {
            highlightTimer -= delta;
            if (highlightTimer <= 0) {
                highlightButton = -1;
            }
        }

        // Process states
        switch (state) {
        case SHOWING_SEQUENCE:
            updatePlayback(delta);
            break;
        case AWAITING_INPUT:
            updateInputDetection();
            break;
        case EVALUATING:
            // Brief pause state during evaluation if needed
            break;
        case ROUND_WON:
            stateTimer -= delta;
            if (stateTimer <= 0) {
                currentRound++;
                if (currentRound > MAX_ROUNDS) {
                    isSolved = true;
                    state = SimonState.GAME_WON;
                } else {
                    playbackIndex = 0;
                    playbackTimer = 0f;
                    inputIndex = 0;
                    soundPlayedForStep = false;
                    playbackDelayTimer = 1.0f; // Pause before sequence starts playing to avoid overlapping cutoff
                    state = SimonState.SHOWING_SEQUENCE;
                }
            }
            break;
        case FAILED_DELAY:
            stateTimer -= delta;
            if (stateTimer <= 0) {
                reset();
            }
            break;
        default:
            break;
        }
    }

    private void updatePlayback(final float delta) {
        if (playbackDelayTimer > 0) {
            playbackDelayTimer -= delta;
            highlightButton = -1;
            return;
        }

        final float stepDuration = getStepDuration();
        final float litDuration = getLitDuration();

        playbackTimer += delta;

        // Light up button and play sound at step start
        if (playbackTimer < litDuration) {
            highlightButton = sequence[playbackIndex];
            if (!soundPlayedForStep) {
                playTone(highlightButton);
                soundPlayedForStep = true;
            }
        } else {
            // Turn off button for the dim duration
            highlightButton = -1;
        }

        if (playbackTimer >= stepDuration) {
            playbackTimer = 0f;
            soundPlayedForStep = false;
            playbackIndex++;
            if (playbackIndex >= currentRound) {
                state = SimonState.AWAITING_INPUT;
            }
        }
    }

    private void updateInputDetection() {
        if (Gdx.input.justTouched() && context != null) {
            tmpMouse.set(Gdx.input.getX(), Gdx.input.getY());
            context.unproject(tmpMouse);
            final float mx = tmpMouse.x;
            final float my = tmpMouse.y;

            for (int i = 0; i < 4; i++) {
                if (buttons[i].contains(mx, my)) {
                    // Flash button
                    highlightButton = i;
                    highlightTimer = 0.25f;
                    playTone(i);

                    // Check correctness
                    playerInput[inputIndex] = i;
                    if (playerInput[inputIndex] != sequence[inputIndex]) {
                        // Wrong answer -> dispatch sfx, wait briefly, reset puzzle
                        EventDispatcher.getInstance().playSfx(AssetPaths.SFX_PUZZLE_WRONG);
                        state = SimonState.FAILED_DELAY;
                        stateTimer = 1.5f;
                    } else {
                        inputIndex++;
                        if (inputIndex >= currentRound) {
                            state = SimonState.ROUND_WON;
                            stateTimer = 0.8f; // Pause briefly
                        }
                    }
                    break;
                }
            }
        }
    }

    private void playTone(final int buttonIndex) {
        if (buttonIndex >= 0 && buttonIndex < 4) {
            EventDispatcher.getInstance().playSfx(tones[buttonIndex]);
        }
    }

    private float getStepDuration() {
        if (currentRound <= 3)
            return 0.8f;
        if (currentRound <= 6)
            return 0.6f;
        if (currentRound <= 9)
            return 0.4f;
        return 0.25f;
    }

    private float getLitDuration() {
        if (currentRound <= 3)
            return 0.5f;
        if (currentRound <= 6)
            return 0.4f;
        if (currentRound <= 9)
            return 0.28f;
        return 0.18f;
    }


    @Override
    public void render(final ShapeRenderer shapeRenderer, final SpriteBatch batch) {
        if (isSolved) {
            return;
        }

        if (showIntro) {
            drawIntro(shapeRenderer, batch);
            return;
        }

        // Draw slots first to create a sunken board visual slot around keys
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(slotColor);
        for (int i = 0; i < 4; i++) {
            final Rectangle rect = buttons[i];
            shapeRenderer.rect(rect.x - SLOT_OFFSET, rect.y - SLOT_OFFSET, rect.width + SLOT_OFFSET * 2,
                    rect.height + SLOT_OFFSET * 2);
        }

        // Draw buttons inside slots
        for (int i = 0; i < 4; i++) {
            if (highlightButton == i) {
                shapeRenderer.setColor(highlightColors[i]);
            } else {
                shapeRenderer.setColor(baseColors[i]);
            }
            final Rectangle rect = buttons[i];
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.end();

        // Draw bevel overlays for 3D raised key look
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 4; i++) {
            final Rectangle rect = buttons[i];

            // Top bevel (light)
            shapeRenderer.setColor(bevelLight);
            shapeRenderer.rect(rect.x, rect.y + rect.height - BEVEL_T, rect.width, BEVEL_T);

            // Left bevel (light)
            shapeRenderer.rect(rect.x, rect.y, BEVEL_T, rect.height);

            // Bottom bevel (dark)
            shapeRenderer.setColor(bevelDark);
            shapeRenderer.rect(rect.x, rect.y, rect.width, BEVEL_T);

            // Right bevel (dark)
            shapeRenderer.rect(rect.x + rect.width - BEVEL_T, rect.y, BEVEL_T, rect.height);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw text
        batch.begin();
        final BitmapFont font = context.getFont();
        font.setColor(Color.WHITE);

        textBuilder.setLength(0);
        textBuilder.append("Simon Game - Vòng: ").append(currentRound).append(" / ").append(MAX_ROUNDS);

        if (textBoxTexture != null) {
            drawTextInBox(batch, font, textBuilder, 400f, 530f, textBoxTexture, textLayout);
        } else {
            font.draw(batch, textBuilder, 260f, 520f);
        }

        textBuilder.setLength(0);
        if (state == SimonState.SHOWING_SEQUENCE) {
            textBuilder.append("Ghi nhớ dãy nút...");
        } else if (state == SimonState.AWAITING_INPUT) {
            textBuilder.append("Hãy lặp lại dãy nút!");
        } else if (state == SimonState.FAILED_DELAY) {
            textBuilder.append("Sai rồi! Chuẩn bị thử lại...");
        } else {
            textBuilder.append("Đang kiểm tra...");
        }

        if (textBoxTexture != null) {
            drawTextInBox(batch, font, textBuilder, 400f, 80f, textBoxTexture, textLayout);
        } else {
            font.draw(batch, textBuilder, 290f, 80f);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        // No custom fonts/textures instantiated locally, using shared context components
    }

    private void drawIntro(final ShapeRenderer shapeRenderer, final SpriteBatch batch) {
        final String introText = "Chào mừng bạn đến với thử thách đầu tiên!\n\n"
                + "Luật chơi Simon Game rất đơn giản:\n"
                + "1. Hệ thống sẽ phát một chuỗi các ô màu sáng kèm âm thanh.\n"
                + "2. Hãy ghi nhớ và bấm lại đúng thứ tự các ô màu đó.\n" + "3. Vượt qua đủ " + MAX_ROUNDS
                + " vòng để hoàn thành thử thách.\n\n" + "Chú ý: Bấm sai bất kỳ ô nào sẽ phải chơi lại từ đầu!";

        drawIntroModal(shapeRenderer, batch, "THỬ THÁCH 1: SIMON GAME", introText);
    }

    public String[] getTones() {
        return tones;
    }
}
