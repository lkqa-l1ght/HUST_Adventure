package hust.adventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import hust.adventure.entities.player.Player;
import hust.adventure.entities.player.input.DebugInputHandler;

/**
 * Renders the debug mode overlay panel when debug mode is enabled. Queries state from DebugInputHandler to render
 * selection menus.
 */
public class DebugUI {
    private DebugInputHandler inputHandler;
    private final StringBuilder sb = new StringBuilder();

    // UI Layout Constants to avoid magic numbers
    private static final float PANEL_X = 520f;
    private static final float PANEL_Y = 160f;
    private static final float PANEL_WIDTH = 260f;
    private static final float PANEL_HEIGHT = 420f;

    private static final float SELECT_X = 240f;
    private static final float SELECT_Y = 160f;
    private static final float SELECT_WIDTH = 260f;
    private static final float SELECT_HEIGHT = 420f;

    private static final float LINE_HEIGHT = 18f;
    private static final float SECTION_TITLE_HEIGHT = 20f;
    private static final float SECTION_SPACING = 24f;
    private static final float SMALL_SPACING = 6f;

    // Stylized color theme constants
    private static final Color BG_COLOR = new Color(0.08f, 0.09f, 0.13f, 0.85f);
    private static final Color BORDER_COLOR = new Color(0.18f, 0.50f, 0.93f, 0.9f);
    private static final Color TITLE_COLOR = new Color(0.95f, 0.61f, 0.07f, 1f);
    private static final Color HIGHLIGHT_BG = new Color(0.18f, 0.50f, 0.93f, 0.35f);

    /**
     * Constructs a new DebugUI.
     */
    public DebugUI() {
    }

    /**
     * Sets the input handler from which the UI queries selection state.
     *
     * @param inputHandler the debug input handler
     */
    public void setInputHandler(final DebugInputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * Renders the debug panel overlay and any active selection menus. Also renders the detailed F3 debug overlay if
     * active.
     *
     * @param batch         the sprite batch
     * @param shapeRenderer the shape renderer
     * @param font          the bitmap font
     * @param debugInfo     the detailed debug information DTO
     * @param player        the current player instance
     */
    public void render(final SpriteBatch batch, final ShapeRenderer shapeRenderer, final BitmapFont font,
            final DebugInfoUIData debugInfo, final Player player, final boolean showDebugMenu,
            final boolean showDebugInfo, final boolean isGodMode, final boolean isFastRun) {

        if (!showDebugMenu && !showDebugInfo) {
            return;
        }

        // Render panels backgrounds and borders
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (showDebugMenu) {
            final boolean active = (inputHandler != null && inputHandler.isActive());
            final DebugOption[] currentOptions = (inputHandler != null) ? inputHandler.getCurrentOptions(player) : null;
            final int selectedIndex = (inputHandler != null) ? inputHandler.getSelectedIndex() : 0;

            // Main panel background
            shapeRenderer.setColor(BG_COLOR);
            shapeRenderer.rect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);

            // Selection panel background if active
            if (active && currentOptions != null) {
                shapeRenderer.rect(SELECT_X, SELECT_Y, SELECT_WIDTH, SELECT_HEIGHT);

                // Render selected option highlight background
                shapeRenderer.setColor(HIGHLIGHT_BG);
                float itemY = SELECT_Y + SELECT_HEIGHT - 75f - (selectedIndex * 30f);
                shapeRenderer.rect(SELECT_X + 10f, itemY - 5f, SELECT_WIDTH - 20f, 26f);
            }
        }

        if (showDebugInfo) {
            // Render detailed info panel background
            shapeRenderer.setColor(BG_COLOR);
            shapeRenderer.rect(14f, 14f, 300f, 470f);
        }

        shapeRenderer.end();

        // Neon border lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(BORDER_COLOR);

        if (showDebugMenu) {
            shapeRenderer.rect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);
            final boolean active = (inputHandler != null && inputHandler.isActive());
            final DebugOption[] currentOptions = (inputHandler != null) ? inputHandler.getCurrentOptions(player) : null;
            if (active && currentOptions != null) {
                shapeRenderer.rect(SELECT_X, SELECT_Y, SELECT_WIDTH, SELECT_HEIGHT);
            }
        }

        if (showDebugInfo) {
            // Render detailed info panel border
            shapeRenderer.rect(14f, 14f, 300f, 470f);
        }

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Render text elements
        batch.begin();

        if (showDebugMenu) {
            final boolean active = (inputHandler != null && inputHandler.isActive());
            final DebugOption[] currentOptions = (inputHandler != null) ? inputHandler.getCurrentOptions(player) : null;
            final int selectedIndex = (inputHandler != null) ? inputHandler.getSelectedIndex() : 0;
            final SelectionMode activeMode = (inputHandler != null) ? inputHandler.getActiveMode() : SelectionMode.NONE;

            // Main panel title and info
            font.setColor(TITLE_COLOR);
            font.draw(batch, "=== DEBUG MENU ===", PANEL_X + 30f, PANEL_Y + PANEL_HEIGHT - 20f);

            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, "Press keys to action:", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 60f);

            // F2: Debug Menu indicator
            font.setColor(Color.WHITE);
            font.draw(batch, "[F2] Debug Menu: ", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 100f);
            font.setColor(Color.GREEN);
            font.draw(batch, "VISIBLE", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 100f);

            // F3: Hitboxes indicator
            font.setColor(Color.WHITE);
            font.draw(batch, "[F3] Hitboxes: ", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 135f);
            if (showDebugInfo) {
                font.setColor(Color.GREEN);
                font.draw(batch, "VISIBLE", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 135f);
            } else {
                font.setColor(Color.RED);
                font.draw(batch, "HIDDEN", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 135f);
            }

            // F4: God Mode
            font.setColor(Color.WHITE);
            font.draw(batch, "[F4] God Mode: ", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 170f);
            if (isGodMode) {
                font.setColor(Color.GREEN);
                font.draw(batch, "ON", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 170f);
            } else {
                font.setColor(Color.RED);
                font.draw(batch, "OFF", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 170f);
            }

            // F5: Speed Run
            font.setColor(Color.WHITE);
            font.draw(batch, "[F5] Speed Hack: ", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 205f);
            if (isFastRun) {
                font.setColor(Color.GREEN);
                font.draw(batch, "ON", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 205f);
            } else {
                font.setColor(Color.RED);
                font.draw(batch, "OFF", PANEL_X + 150f, PANEL_Y + PANEL_HEIGHT - 205f);
            }

            // F6: Switch Map
            font.setColor(Color.WHITE);
            font.draw(batch, "[F6] Switch Map", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 240f);

            // F7: Spawn Item
            font.draw(batch, "[F7] Spawn Item", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 275f);

            // F8: Spawn Monster
            font.draw(batch, "[F8] Spawn Monster", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 310f);

            // F9: Equip Item
            font.draw(batch, "[F9] Equip Item", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 345f);

            // F10: Level Up
            font.draw(batch, "[F10] Level Up", PANEL_X + 15f, PANEL_Y + PANEL_HEIGHT - 380f);

            // Render Selection list if active
            if (active && currentOptions != null) {
                font.setColor(TITLE_COLOR);
                sb.setLength(0);
                sb.append("SELECT ").append(activeMode.name());
                font.draw(batch, sb, SELECT_X + 20f, SELECT_Y + SELECT_HEIGHT - 20f);

                font.setColor(Color.LIGHT_GRAY);
                font.draw(batch, "Arrows to move, Enter select, ESC", SELECT_X + 15f, SELECT_Y + SELECT_HEIGHT - 45f);

                for (int i = 0; i < currentOptions.length; i++) {
                    if (i == selectedIndex) {
                        font.setColor(Color.WHITE);
                        sb.setLength(0);
                        sb.append("> ").append(currentOptions[i].getDisplayName());
                        font.draw(batch, sb, SELECT_X + 20f, SELECT_Y + SELECT_HEIGHT - 75f - (i * 30f));
                    } else {
                        font.setColor(Color.LIGHT_GRAY);
                        sb.setLength(0);
                        sb.append("  ").append(currentOptions[i].getDisplayName());
                        font.draw(batch, sb, SELECT_X + 20f, SELECT_Y + SELECT_HEIGHT - 75f - (i * 30f));
                    }
                }
            }
        }

        if (showDebugInfo) {
            float currentY = 464f;

            // Title System & Level
            font.setColor(TITLE_COLOR);
            font.draw(batch, "=== SYSTEM & LEVEL ===", 24f, currentY);
            currentY -= SECTION_TITLE_HEIGHT;

            font.setColor(Color.WHITE);
            sb.setLength(0);
            sb.append("Pos: X = ");
            appendFloat(sb, debugInfo.getX(), 2);
            sb.append(", Y = ");
            appendFloat(sb, debugInfo.getY(), 2);
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Map: ").append(debugInfo.getMapName());
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("State: ").append(debugInfo.getStateName());
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Speed: ");
            appendFloat(sb, debugInfo.getSpeed(), 1);
            font.draw(batch, sb, 24f, currentY);
            currentY -= SECTION_SPACING;

            // Title Active Weapons
            font.setColor(TITLE_COLOR);
            font.draw(batch, "=== ACTIVE WEAPONS ===", 24f, currentY);
            currentY -= SECTION_TITLE_HEIGHT;

            font.setColor(Color.WHITE);
            if (debugInfo.getWeapons().isEmpty()) {
                font.draw(batch, "None", 24f, currentY);
                currentY -= LINE_HEIGHT;
            } else {
                for (final String wInfo : debugInfo.getWeapons()) {
                    font.draw(batch, wInfo, 24f, currentY);
                    currentY -= LINE_HEIGHT;
                }
            }
            currentY -= SMALL_SPACING;

            // Title Active Gears
            font.setColor(TITLE_COLOR);
            font.draw(batch, "=== ACTIVE GEARS ===", 24f, currentY);
            currentY -= SECTION_TITLE_HEIGHT;

            font.setColor(Color.WHITE);
            if (debugInfo.getGears().isEmpty()) {
                font.draw(batch, "None", 24f, currentY);
                currentY -= LINE_HEIGHT;
            } else {
                for (final String gInfo : debugInfo.getGears()) {
                    font.draw(batch, gInfo, 24f, currentY);
                    currentY -= LINE_HEIGHT;
                }
            }
            currentY -= SMALL_SPACING;

            // Title Multipliers
            font.setColor(TITLE_COLOR);
            font.draw(batch, "=== MULTIPLIERS ===", 24f, currentY);
            currentY -= SECTION_TITLE_HEIGHT;

            font.setColor(Color.WHITE);
            sb.setLength(0);
            sb.append("Power Mult: ");
            appendFloat(sb, debugInfo.getPowerMultiplier(), 2);
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Cooldown Mult: ");
            appendFloat(sb, debugInfo.getCooldownMultiplier(), 2);
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Area Mult: ");
            appendFloat(sb, debugInfo.getAreaMultiplier(), 2);
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Magnet Mult: ");
            appendFloat(sb, debugInfo.getMagnetMultiplier(), 2);
            font.draw(batch, sb, 24f, currentY);
            currentY -= SECTION_SPACING;

            // Title Performance
            font.setColor(TITLE_COLOR);
            font.draw(batch, "=== PERFORMANCE ===", 24f, currentY);
            currentY -= SECTION_TITLE_HEIGHT;

            font.setColor(Color.WHITE);
            sb.setLength(0);
            sb.append("FPS: ").append(debugInfo.getFps());
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Active Entities: ").append(debugInfo.getActiveEntitiesCount());
            font.draw(batch, sb, 24f, currentY);
            currentY -= LINE_HEIGHT;

            sb.setLength(0);
            sb.append("Memory: ").append(debugInfo.getUsedMemoryMB()).append(" MB / ")
                    .append(debugInfo.getTotalMemoryMB()).append(" MB");
            font.draw(batch, sb, 24f, currentY);
        }

        batch.end();
    }

    private void appendFloat(final StringBuilder builder, final float val, final int decimals) {
        if (Float.isNaN(val)) {
            builder.append("NaN");
            return;
        }
        if (Float.isInfinite(val)) {
            builder.append(val > 0 ? "Infinity" : "-Infinity");
            return;
        }
        float tempVal = val;
        if (tempVal < 0) {
            builder.append('-');
            tempVal = -tempVal;
        }
        float rounder = 0.5f;
        for (int i = 0; i < decimals; i++) {
            rounder /= 10.0f;
        }
        tempVal += rounder;
        long ipart = (long) tempVal;
        builder.append(ipart);
        if (decimals > 0) {
            builder.append('.');
            float fpart = tempVal - ipart;
            for (int i = 0; i < decimals; i++) {
                fpart *= 10.0f;
                int digit = (int) fpart;
                builder.append(digit);
                fpart -= digit;
            }
        }
    }
}
