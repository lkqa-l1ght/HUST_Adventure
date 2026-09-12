package hust.adventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import hust.adventure.core.TimeProvider;

/**
 * Upgraded HUD – modern bar design with gradient fills, icons, rounded feel, and a low-HP pulse warning.
 */
public class HUD {

    // Layout constants
    private static final float PANEL_X = 14f;
    private static final float PANEL_Y = 517f;
    private static final float PANEL_W = 210f;
    private static final float PANEL_H = 79f;

    private static final float BAR_X = 42f;
    private static final float BAR_W = 155f;
    private static final float BAR_H = 13f;
    private static final float BAR_HP_Y = 575f;
    private static final float BAR_SP_Y = 556f;
    private static final float BAR_EX_Y = 537f;

    // White pixel for drawing solid rects via SpriteBatch (avoids ShapeRenderer flush)
    private static Texture whitePixel;

    // Low-HP pulse
    private float pulseTimer = 0f;

    private TimeProvider timeProvider;

    private final StringBuilder sb = new StringBuilder();

    public HUD() {
    }

    public static void disposeStatic() {
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    // ── helpers ────────────────────────────────────────────────────────────

    /**
     * Draws a status bar with dark background and two-color gradient fill.
     */
    private void drawBar(ShapeRenderer sr, float x, float y, float w, float h, float percent, Color colLeft,
            Color colRight, float bgAlpha) {
        // Background
        sr.setColor(0.1f, 0.1f, 0.12f, bgAlpha);
        sr.rect(x, y, w, h);

        // Fill gradient by drawing thin slices
        if (percent > 0) {
            float fillW = Math.max(2f, w * percent);
            int steps = (int) fillW;
            for (int i = 0; i < steps; i++) {
                float t = i / (float) steps;
                float r = colLeft.r + (colRight.r - colLeft.r) * t;
                float g = colLeft.g + (colRight.g - colLeft.g) * t;
                float b = colLeft.b + (colRight.b - colLeft.b) * t;
                sr.setColor(r, g, b, 1f);
                sr.rect(x + i, y, 1f, h);
            }
        }

        // Thin border
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1f, 1f, 1f, 0.18f);
        sr.rect(x, y, w, h);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
    }

    // ── main render ────────────────────────────────────────────────────────
    public void render(SpriteBatch batch, ShapeRenderer sr, BitmapFont font, HUDData data) {
        if (data == null) {
            return;
        }

        float hpPct = Math.max(0, Math.min(1, data.getHp() / data.getMaxHp()));
        float spPct = Math.max(0, Math.min(1, data.getStamina() / data.getMaxStamina()));
        float exPct = Math.max(0, Math.min(1, data.getExp() / data.getExpToNextLevel()));

        // Pulse timer for low HP warning
        pulseTimer += Gdx.graphics.getDeltaTime();
        boolean lowHp = hpPct < 0.25f;
        float pulse = (float) (Math.sin(pulseTimer * 5.0) * 0.5 + 0.5); // 0 to 1

        // ── ShapeRenderer pass ──────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // HP bar: green -> yellow -> red based on percentage
        Color hpLeft, hpRight;
        if (hpPct > 0.5f) {
            hpLeft = new Color(0.1f, 0.9f, 0.3f, 1f);
            hpRight = new Color(0.4f, 1.0f, 0.2f, 1f);
        } else if (hpPct > 0.25f) {
            hpLeft = new Color(1.0f, 0.65f, 0.0f, 1f);
            hpRight = new Color(1.0f, 0.85f, 0.1f, 1f);
        } else {
            // Pulse red when HP is critically low
            float pr = 0.8f + pulse * 0.2f;
            hpLeft = new Color(pr, 0.05f, 0.05f, 1f);
            hpRight = new Color(1.0f, 0.2f + pulse * 0.2f, 0.0f, 1f);
        }
        drawBar(sr, BAR_X, BAR_HP_Y, BAR_W, BAR_H, hpPct, hpLeft, hpRight, 0.55f);

        // SP bar: purple -> cyan
        drawBar(sr, BAR_X, BAR_SP_Y, BAR_W, BAR_H, spPct, new Color(0.5f, 0.1f, 0.9f, 1f),
                new Color(0.1f, 0.7f, 1.0f, 1f), 0.55f);

        // EXP bar: dark purple -> pink
        drawBar(sr, BAR_X, BAR_EX_Y, BAR_W, BAR_H, exPct, new Color(0.5f, 0.0f, 0.7f, 1f),
                new Color(1.0f, 0.3f, 0.9f, 1f), 0.55f);

        // Outer panel border pulses red on low HP
        if (lowHp) {
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(1f, 0f, 0f, 0.3f + pulse * 0.6f);
            sr.rect(PANEL_X - 1, PANEL_Y - 1, PANEL_W + 2, PANEL_H + 2);
            sr.end();
            sr.begin(ShapeRenderer.ShapeType.Filled);
        }

        sr.end();

        // ── SpriteBatch text pass ───────────────────────────────────────────
        batch.begin();

        // Icon labels on the left of status bars
        font.setColor(1f, 0.4f, 0.4f, 1f);
        font.draw(batch, "HP", PANEL_X + 3, BAR_HP_Y + BAR_H - 1);
        font.setColor(0.6f, 0.4f, 1.0f, 1f);
        font.draw(batch, "SP", PANEL_X + 3, BAR_SP_Y + BAR_H - 1);
        font.setColor(0.9f, 0.4f, 1.0f, 1f);
        font.draw(batch, "EX", PANEL_X + 3, BAR_EX_Y + BAR_H - 1);

        // Numeric values on the right
        font.setColor(Color.WHITE);
        font.draw(batch, (int) data.getHp() + "/" + (int) data.getMaxHp(), BAR_X + BAR_W + 4, BAR_HP_Y + BAR_H - 1);
        font.draw(batch, (int) data.getStamina() + "/" + (int) data.getMaxStamina(), BAR_X + BAR_W + 4,
                BAR_SP_Y + BAR_H - 1);
        font.setColor(0.8f, 0.6f, 1f, 1f);
        font.draw(batch, "LV" + data.getLevel(), BAR_X + BAR_W + 4, BAR_EX_Y + BAR_H - 1);

        // Timer display
        int totalSeconds = (int) data.getCurrentTime();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        font.setColor(0.8f, 0.8f, 0.8f, 1f);
        sb.setLength(0);
        appendTwoDigits(sb, minutes);
        sb.append(':');
        appendTwoDigits(sb, seconds);
        font.draw(batch, sb, 370, 590);

        font.setColor(Color.WHITE);
        batch.end();
    }

    public void dispose() {
    }

    private void appendTwoDigits(StringBuilder builder, int value) {
        if (value < 10) {
            builder.append('0');
        }
        builder.append(value);
    }
}
