package hust.adventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern Level Up UI rendering and key input delegation.
 * Fully decoupled from Player and UpgradeAction models.
 */
public class LevelUpUI {
    private final List<LevelUpChoiceData> currentChoices = new ArrayList<>();
    private Runnable onResume;

    public LevelUpUI() {
    }

    public void setOnResume(final Runnable onResume) {
        this.onResume = onResume;
    }

    public void setChoices(final LevelUpUIData data) {
        this.currentChoices.clear();
        if (data != null && data.getChoices() != null) {
            this.currentChoices.addAll(data.getChoices());
        }
    }

    public void render(final SpriteBatch batch, final ShapeRenderer shapeRenderer, final BitmapFont font) {
        if (currentChoices.isEmpty())
            return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Nền đen mờ
        shapeRenderer.setColor(new Color(0, 0, 0, 0.8f));
        shapeRenderer.rect(0, 0, 800, 600);

        // Khung Level Up
        final float panelW = 500;
        final float panelH = 400;
        final float panelX = (800 - panelW) / 2;
        final float panelY = (600 - panelH) / 2;

        shapeRenderer.setColor(new Color(0.1f, 0.2f, 0.4f, 1f));
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        // Viền
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rectLine(panelX, panelY, panelX + panelW, panelY, 4);
        shapeRenderer.rectLine(panelX, panelY + panelH, panelX + panelW, panelY + panelH, 4);
        shapeRenderer.rectLine(panelX, panelY, panelX, panelY + panelH, 4);
        shapeRenderer.rectLine(panelX + panelW, panelY, panelX + panelW, panelY + panelH, 4);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
        font.setColor(Color.YELLOW);
        font.draw(batch, "--- LEVEL UP! ---", panelX + 170, panelY + panelH - 30);
        font.setColor(Color.WHITE);
        font.draw(batch, "Chon 1 phan thuong:", panelX + 50, panelY + panelH - 80);

        int offsetY = 130;

        for (int i = 0; i < currentChoices.size(); i++) {
            final LevelUpChoiceData action = currentChoices.get(i);
            final int choiceNum = i + 1;

            font.setColor(Color.CYAN);
            font.draw(batch, "[" + choiceNum + "] " + action.getName(), panelX + 70, panelY + panelH - offsetY);
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "    " + action.getDescription(), panelX + 70, panelY + panelH - offsetY - 25);

            offsetY += 80;
        }

        batch.end();
    }

    public void update(final int keyPressed, final java.util.function.Consumer<Integer> choiceCallback) {
        if (currentChoices.isEmpty()) {
            return;
        }

        if (keyPressed > 0 && keyPressed <= currentChoices.size()) {
            if (choiceCallback != null) {
                choiceCallback.accept(keyPressed - 1);
            }
            EventDispatcher.getInstance().playSfx(AssetPaths.SFX_UI_CLICK);
            if (onResume != null) {
                onResume.run();
            }
            currentChoices.clear();
        }
    }

    public void dispose() {
    }
}
