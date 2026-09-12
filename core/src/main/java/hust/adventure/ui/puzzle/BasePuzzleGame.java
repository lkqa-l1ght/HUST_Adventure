package hust.adventure.ui.puzzle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;
import hust.adventure.screens.PlayScreen;

/**
 * Base abstraction for library academic challenge mini-games providing common intro modal rendering,
 * dialogue text framing, and lifecycle state management.
 */
public abstract class BasePuzzleGame implements PuzzleGame {
    protected static final float INTRO_BOX_W = 620f;
    protected static final float INTRO_BOX_H = 460f;
    protected static final float INTRO_BOX_X = 90f;
    protected static final float INTRO_BOX_Y = 70f;

    protected static final float START_BTN_W = 160f;
    protected static final float START_BTN_H = 45f;
    protected static final float START_BTN_X = 400f - START_BTN_W / 2f;
    protected static final float START_BTN_Y = 110f;

    private static final Color OVERLAY_COLOR = new Color(0f, 0f, 0f, 0.75f);
    private static final Color TITLE_COLOR = new Color(0.6f, 0.1f, 0.1f, 1f);
    private static final Color START_BTN_TEXT_COLOR = new Color(0.1f, 0.5f, 0.1f, 1f);

    protected PlayScreen context;
    protected boolean isSolved;
    protected boolean showIntro = true;

    protected Texture textBoxTexture;
    protected final GlyphLayout textLayout = new GlyphLayout();
    protected final Vector2 tmpMouse = new Vector2();

    @Override
    public String getBackgroundPath() {
        return AssetPaths.MAP_LIBRARY_BG;
    }

    @Override
    public void init(final PlayScreen context) {
        this.context = context;
        if (context != null && context.getGame() != null && context.getGame().getAssetManager() != null) {
            this.textBoxTexture = context.getGame().getAssetManager().getTexture(AssetPaths.UI_TEXT_BOX);
        }
    }

    @Override
    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(final boolean solved) {
        this.isSolved = solved;
    }

    public boolean isIntroVisible() {
        return showIntro;
    }

    public void setIntroVisible(final boolean visible) {
        this.showIntro = visible;
    }

    /**
     * Checks if the start button was clicked during the intro modal phase.
     *
     * @return true if the start button was clicked and intro was dismissed
     */
    protected boolean checkIntroClick() {
        if (!showIntro) {
            return false;
        }

        if (Gdx.input.justTouched()) {
            tmpMouse.set(Gdx.input.getX(), Gdx.input.getY());
            if (context != null) {
                context.unproject(tmpMouse);
            }
            final float mx = tmpMouse.x;
            final float my = tmpMouse.y;

            if (mx >= START_BTN_X && mx <= START_BTN_X + START_BTN_W
                    && my >= START_BTN_Y && my <= START_BTN_Y + START_BTN_H) {
                showIntro = false;
                EventDispatcher.getInstance().playSfx(AssetPaths.SFX_UI_CLICK);
                onIntroDismissed();
                return true;
            }
        }
        return false;
    }

    /**
     * Hook called immediately when the user dismisses the intro modal.
     */
    protected void onIntroDismissed() {
    }

    /**
     * Draws a reusable text box with centered text.
     */
    protected void drawTextInBox(final SpriteBatch batch, final BitmapFont font, final CharSequence text,
                                final float x, final float y, final Texture textBoxTexture, final GlyphLayout layout) {
        layout.setText(font, text);
        final float paddingX = 20f;
        final float paddingY = 15f;
        final float boxWidth = layout.width + paddingX * 2;
        final float boxHeight = layout.height + paddingY * 2;

        final float boxX = x - boxWidth / 2f;
        final float boxY = y - boxHeight / 2f;

        if (textBoxTexture != null) {
            batch.draw(textBoxTexture, boxX, boxY, boxWidth, boxHeight);
        }

        final Color origColor = font.getColor();
        final float r = origColor.r;
        final float g = origColor.g;
        final float b = origColor.b;
        final float a = origColor.a;
        font.setColor(Color.BLACK);
        font.draw(batch, text, x - layout.width / 2f, y + layout.height / 2f);
        font.setColor(r, g, b, a);
    }

    /**
     * Renders standard academic challenge introduction modal dialog.
     */
    protected void drawIntroModal(final ShapeRenderer shapeRenderer, final SpriteBatch batch,
                                  final String title, final String introText) {
        // Check hover
        tmpMouse.set(Gdx.input.getX(), Gdx.input.getY());
        if (context != null) {
            context.unproject(tmpMouse);
        }
        final boolean isHovered = (tmpMouse.x >= START_BTN_X && tmpMouse.x <= START_BTN_X + START_BTN_W
                && tmpMouse.y >= START_BTN_Y && tmpMouse.y <= START_BTN_Y + START_BTN_H);

        // Draw overlay using ShapeRenderer
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(OVERLAY_COLOR);
        shapeRenderer.rect(0, 0, 800, 600);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Draw Text Box and Texts using SpriteBatch
        batch.begin();
        if (textBoxTexture != null) {
            batch.setColor(Color.WHITE);
            batch.draw(textBoxTexture, INTRO_BOX_X, INTRO_BOX_Y, INTRO_BOX_W, INTRO_BOX_H);
        }

        final BitmapFont font = context != null ? context.getFont() : null;
        if (font != null) {
            final Color origColor = font.getColor();

            // Draw title
            font.setColor(TITLE_COLOR);
            textLayout.setText(font, title);
            font.draw(batch, title, 400f - textLayout.width / 2f, 480f);

            // Draw intro body text
            font.setColor(Color.BLACK);
            font.draw(batch, introText, 140f, 420f);

            // Draw Start Button Box
            if (textBoxTexture != null) {
                batch.setColor(isHovered ? Color.LIGHT_GRAY : Color.WHITE);
                batch.draw(textBoxTexture, START_BTN_X, START_BTN_Y, START_BTN_W, START_BTN_H);
            }

            // Draw Start Button Text
            font.setColor(START_BTN_TEXT_COLOR);
            textLayout.setText(font, "BẮT ĐẦU");
            font.draw(batch, "BẮT ĐẦU", 400f - textLayout.width / 2f,
                    START_BTN_Y + START_BTN_H / 2f + textLayout.height / 2f);

            font.setColor(origColor);
        }
        batch.end();
    }
}
