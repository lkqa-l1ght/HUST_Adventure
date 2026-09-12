package hust.adventure.screens.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import hust.adventure.screens.PlayScreen;

/**
 * Reusable banner component to render level completion and objective notification messages.
 *
 * <p>Encapsulates message duration timing, alpha transparency fading, text measurement,
 * text box background rendering, and centered layout positioning.</p>
 */
public class LevelNotificationBanner {
    /**
     * Default display duration in seconds for notification messages.
     */
    public static final float DEFAULT_DURATION = 4.0f;

    /**
     * Horizontal padding in pixels added around the rendered message text box.
     */
    public static final float DEFAULT_BOX_PADDING_X = 40f;

    /**
     * Vertical padding in pixels added around the rendered message text box.
     */
    public static final float DEFAULT_BOX_PADDING_Y = 30f;

    /**
     * Default horizontal screen center coordinate for rendering the banner.
     */
    public static final float DEFAULT_CENTER_X = 400f;

    /**
     * Default vertical screen center coordinate for rendering the banner.
     */
    public static final float DEFAULT_CENTER_Y = 300f;

    private float timer = 0f;
    private float duration = DEFAULT_DURATION;
    private String message;
    private Texture textBoxTexture;
    private final GlyphLayout glyphLayout;

    /**
     * Constructs a new LevelNotificationBanner with default configuration and layout.
     */
    public LevelNotificationBanner() {
        this(null, new GlyphLayout());
    }

    /**
     * Constructs a new LevelNotificationBanner with the specified background texture.
     *
     * @param textBoxTexture  the background texture for the banner, or null for text-only rendering.
     */
    public LevelNotificationBanner(final Texture textBoxTexture) {
        this(textBoxTexture, new GlyphLayout());
    }

    /**
     * Constructs a new LevelNotificationBanner with the specified background texture and custom layout helper.
     *
     * @param textBoxTexture  the background texture for the banner, or null for text-only rendering.
     * @param glyphLayout     the GlyphLayout instance used to compute text bounds.
     */
    public LevelNotificationBanner(final Texture textBoxTexture, final GlyphLayout glyphLayout) {
        this.textBoxTexture = textBoxTexture;
        this.glyphLayout = glyphLayout != null ? glyphLayout : new GlyphLayout();
    }

    /**
     * Activates the banner to display the given message for the default duration.
     *
     * @param message  the text message to display.
     */
    public void show(final String message) {
        show(message, this.textBoxTexture, DEFAULT_DURATION);
    }

    /**
     * Activates the banner to display the given message for a specified duration.
     *
     * @param message   the text message to display.
     * @param duration  display duration in seconds.
     */
    public void show(final String message, final float duration) {
        show(message, this.textBoxTexture, duration);
    }

    /**
     * Activates the banner to display the given message with a background texture for the default duration.
     *
     * @param message         the text message to display.
     * @param textBoxTexture  the background texture to render behind the text.
     */
    public void show(final String message, final Texture textBoxTexture) {
        show(message, textBoxTexture, DEFAULT_DURATION);
    }

    /**
     * Activates the banner to display the given message with a background texture for a specified duration.
     *
     * @param message         the text message to display.
     * @param textBoxTexture  the background texture to render behind the text, or null to keep current texture.
     * @param duration        display duration in seconds.
     */
    public void show(final String message, final Texture textBoxTexture, final float duration) {
        this.message = message;
        if (textBoxTexture != null) {
            this.textBoxTexture = textBoxTexture;
        }
        this.duration = duration > 0f ? duration : DEFAULT_DURATION;
        this.timer = this.duration;
    }

    /**
     * Updates the remaining display timer and recalculates alpha fading.
     *
     * @param delta  elapsed frame time in seconds.
     */
    public void update(final float delta) {
        if (timer > 0f) {
            timer -= delta;
            if (timer < 0f) {
                timer = 0f;
            }
        }
    }

    /**
     * Draws the banner using the batch and font extracted from the PlayScreen context.
     *
     * @param context  the active PlayScreen providing SpriteBatch and BitmapFont.
     */
    public void draw(final PlayScreen context) {
        if (context == null || !isVisible()) {
            return;
        }
        draw(context.getBatch(), context.getFont());
    }

    /**
     * Draws the banner background box and text using the provided SpriteBatch and BitmapFont.
     *
     * @param batch  the active SpriteBatch for rendering textures and text.
     * @param font   the BitmapFont used to render the message text.
     */
    public void draw(final SpriteBatch batch, final BitmapFont font) {
        if (!isVisible() || batch == null || font == null || message == null) {
            return;
        }

        final float alpha = getAlpha();

        final boolean wasDrawing = batch.isDrawing();
        if (!wasDrawing) {
            batch.begin();
        }

        final Color batchColor = batch.getColor();
        final float origBatchR = batchColor.r;
        final float origBatchG = batchColor.g;
        final float origBatchB = batchColor.b;
        final float origBatchA = batchColor.a;

        final Color fontColor = font.getColor();
        final float origFontR = fontColor.r;
        final float origFontG = fontColor.g;
        final float origFontB = fontColor.b;
        final float origFontA = fontColor.a;

        batch.setColor(1f, 1f, 1f, alpha);
        font.setColor(0f, 0f, 0f, alpha);

        glyphLayout.setText(font, message);

        final float boxW = glyphLayout.width + DEFAULT_BOX_PADDING_X;
        final float boxH = glyphLayout.height + DEFAULT_BOX_PADDING_Y;
        final float boxX = DEFAULT_CENTER_X - boxW / 2f;
        final float boxY = DEFAULT_CENTER_Y - boxH / 2f;

        if (textBoxTexture != null) {
            batch.draw(textBoxTexture, boxX, boxY, boxW, boxH);
        }
        font.draw(batch, message, DEFAULT_CENTER_X - glyphLayout.width / 2f, DEFAULT_CENTER_Y + glyphLayout.height / 2f);

        batch.setColor(origBatchR, origBatchG, origBatchB, origBatchA);
        font.setColor(origFontR, origFontG, origFontB, origFontA);

        if (!wasDrawing) {
            batch.end();
        }
    }

    /**
     * Checks whether the notification banner is currently visible.
     *
     * @return true if the remaining display timer is greater than zero, false otherwise.
     */
    public boolean isVisible() {
        return timer > 0f;
    }

    /**
     * Computes the current transparency alpha value for fading out.
     *
     * <p>Maintains full opacity (1.0) during the first half of the duration and linearly
     * fades down to zero during the second half.</p>
     *
     * @return the calculated alpha value clamped between 0.0 and 1.0.
     */
    public float getAlpha() {
        if (duration <= 0f || timer <= 0f) {
            return 0f;
        }
        final float progress = timer / duration;
        return Math.max(0f, Math.min(1f, progress * 2f));
    }

    /**
     * Gets the remaining display timer in seconds.
     *
     * @return remaining seconds.
     */
    public float getTimer() {
        return timer;
    }

    /**
     * Gets the configured total display duration in seconds.
     *
     * @return total duration in seconds.
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Gets the currently active notification message.
     *
     * @return message string, or null if uninitialized.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the background texture of the banner.
     *
     * @return the background Texture, or null if none set.
     */
    public Texture getTexture() {
        return textBoxTexture;
    }

    /**
     * Sets the background texture of the banner.
     *
     * @param textBoxTexture  the background texture to use.
     */
    public void setTexture(final Texture textBoxTexture) {
        this.textBoxTexture = textBoxTexture;
    }

    /**
     * Gets the GlyphLayout used for text dimension calculations.
     *
     * @return the GlyphLayout instance.
     */
    public GlyphLayout getGlyphLayout() {
        return glyphLayout;
    }

    /**
     * Resets the banner timer immediately to zero, dismissing the banner.
     */
    public void reset() {
        this.timer = 0f;
    }
}
