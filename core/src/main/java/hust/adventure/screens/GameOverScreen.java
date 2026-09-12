package hust.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import hust.adventure.HustGame;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.core.data.LevelConfig;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Vector2;

/**
 * Game Over screen – hiện ra khi player chết. Nhấn R → restart từ MAP_1 + reset ProgressContext. Nhấn ESC → thoát game.
 */
public class GameOverScreen extends BaseScreen {
    private final GameProgressContext progressContext;

    private static final float UI_W = 800f;
    private static final float UI_H = 600f;

    private final OrthographicCamera uiCam;
    private final Viewport viewport;
    private final Vector2 tmpMouse = new Vector2();
    private static Texture whitePixel;

    // Fade-in
    private float fadeAlpha = 1f; // bắt đầu tối, dần sáng
    private float fadeTimer = 0f;
    private static final float FADE_DURATION = 1.0f;

    // Pulse hiệu ứng chữ GAME OVER
    private float pulseTimer = 0f;

    // Nút
    private static final float BTN_W = 220f;
    private static final float BTN_H = 48f;
    private static final float BTN_RESTART_X = UI_W / 2f - BTN_W - 20f;
    private static final float BTN_RESTART_Y = UI_H / 2f - 100f;
    private static final float BTN_QUIT_X = UI_W / 2f + 20f;
    private static final float BTN_QUIT_Y = UI_H / 2f - 100f;

    private boolean restartHover = false;
    private boolean quitHover = false;

    public GameOverScreen(final HustGame game) {
        super(game);
        this.progressContext = game.getProgressContext();
        uiCam = new OrthographicCamera();
        viewport = new FitViewport(UI_W, UI_H, uiCam);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public static void disposeStatic() {
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }

    // ── Screen lifecycle ────────────────────────────────────────────────────
    @Override
    public void show() {
        fadeAlpha = 1f;
        fadeTimer = 0f;
        Gdx.input.setInputProcessor(null);
        if (game.getAudioManager() != null) {
            game.getAudioManager().playMusic("audio/music/game_over.mp3", true);
        }
    }

    @Override
    public void render(float delta) {
        // Update timers
        fadeTimer += delta;
        fadeAlpha = Math.max(0f, 1f - fadeTimer / FADE_DURATION);
        pulseTimer += delta;

        // Mouse hover detection using unprojected coordinates
        tmpMouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(tmpMouse);
        float mx = tmpMouse.x;
        float my = tmpMouse.y;
        restartHover = inButton(mx, my, BTN_RESTART_X, BTN_RESTART_Y);
        quitHover = inButton(mx, my, BTN_QUIT_X, BTN_QUIT_Y);

        // Input
        if (fadeTimer > FADE_DURATION) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R) || (Gdx.input.justTouched() && restartHover)) {
                doRestart();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || (Gdx.input.justTouched() && quitHover)) {
                Gdx.app.exit();
                return;
            }
        }

        // ── Draw ────────────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(uiCam.combined);
        batch.setProjectionMatrix(uiCam.combined);

        // --- Background gradient (dark red → black) ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Vignette đỏ viền
        for (int i = 0; i < 40; i++) {
            float t = i / 40f;
            float a = (1f - t) * 0.55f;
            shapeRenderer.setColor(0.6f, 0f, 0f, a);
            float margin = i * 10f;
            shapeRenderer.rect(margin, margin, UI_W - margin * 2, UI_H - margin * 2);
        }

        // Nền chính
        shapeRenderer.setColor(0.04f, 0.02f, 0.02f, 1f);
        shapeRenderer.rect(80, 100, UI_W - 160, UI_H - 200);

        // Đường viền neon đỏ
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.9f, 0.1f, 0.1f, 0.9f);
        shapeRenderer.rect(80, 100, UI_W - 160, UI_H - 200);
        shapeRenderer.rect(84, 104, UI_W - 168, UI_H - 208);
        shapeRenderer.end();

        // --- Buttons ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Nút CHƠI LẠI
        float restartAlpha = restartHover ? 0.95f : 0.7f;
        shapeRenderer
                .setColor(restartHover ? new Color(0.2f, 0.7f, 0.2f, restartAlpha) : new Color(0.1f, 0.4f, 0.1f, 0.7f));
        shapeRenderer.rect(BTN_RESTART_X, BTN_RESTART_Y, BTN_W, BTN_H);

        // Nút THOÁT
        shapeRenderer.setColor(quitHover ? new Color(0.7f, 0.2f, 0.2f, 0.95f) : new Color(0.4f, 0.1f, 0.1f, 0.7f));
        shapeRenderer.rect(BTN_QUIT_X, BTN_QUIT_Y, BTN_W, BTN_H);

        // Fade overlay (black → fade in)
        if (fadeAlpha > 0) {
            shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);
            shapeRenderer.rect(0, 0, UI_W, UI_H);
        }

        shapeRenderer.end();

        // Button viền
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(restartHover ? Color.GREEN : new Color(0.3f, 0.8f, 0.3f, 1f));
        shapeRenderer.rect(BTN_RESTART_X, BTN_RESTART_Y, BTN_W, BTN_H);
        shapeRenderer.setColor(quitHover ? Color.RED : new Color(0.8f, 0.3f, 0.3f, 1f));
        shapeRenderer.rect(BTN_QUIT_X, BTN_QUIT_Y, BTN_W, BTN_H);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- Text ---
        batch.begin();
        GlyphLayout layout = new GlyphLayout();

        // Tiêu đề GAME OVER – pulse màu đỏ ↔ cam
        float pulse = (float) (Math.sin(pulseTimer * 3.0) * 0.5 + 0.5);
        font.setColor(1f, pulse * 0.4f, 0f, 1f);
        layout.setText(font, "GAME OVER");
        font.draw(batch, layout, (UI_W - layout.width) / 2f, UI_H / 2f + 120f);

        // Dòng phụ
        font.setColor(0.8f, 0.6f, 0.6f, 1f);
        layout.setText(font, "X da khong the hoan thanh do an...");
        font.draw(batch, layout, (UI_W - layout.width) / 2f, UI_H / 2f + 60f);

        // Stats
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        layout.setText(font, "Cap do: " + progressContext.getLevel() + "   EXP: " + (int) progressContext.getExp());
        font.draw(batch, layout, (UI_W - layout.width) / 2f, UI_H / 2f + 20f);

        // Nút text
        font.setColor(Color.WHITE);
        layout.setText(font, "[R] CHOI LAI");
        font.draw(batch, layout, BTN_RESTART_X + (BTN_W - layout.width) / 2f, BTN_RESTART_Y + BTN_H / 2f + 8f);

        layout.setText(font, "[ESC] THOAT");
        font.draw(batch, layout, BTN_QUIT_X + (BTN_W - layout.width) / 2f, BTN_QUIT_Y + BTN_H / 2f + 8f);

        // Hint
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        layout.setText(font, "Nhan R de bat dau lai tu dau");
        font.draw(batch, layout, (UI_W - layout.width) / 2f, BTN_RESTART_Y - 30f);

        font.setColor(Color.WHITE);
        batch.end();
    }

    private boolean inButton(float mx, float my, float bx, float by) {
        return mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H;
    }

    private void doRestart() {
        // Lấy config màn hiện tại TRƯỚC khi reset (reset sẽ không xóa config)
        LevelConfig savedConfig = progressContext.getCurrentLevelConfig();
        if (savedConfig == null) {
            // Fallback nếu chưa lưu được (ví dụ: chết ngay màn đầu)
            LevelConfig template = game.getLevelDataManager().getLevelConfig("FINAL_OUTSIDE");
            savedConfig = new LevelConfig("FINAL_OUTSIDE", template.getName(), template.getMapPath(),
                    template.getSpawnX(), template.getSpawnY(), template.getZoom(), template.getBgmPath(),
                    template.getAmbientColor(), template.isInfinite());
        }
        if (progressContext.hasCheckpoint()) {
            progressContext.restoreCheckpoint();
        } else {
            progressContext.reset();
        }
        final LevelLoadingScreen loadingScreen = new LevelLoadingScreen(game, savedConfig);
        game.getScreenTransition().fadeOut(loadingScreen, 0.5f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        // static resources handled in HustGame.dispose()
    }
}
