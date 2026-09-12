package hust.adventure.screens.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import hust.adventure.core.assets.AssetPaths;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.screens.LoadingScreen;
import hust.adventure.screens.PlayScreen;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.progression.MapDirector;

import java.util.ArrayList;
import java.util.List;

/**
 * Behavior class for the final Boss Fight level, managing dialogues, Q&A, and typing phases.
 */
public class BossFightBehavior implements LevelBehavior {
    // Phase constants
    private static final int PHASE_CUTSCENE = 0;
    private static final int PHASE_QA = 1;
    private static final int PHASE_DODGE = 2;
    private static final int PHASE_FINAL = 3;
    private static final int PHASE_VICTORY = 4;

    // Boss fight parameter constants
    private static final float QUESTION_TIMER_RESET = 5f;
    private static final float ANSWER_HEAL = 10f;
    private static final float WRONG_ANSWER_DAMAGE = 25f;
    private static final float TIME_OUT_DAMAGE = 25f;
    private static final float FINAL_PHASE_WRONG_DAMAGE = 30f;
    private static final float PAPER_DODGE_DAMAGE = 10f;

    private static final String[] PAPER_TEXTS = { "SAI", "CHINH LAI", "THIEU REF" };

    private Enemy finalBoss;
    private int phase = PHASE_CUTSCENE;

    private final String[] dialogue = { "...Em da den.", "Ta nghe noi em da vuot qua thu vien... va phong lab.",
            "Bay gio... hay bao ve do an cua em." };
    private int dialogueIndex = 0;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private Rectangle[] answerRects;
    private float answerTimer = QUESTION_TIMER_RESET;

    // Phase 2 Dodge
    private List<FallingPaper> fallingPapers;

    // Background texture
    private Texture bgTexture;

    private Stage stage;
    private TextField textField;
    private boolean typingPhase = false;
    private boolean victory = false;
    private float victoryTimer = 0f;

    // Screen shake
    private float shakeTimer = 0f;
    private static final float SHAKE_DURATION = 0.5f;
    private static final float SHAKE_MAGNITUDE = 7f;

    static class Question {
        final String text;
        final String[] answers;
        final int correctIndex;

        Question(final String t, final String[] a, final int c) {
            this.text = t;
            this.answers = a;
            this.correctIndex = c;
        }
    }

    static class FallingPaper {
        final Rectangle rect;
        final String text;

        FallingPaper(final Rectangle r, final String t) {
            this.rect = r;
            this.text = t;
        }
    }

    @Override
    public void init(final PlayScreen context) {
        finalBoss = (Enemy) context.getEntityFactory().createEnemy("final_boss", 370, 450);

        bgTexture = context.getGame().getAssetManager().getTexture("map/Boss Room.jpg");

        initBossContent();
        initUI(context);
    }

    private void initBossContent() {
        questions = new ArrayList<>();
        questions.add(new Question("Tai sao em chon thuat toan nay?",
                new String[] { "Em thay tren mang", "Em copy ban", "Do phuc tap phu hop" }, 2));
        questions.add(new Question("Dataset cua em co bao nhieu records?",
                new String[] { "Nhieu", "Chua dem", "10,847 records" }, 2));
        questions.add(new Question("Code O(n^2) - tai sao khong dung O(n log n)?",
                new String[] { "Khong biet", "Vi dataset nho", "Vi nhin quen hon" }, 1));
        questions.add(new Question("He thong deploy o dau?",
                new String[] { "Localhost", "Cloud voi CI/CD", "May ban em" }, 1));

        answerRects = new Rectangle[3];
        answerRects[0] = new Rectangle(100, 200, 150, 40);
        answerRects[1] = new Rectangle(325, 200, 150, 40);
        answerRects[2] = new Rectangle(550, 200, 150, 40);

        fallingPapers = new ArrayList<>();
    }

    private void initUI(final PlayScreen context) {
        stage = new Stage(new FitViewport(800, 600));
        final Skin skin = new Skin();
        final Pixmap pixmap = new Pixmap(100, 30, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("default", context.getFont());

        final TextField.TextFieldStyle tfs = new TextField.TextFieldStyle();
        tfs.font = skin.getFont("default");
        tfs.fontColor = Color.BLACK;
        tfs.background = skin.newDrawable("white", Color.WHITE);
        tfs.cursor = skin.newDrawable("white", Color.BLACK);

        textField = new TextField("", tfs);
        textField.setPosition(300, 200);
        textField.setSize(200, 40);
        textField.setVisible(false);
        stage.addActor(textField);
    }

    @Override
    public void update(final PlayScreen context, final float delta) {
        // Cập nhật shake timer
        if (shakeTimer > 0) {
            shakeTimer -= delta;
        }

        float dt = delta;
        if (context.getProgressContext().isHasNao() && context.getInputReader().isQJustPressed()) {
            dt *= 0.3f;
        }

        switch (phase) {
        case PHASE_CUTSCENE:
            updateCutscene(context);
            break;
        case PHASE_QA:
        case PHASE_DODGE:
            updateBattle(context, dt);
            break;
        case PHASE_FINAL:
            updateFinalPhase(context, delta);
            break;
        case PHASE_VICTORY:
            victoryTimer += delta;
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                context.getGame().setScreen(new LoadingScreen(context.getGame()));
            }
            break;
        }
    }

    private void updateCutscene(final PlayScreen context) {
        if (context.getInputReader().isEnterJustPressed()) {
            dialogueIndex++;
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_DIALOGUE_NEXT);
            if (dialogueIndex >= dialogue.length) {
                phase = PHASE_QA;
            }
        }
    }

    private void updateBattle(final PlayScreen context, float dt) {
        if (currentQuestionIndex < questions.size()) {
            answerTimer -= dt;
            if (phase == PHASE_DODGE) {
                updateDodge(context, dt);
            }

            if (answerTimer <= 0) {
                context.getPlayer().takeDamage(TIME_OUT_DAMAGE);
                shakeTimer = SHAKE_DURATION;
                EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_WRONG);
                nextQuestion();
            } else if (context.getInputReader().isSpaceJustPressed()) {
                handleAnswerInput(context);
            }
        }
    }

    private void updateDodge(final PlayScreen context, float dt) {
        // Spawn falling papers
        if (MathUtils.random() < 2f * dt) {
            final float startX = MathUtils.random(100f, 700f);
            final String text = PAPER_TEXTS[MathUtils.random(PAPER_TEXTS.length - 1)];
            fallingPapers.add(new FallingPaper(new Rectangle(startX, 600f, 80f, 30f), text));
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_PAPER_SPAWN);
        }

        // Check collision with player
        final Rectangle playerBounds = new Rectangle(context.getPlayer().getX() - 25f, context.getPlayer().getY() - 25f,
                50f, 50f);

        for (int i = fallingPapers.size() - 1; i >= 0; i--) {
            final FallingPaper paper = fallingPapers.get(i);
            paper.rect.y -= 150f * dt;
            if (paper.rect.overlaps(playerBounds)) {
                context.getPlayer().takeDamage(PAPER_DODGE_DAMAGE);
                fallingPapers.remove(i);
            } else if (paper.rect.y < 0) {
                fallingPapers.remove(i);
            }
        }
    }

    private void handleAnswerInput(final PlayScreen context) {
        for (int i = 0; i < 3; i++) {
            if (answerRects[i].contains(context.getPlayer().getX(), context.getPlayer().getY())) {
                if (i == questions.get(currentQuestionIndex).correctIndex) {
                    context.getPlayer().heal(ANSWER_HEAL);
                    EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_CORRECT);
                } else {
                    context.getPlayer().takeDamage(WRONG_ANSWER_DAMAGE);
                    shakeTimer = SHAKE_DURATION;
                    EventDispatcher.getInstance().playSfx(AssetPaths.SFX_ANSWER_WRONG);
                }
                nextQuestion();
                break;
            }
        }
    }

    private void nextQuestion() {
        currentQuestionIndex++;
        answerTimer = QUESTION_TIMER_RESET;
        checkPhase();
    }

    private void updateFinalPhase(final PlayScreen context, float delta) {
        if (!typingPhase) {
            typingPhase = true;
            Gdx.input.setInputProcessor(stage);
            textField.setVisible(true);
            stage.setKeyboardFocus(textField);
        } else {
            stage.act(delta);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                final String answer = textField.getText().trim().toUpperCase();
                if (answer.equals("PASS") || answer.equals("GRADUATE")) {
                    if (finalBoss != null) {
                        finalBoss.takeDamage(finalBoss.getHp());
                    }
                    onBossDefeated(context);
                } else {
                    context.getPlayer().takeDamage(FINAL_PHASE_WRONG_DAMAGE);
                    shakeTimer = SHAKE_DURATION;
                    EventDispatcher.getInstance().playSfx(AssetPaths.SFX_PUZZLE_WRONG);
                    textField.setText("");
                }
            }
        }
    }

    private void checkPhase() {
        if (currentQuestionIndex >= 2 && phase == PHASE_QA) {
            phase = PHASE_DODGE;
        }
        if (currentQuestionIndex >= questions.size()) {
            phase = PHASE_FINAL;
        }
    }

    @Override
    public void draw(final PlayScreen context) {
        // ── Camera shake ──────────────────────────────────────────────────
        float shakeX = 0f, shakeY = 0f;
        if (shakeTimer > 0) {
            float intensity = shakeTimer / SHAKE_DURATION;
            shakeX = MathUtils.random(-SHAKE_MAGNITUDE * intensity, SHAKE_MAGNITUDE * intensity);
            shakeY = MathUtils.random(-SHAKE_MAGNITUDE * intensity, SHAKE_MAGNITUDE * intensity);
            context.getCamera().position.add(shakeX, shakeY, 0);
            context.getCamera().update();
        }

        switch (phase) {
        case PHASE_CUTSCENE:
            drawCutscene(context);
            break;
        case PHASE_QA:
        case PHASE_DODGE:
            drawBattle(context);
            break;
        case PHASE_FINAL:
            drawFinalPhase(context);
            break;
        case PHASE_VICTORY:
            drawVictory(context);
            break;
        }

        // Kh\u00f4i ph\u1ee5c camera sau khi draw xong
        restoreCamera(context, shakeX, shakeY);
    }

    private void drawCutscene(final PlayScreen context) {
        final float BOX_X = 80f, BOX_Y = 40f, BOX_W = 640f, BOX_H = 110f;

        context.getShapeRenderer().setProjectionMatrix(context.getCamera().combined);
        context.getShapeRenderer().begin(ShapeType.Filled);

        // Panel nền dark academic
        context.getShapeRenderer().setColor(0.04f, 0.03f, 0.10f, 0.95f);
        context.getShapeRenderer().rect(BOX_X, BOX_Y, BOX_W, BOX_H);

        // Header accent bar
        context.getShapeRenderer().setColor(0.55f, 0.15f, 0.05f, 1f);
        context.getShapeRenderer().rect(BOX_X, BOX_Y + BOX_H - 24f, BOX_W, 24f);
        context.getShapeRenderer().end();

        // Border
        context.getShapeRenderer().begin(ShapeType.Line);
        context.getShapeRenderer().setColor(0.85f, 0.35f, 0.1f, 1f);
        context.getShapeRenderer().rect(BOX_X, BOX_Y, BOX_W, BOX_H);
        context.getShapeRenderer().setColor(0.4f, 0.15f, 0.05f, 0.5f);
        context.getShapeRenderer().rect(BOX_X + 2, BOX_Y + 2, BOX_W - 4, BOX_H - 4);
        context.getShapeRenderer().end();

        context.getGame().getSpriteBatch().setProjectionMatrix(context.getCamera().combined);
        context.getGame().getSpriteBatch().begin();
        // Speaker name
        context.getGame().getFont().setColor(1f, 0.65f, 0.2f, 1f);
        context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "Tạ Hải Tùng", BOX_X + 10f,
                BOX_Y + BOX_H - 6f);
        // Dialogue
        if (dialogueIndex < dialogue.length) {
            context.getGame().getFont().setColor(Color.WHITE);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "\"" + dialogue[dialogueIndex] + "\"",
                    BOX_X + 14f, BOX_Y + BOX_H - 34f);
            // Blink hint
            float blink = (System.currentTimeMillis() / 500) % 2 == 0 ? 1f : 0.3f;
            context.getGame().getFont().setColor(0.7f, 0.7f, 0.7f, blink);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "▶ Nhấn ENTER để tiếp tục",
                    BOX_X + BOX_W - 230f, BOX_Y + 18f);
        }
        context.getGame().getSpriteBatch().end();
    }

    private void drawBattle(final PlayScreen context) {
        if (currentQuestionIndex < questions.size()) {
            final Question q = questions.get(currentQuestionIndex);

            final float QX = 70f, QY = 310f, QW = 660f, QH = 55f;
            final float[] ANS_X = { 80f, 300f, 520f };
            final float ANS_Y = 220f, ANS_W = 195f, ANS_H = 55f;
            final String[] LABELS = { "A", "B", "C" };

            context.getShapeRenderer().setProjectionMatrix(context.getCamera().combined);
            context.getShapeRenderer().begin(ShapeType.Filled);

            // ── Question panel ─────────────────────────────────────────────
            context.getShapeRenderer().setColor(0.05f, 0.04f, 0.13f, 0.96f);
            context.getShapeRenderer().rect(QX, QY, QW, QH);
            // Left accent stripe
            context.getShapeRenderer().setColor(0.2f, 0.6f, 1.0f, 1f);
            context.getShapeRenderer().rect(QX, QY, 5f, QH);

            // ── Answer slots ───────────────────────────────────────────────
            for (int i = 0; i < 3; i++) {
                boolean playerInside = answerRects[i].contains(context.getPlayer().getX(), context.getPlayer().getY());
                if (playerInside) {
                    context.getShapeRenderer().setColor(0.25f, 0.55f, 0.95f, 0.95f);
                } else {
                    context.getShapeRenderer().setColor(0.08f, 0.07f, 0.20f, 0.92f);
                }
                context.getShapeRenderer().rect(ANS_X[i], ANS_Y, ANS_W, ANS_H);

                // Letter badge background
                context.getShapeRenderer().setColor(0.2f, 0.4f, 0.85f, 1f);
                context.getShapeRenderer().rect(ANS_X[i], ANS_Y + ANS_H - 20f, 20f, 20f);
            }

            // ── Timer bar ──────────────────────────────────────────────────
            float timerPct = answerTimer / QUESTION_TIMER_RESET;
            // Background
            context.getShapeRenderer().setColor(0.1f, 0.05f, 0.05f, 1f);
            context.getShapeRenderer().rect(QX, QY - 10f, QW, 7f);
            // Fill gradient green→red
            int tSteps = (int) (QW * timerPct);
            for (int i = 0; i < tSteps; i++) {
                float t = 1f - (i / (float) tSteps);
                context.getShapeRenderer().setColor(t, 1f - t * 0.8f, 0f, 1f);
                context.getShapeRenderer().rect(QX + i, QY - 10f, 1f, 7f);
            }

            context.getShapeRenderer().end();

            // Borders
            context.getShapeRenderer().begin(ShapeType.Line);
            context.getShapeRenderer().setColor(0.3f, 0.6f, 1.0f, 0.8f);
            context.getShapeRenderer().rect(QX, QY, QW, QH);
            for (int i = 0; i < 3; i++) {
                boolean playerInside = answerRects[i].contains(context.getPlayer().getX(), context.getPlayer().getY());
                context.getShapeRenderer().setColor(playerInside ? Color.WHITE : new Color(0.3f, 0.5f, 0.9f, 0.7f));
                context.getShapeRenderer().rect(ANS_X[i], ANS_Y, ANS_W, ANS_H);
            }
            context.getShapeRenderer().end();

            // ── Text ───────────────────────────────────────────────────────
            context.getGame().getSpriteBatch().setProjectionMatrix(context.getCamera().combined);
            context.getGame().getSpriteBatch().begin();

            // Question number
            context.getGame().getFont().setColor(0.5f, 0.8f, 1.0f, 1f);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(),
                    "Câu " + (currentQuestionIndex + 1) + "/" + questions.size(), QX + 10f, QY + QH - 4f);

            // Question text
            context.getGame().getFont().setColor(Color.WHITE);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(), q.text, QX + 90f, QY + QH - 4f);

            // Answer texts + letter badge
            for (int i = 0; i < q.answers.length; i++) {
                // Badge letter
                context.getGame().getFont().setColor(Color.WHITE);
                context.getGame().getFont().draw(context.getGame().getSpriteBatch(), LABELS[i], ANS_X[i] + 4f,
                        ANS_Y + ANS_H - 4f);
                // Answer text
                context.getGame().getFont().setColor(0.9f, 0.9f, 1.0f, 1f);
                context.getGame().getFont().draw(context.getGame().getSpriteBatch(), q.answers[i], ANS_X[i] + 8f,
                        ANS_Y + ANS_H - 24f);
            }

            // Hint
            context.getGame().getFont().setColor(0.5f, 0.6f, 0.8f, 1f);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "Di chuyển vào ô đáp án → nhấn SPACE",
                    220f, 175f);

            context.getGame().getFont().setColor(Color.WHITE);
            context.getGame().getSpriteBatch().end();
        }

        // ── Falling papers (Phase DODGE) ────────────────────────────────────
        if (phase == PHASE_DODGE) {
            context.getShapeRenderer().setProjectionMatrix(context.getCamera().combined);
            context.getShapeRenderer().begin(ShapeType.Filled);
            for (final FallingPaper paper : fallingPapers) {
                // Paper body
                context.getShapeRenderer().setColor(0.95f, 0.95f, 0.88f, 1f);
                context.getShapeRenderer().rect(paper.rect.x, paper.rect.y, paper.rect.width, paper.rect.height);
                // Red top strip
                context.getShapeRenderer().setColor(0.9f, 0.1f, 0.1f, 1f);
                context.getShapeRenderer().rect(paper.rect.x, paper.rect.y + paper.rect.height - 7f, paper.rect.width,
                        7f);
            }
            context.getShapeRenderer().end();

            context.getShapeRenderer().begin(ShapeType.Line);
            context.getShapeRenderer().setColor(0.7f, 0.1f, 0.1f, 0.8f);
            for (final FallingPaper paper : fallingPapers) {
                context.getShapeRenderer().rect(paper.rect.x, paper.rect.y, paper.rect.width, paper.rect.height);
            }
            context.getShapeRenderer().end();

            context.getGame().getSpriteBatch().setProjectionMatrix(context.getCamera().combined);
            context.getGame().getSpriteBatch().begin();
            context.getGame().getFont().setColor(0.8f, 0.05f, 0.05f, 1f);
            for (final FallingPaper paper : fallingPapers) {
                context.getGame().getFont().draw(context.getGame().getSpriteBatch(), paper.text, paper.rect.x + 5f,
                        paper.rect.y + 20f);
            }
            context.getGame().getFont().setColor(Color.WHITE);
            context.getGame().getSpriteBatch().end();
        }
    }

    private void drawFinalPhase(final PlayScreen context) {
        // Draw code puzzle box background
        context.getShapeRenderer().setProjectionMatrix(context.getCamera().combined);
        context.getShapeRenderer().begin(ShapeType.Filled);
        context.getShapeRenderer().setColor(0f, 0f, 0f, 0.8f);
        context.getShapeRenderer().rect(200f, 200f, 400f, 150f);
        context.getShapeRenderer().end();

        // Draw code puzzle texts
        context.getGame().getSpriteBatch().setProjectionMatrix(context.getCamera().combined);
        context.getGame().getSpriteBatch().begin();
        context.getGame().getFont().setColor(Color.WHITE);
        context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "if (codeWorks && studentUnderstands) {",
                220f, 330f);
        context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "    return ???;", 220f, 300f);
        context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "}", 220f, 270f);
        context.getGame().getSpriteBatch().end();

        stage.draw();
    }

    private void drawVictory(final PlayScreen context) {
        // White screen victory fade-in
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        context.getShapeRenderer().setProjectionMatrix(context.getCamera().combined);
        context.getShapeRenderer().begin(ShapeType.Filled);
        context.getShapeRenderer().setColor(new Color(1f, 1f, 1f, Math.min(1f, victoryTimer / 2f)));
        context.getShapeRenderer().rect(0f, 0f, 800f, 600f);
        context.getShapeRenderer().end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        context.getGame().getSpriteBatch().setProjectionMatrix(context.getCamera().combined);
        context.getGame().getSpriteBatch().begin();

        if (victoryTimer > 3f) {
            context.getGame().getFont().setColor(Color.BLACK);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "✅ ASSIGNMENT SUBMITTED SUCCESSFULLY",
                    250f, 400f);
            if (victoryTimer > 5f) {
                context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "Chuc mung em da qua mon - THT",
                        280f, 200f + (victoryTimer - 5f) * 50f);
            }
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(), "[Nhấn Esc để về Menu]", 300f, 100f);
        } else {
            // Classic Vietnamese victory message during fade-in
            context.getGame().getFont().setColor(Color.YELLOW);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(),
                    "CHIẾN THẮNG!!! CHÚC MỪNG BẠN ĐÃ TỐT NGHIỆP!", 100f, 300f);
            context.getGame().getFont().draw(context.getGame().getSpriteBatch(),
                    "Thời gian kết thúc: " + (int) victoryTimer + "s", 100f, 250f);
        }
        context.getGame().getSpriteBatch().end();
    }

    /**
     * Handles boss defeat, unlocking graduation and triggering victory sequences.
     *
     * @param context the level context providing subsystems
     */
    public void onBossDefeated(final PlayScreen context) {
        victory = true;
        phase = PHASE_VICTORY;
        if (textField != null) {
            textField.setVisible(false);
        }
        if (context != null && context.getInputReader() instanceof InputProcessor) {
            Gdx.input.setInputProcessor((InputProcessor) context.getInputReader());
        }

        final GameProgressContext progress = context != null ? context.getProgressContext() : null;
        if (progress != null && progress.getMapDirector() != null) {
            progress.getMapDirector().onBossDefeated();
        }
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.GRADUATION, null));
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.PLAY_BGM, "audio/music/win_menu.mp3"));
    }

    @Override
    public boolean canTransition(final PlayScreen context) {
        return LevelBehavior.super.canTransition(context) || victory;
    }

    @Override
    public boolean isAutoAttackAllowed() {
        return false;
    }

    @Override
    public void dispose(final PlayScreen context) {
        if (stage != null) {
            stage.dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    /** Gọi sau khi draw() xong để khôi phục camera về vị trí gốc (undo shake). */
    private void restoreCamera(final PlayScreen context, float shakeX, float shakeY) {
        if (shakeX != 0f || shakeY != 0f) {
            context.getCamera().position.add(-shakeX, -shakeY, 0);
            context.getCamera().update();
        }
    }
}
