package hust.adventure.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * HUD component responsible for rendering active status effects, spells, and collected artifacts.
 */
public class StatusEffectsHUD {
    private final StringBuilder sb = new StringBuilder();

    public StatusEffectsHUD() {
    }

    /**
     * Renders active spells, status effects, and artifacts overlay.
     *
     * @param batch the SpriteBatch to draw with
     * @param font  the BitmapFont to use for rendering text
     * @param data  the status effects data DTO
     */
    public void render(final SpriteBatch batch, final BitmapFont font, final StatusEffectsData data) {
        if (data == null) {
            return;
        }
        batch.begin();

        // ── Artifacts ────────────────────────────────────────────────────────
        float afx = 14f;
        float afy = 492f;
        if (data.isHasNao()) {
            font.setColor(1f, 0.85f, 0.2f, 1f);
            font.draw(batch, "★ Nao 100%", afx, afy);
            afy -= 16f;
        }
        if (data.isHasUsb()) {
            font.setColor(0.3f, 0.8f, 1f, 1f);
            font.draw(batch, "★ USB", afx, afy);
            afy -= 16f;
        }

        // ── Active Spells / Status Effects ───────────────────────────────────
        float spellY = afy - 4f;
        if (data.getEnemyTimeScale() == 0f) {
            font.setColor(1f, 0.5f, 0f, 1f);
            font.draw(batch, "⚡ STUN!", 14, spellY);
            spellY -= 16f;
        } else if (data.getEnemyTimeScale() == 0.3f) {
            font.setColor(1f, 0.9f, 0f, 1f);
            font.draw(batch, "⏱ SLOW", 14, spellY);
            spellY -= 16f;
        }
        if (data.getShowEnemiesTimer() > 0f) {
            font.setColor(0.3f, 1f, 1f, 1f);
            sb.setLength(0);
            sb.append("RADAR ").append(Math.round(data.getShowEnemiesTimer())).append("s");
            font.draw(batch, sb, 14, spellY);
            spellY -= 16f;
        }

        if (data.isSpeedBoosted()) {
            font.setColor(0.3f, 1f, 0.8f, 1f);
            font.draw(batch, "SPEED+", 14, spellY);
            spellY -= 16f;
        }
        if (data.isHpRegen()) {
            font.setColor(0.3f, 1f, 0.4f, 1f);
            font.draw(batch, "HP REGEN", 14, spellY);
            spellY -= 16f;
        }
        if (data.isConfused()) {
            font.setColor(1f, 0.2f, 1f, 1f);
            font.draw(batch, "CONFUSED!", 14, spellY);
            spellY -= 16f;
        }

        font.setColor(Color.WHITE);
        batch.end();
    }
}
