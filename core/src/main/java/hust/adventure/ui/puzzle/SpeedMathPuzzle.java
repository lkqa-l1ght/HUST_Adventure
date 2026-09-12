package hust.adventure.ui.puzzle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;
import hust.adventure.screens.PlayScreen;

/**
 * Speed Math (Addition Only) mini-game puzzle.
 */
public class SpeedMathPuzzle extends BasePuzzleGame {
    private static final int MAX_ROUNDS = 7;
    private static final float FEEDBACK_DURATION = 0.5f;

    public enum MathState {
        AWAITING_INPUT, CORRECT_FEEDBACK, WRONG_FEEDBACK
    }

    private MathState state;

    private int currentRound;
    private float timeRemaining;
    private final float[] roundTimeLimits = new float[MAX_ROUNDS];

    // Math operands
    private int operandA;
    private int operandB;
    private int correctAnswer;

    // Feedback
    private float feedbackTimer;
    private boolean isTimeout;

    // Buffers for zero-allocation
    private final StringBuilder answerBuilder;
    private final StringBuilder renderBuilder;

    public SpeedMathPuzzle() {
        this.state = MathState.AWAITING_INPUT;
        this.isSolved = false;
        this.currentRound = 1;
        this.answerBuilder = new StringBuilder();
        this.renderBuilder = new StringBuilder();
        this.showIntro = true;
    }

    @Override
    public void init(final PlayScreen ctx) {
        super.init(ctx);

        // Compute round time limits
        // Rounds 1 to MAX_ROUNDS: linearly from 20.0s to 10.0s
        for (int i = 0; i < MAX_ROUNDS; i++) {
            roundTimeLimits[i] = 20.0f - (i * (20.0f - 10.0f) / (MAX_ROUNDS - 1f));
        }

        reset();
    }

    @Override
    public void reset() {
        this.currentRound = 1;
        this.isSolved = false;
        this.showIntro = true;
        this.feedbackTimer = 0f;
        this.isTimeout = false;
        this.answerBuilder.setLength(0);
        this.state = MathState.AWAITING_INPUT;
        generateQuestion();
    }

    void generateQuestion() {
        operandA = MathUtils.random(10, 99);
        operandB = MathUtils.random(10, 99);
        correctAnswer = operandA + operandB;
        timeRemaining = roundTimeLimits[currentRound - 1];
    }

    void setCurrentRound(final int round) {
        this.currentRound = round;
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

        if (state == MathState.AWAITING_INPUT) {
            timeRemaining -= delta;
            if (timeRemaining <= 0) {
                isTimeout = true;
                playWrongSound();
                state = MathState.WRONG_FEEDBACK;
                feedbackTimer = FEEDBACK_DURATION;
                return;
            }

            handleKeyboardInput();
        } else {
            // Processing feedback state timers
            feedbackTimer -= delta;
            if (feedbackTimer <= 0) {
                if (state == MathState.CORRECT_FEEDBACK) {
                    if (currentRound == MAX_ROUNDS) {
                        isSolved = true;
                    } else {
                        currentRound++;
                        answerBuilder.setLength(0);
                        generateQuestion();
                        state = MathState.AWAITING_INPUT;
                    }
                } else if (state == MathState.WRONG_FEEDBACK) {
                    reset();
                }
            }
        }
    }

    private void handleKeyboardInput() {
        // Handle numeric digit keys (NUM_0 to NUM_9, NUMPAD_0 to NUMPAD_9)
        for (int key = Input.Keys.NUM_0; key <= Input.Keys.NUM_9; key++) {
            if (Gdx.input.isKeyJustPressed(key)) {
                appendDigit((char) ('0' + (key - Input.Keys.NUM_0)));
            }
        }
        for (int key = Input.Keys.NUMPAD_0; key <= Input.Keys.NUMPAD_9; key++) {
            if (Gdx.input.isKeyJustPressed(key)) {
                appendDigit((char) ('0' + (key - Input.Keys.NUMPAD_0)));
            }
        }

        // Handle Backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (answerBuilder.length() > 0) {
                answerBuilder.setLength(answerBuilder.length() - 1);
            }
        }

        // Handle Enter key to submit
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            if (answerBuilder.length() > 0) {
                evaluateAnswer();
            }
        }
    }

    private void appendDigit(final char digit) {
        if (answerBuilder.length() < 3) { // 99+99 = 198 (3 digits max)
            answerBuilder.append(digit);
        }
    }

    private void evaluateAnswer() {
        // Custom zero-allocation integer parser
        int parsedValue = 0;
        for (int i = 0; i < answerBuilder.length(); i++) {
            parsedValue = parsedValue * 10 + (answerBuilder.charAt(i) - '0');
        }

        if (parsedValue == correctAnswer) {
            playCorrectSound();
            state = MathState.CORRECT_FEEDBACK;
            feedbackTimer = FEEDBACK_DURATION;
        } else {
            playWrongSound();
            isTimeout = false;
            state = MathState.WRONG_FEEDBACK;
            feedbackTimer = FEEDBACK_DURATION;
        }
    }

    private void playCorrectSound() {
        EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_CORRECT);
    }

    private void playWrongSound() {
        EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_WRONG);
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

        // Draw timer bar background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        final float maxTime = roundTimeLimits[currentRound - 1];
        if (timeRemaining > maxTime * 0.5f) {
            shapeRenderer.setColor(Color.GREEN);
        } else if (timeRemaining > maxTime * 0.2f) {
            shapeRenderer.setColor(Color.YELLOW);
        } else {
            shapeRenderer.setColor(Color.RED);
        }
        final float timerWidth = 800f * (timeRemaining / maxTime);
        shapeRenderer.rect(0f, 590f, timerWidth, 10f);
        shapeRenderer.end();

        // Render question text centered
        batch.begin();
        final BitmapFont font = context.getFont();
        font.setColor(Color.WHITE);

        // Progress Text
        renderBuilder.setLength(0);
        renderBuilder.append("Toán Nhanh - Câu Hỏi: ").append(currentRound).append(" / ").append(MAX_ROUNDS);
        if (textBoxTexture != null) {
            drawTextInBox(batch, font, renderBuilder, 400f, 530f, textBoxTexture, textLayout);
        } else {
            font.draw(batch, renderBuilder, 20f, 570f);
        }

        // Expression
        renderBuilder.setLength(0);
        renderBuilder.append(operandA).append(" + ").append(operandB).append(" = ?");
        if (textBoxTexture != null) {
            drawTextInBox(batch, font, renderBuilder, 400f, 340f, textBoxTexture, textLayout);
        } else {
            font.draw(batch, renderBuilder, 330f, 340f);
        }

        // Answer
        renderBuilder.setLength(0);
        renderBuilder.append("Đáp án: ").append(answerBuilder);
        if (textBoxTexture != null) {
            drawTextInBox(batch, font, renderBuilder, 400f, 260f, textBoxTexture, textLayout);
        } else {
            font.draw(batch, renderBuilder, 330f, 280f);
        }

        // Feedback overlay
        if (state == MathState.CORRECT_FEEDBACK) {
            font.setColor(Color.GREEN);
            renderBuilder.setLength(0);
            renderBuilder.append("Chính Xác!");
            if (textBoxTexture != null) {
                drawTextInBox(batch, font, renderBuilder, 400f, 170f, textBoxTexture, textLayout);
            } else {
                font.draw(batch, renderBuilder, 350f, 200f);
            }
        } else if (state == MathState.WRONG_FEEDBACK) {
            font.setColor(Color.RED);
            renderBuilder.setLength(0);
            if (isTimeout) {
                renderBuilder.append("Hết Giờ!");
            } else {
                renderBuilder.append("Sai Rồi!");
            }
            if (textBoxTexture != null) {
                drawTextInBox(batch, font, renderBuilder, 400f, 170f, textBoxTexture, textLayout);
            } else {
                font.draw(batch, renderBuilder, 360f, 200f);
            }
        }

        font.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        // No custom fonts/textures instantiated locally, using shared context components
    }

    private void drawIntro(final ShapeRenderer shapeRenderer, final SpriteBatch batch) {
        final String introText = "Chào mừng bạn đến với thử thách cuối cùng!\n\n"
                + "Luật chơi Tính Nhẩm rất đơn giản:\n" + "1. Hệ thống sẽ đưa ra các phép toán cộng ngẫu nhiên.\n"
                + "2. Nhập đáp án bằng các phím số từ bàn phím của bạn.\n"
                + "3. Nhấn [Enter] để gửi đáp án, hoặc [Backspace] để xóa.\n" + "4. Vượt qua đúng " + MAX_ROUNDS
                + " câu hỏi để hoàn thành thử thách.\n"
                + "5. Thời gian giới hạn cho mỗi câu sẽ ngắn dần theo từng vòng!\n\n"
                + "Hãy nhấn nút bên dưới để bắt đầu tính nhẩm!";

        drawIntroModal(shapeRenderer, batch, "THỬ THÁCH 3: TÍNH NHẨM NHANH", introText);
    }

    public float[] getRoundTimeLimits() {
        return roundTimeLimits;
    }

    public int getOperandA() {
        return operandA;
    }

    public int getOperandB() {
        return operandB;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }
}
