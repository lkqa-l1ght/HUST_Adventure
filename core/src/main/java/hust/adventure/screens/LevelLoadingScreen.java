package hust.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import hust.adventure.HustGame;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.core.data.LevelConfig;

/**
 * Loading screen shown between level transitions to prevent visual hitches.
 */
public class LevelLoadingScreen extends BaseScreen {
    private final LevelConfig targetConfig;
    private int frameCount = 0;
    private final Texture textBoxTexture;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public LevelLoadingScreen(final HustGame game, final LevelConfig targetConfig) {
        super(game);
        if (targetConfig == null) {
            throw new IllegalArgumentException("LevelConfig cannot be null");
        }
        this.targetConfig = targetConfig;
        this.textBoxTexture = game.getAssetManager().getTexture(AssetPaths.UI_TEXT_BOX);
    }

    @Override
    public void render(float delta) {
        // Clear screen to a dark shade
        Gdx.gl.glClearColor(0.04f, 0.02f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        final Color origFontColor = font.getColor();

        // Draw loading UI using text_box texture centered
        final String text = "Đang tải bản đồ...";
        glyphLayout.setText(font, text);

        final float boxW = 280f;
        final float boxH = 70f;
        final float boxX = 400f - boxW / 2f;
        final float boxY = 300f - boxH / 2f;

        if (textBoxTexture != null) {
            batch.draw(textBoxTexture, boxX, boxY, boxW, boxH);
        }

        font.setColor(Color.BLACK);
        font.draw(batch, text, 400f - glyphLayout.width / 2f, 300f + glyphLayout.height / 2f);

        font.setColor(origFontColor);
        batch.end();

        frameCount++;
        if (frameCount >= 2) {
            // Load map and transition to target play screen
            final Screen nextScreen = game.getLevelFactory().createLevel(game, targetConfig);
            if (nextScreen != null) {
                game.setScreen(nextScreen);
                game.getScreenTransition().fadeIn(0.5f);
            }
        }
    }
}
