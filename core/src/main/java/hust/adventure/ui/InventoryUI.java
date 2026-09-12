package hust.adventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upgraded Inventory UI – grid layout with item sprites, hover tooltip, and a modern dark-academic panel style.
 * Fully decoupled from Player and Item models.
 */
public class InventoryUI {
    private final List<InventoryItemData> itemList = new ArrayList<>();

    // Panel geometry
    private static final float PNL_W = 460f;
    private static final float PNL_H = 340f;
    private static final float PNL_X = (800 - PNL_W) / 2f;
    private static final float PNL_Y = (600 - PNL_H) / 2f;

    // Grid slots
    private static final int COLS = 5;
    private static final float SLOT_S = 90f; // slot size (square)
    private static final float SLOT_PAD = 6f;
    private static final float GRID_X = PNL_X + (PNL_W - COLS * (SLOT_S + SLOT_PAD)) / 2f + SLOT_PAD / 2f;
    private static final float GRID_Y = PNL_Y + 60f;

    // Sprite cache (path → Texture)
    private static final Map<String, Texture> spriteCache = new HashMap<>();

    private static Texture getSprite(final String path) {
        if (path == null || path.isEmpty())
            return null;
        return spriteCache.computeIfAbsent(path, p -> {
            if (Gdx.files.internal(p).exists())
                return new Texture(Gdx.files.internal(p));
            return null;
        });
    }

    public static void disposeStatic() {
        for (final Texture t : spriteCache.values()) {
            if (t != null) {
                t.dispose();
            }
        }
        spriteCache.clear();
    }

    // Hover state
    private int hoveredIndex = -1;

    public InventoryUI() {
    }

    public void render(final SpriteBatch batch, final ShapeRenderer sr, final BitmapFont font, final InventoryUIData data, final boolean isInventoryOpen, final float mx, final float my) {
        if (!isInventoryOpen)
            return;

        this.itemList.clear();
        if (data != null && data.getItems() != null) {
            this.itemList.addAll(data.getItems());
        }

        // Update hover
        hoveredIndex = -1;
        for (int i = 0; i < itemList.size(); i++) {
            final float[] slot = slotPos(i);
            if (mx >= slot[0] && mx <= slot[0] + SLOT_S && my >= slot[1] && my <= slot[1] + SLOT_S) {
                hoveredIndex = i;
                break;
            }
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ── ShapeRenderer ──────────────────────────────────────────────────
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Full-screen dim
        sr.setColor(0f, 0f, 0f, 0.72f);
        sr.rect(0, 0, 800, 600);

        // Main panel
        sr.setColor(0.06f, 0.05f, 0.13f, 0.97f);
        sr.rect(PNL_X, PNL_Y, PNL_W, PNL_H);

        // Header bar
        sr.setColor(0.15f, 0.08f, 0.30f, 1f);
        sr.rect(PNL_X, PNL_Y + PNL_H - 40f, PNL_W, 40f);

        // Footer bar
        sr.setColor(0.10f, 0.05f, 0.20f, 1f);
        sr.rect(PNL_X, PNL_Y, PNL_W, 38f);

        // Slots background
        final int totalSlots = Math.max(10, itemList.size());
        for (int i = 0; i < totalSlots; i++) {
            final float[] pos = slotPos(i);
            final boolean isOccupied = i < itemList.size();
            final boolean isHovered = (i == hoveredIndex);
            if (isHovered) {
                sr.setColor(0.3f, 0.2f, 0.6f, 0.9f);
            } else if (isOccupied) {
                sr.setColor(0.12f, 0.10f, 0.22f, 1f);
            } else {
                sr.setColor(0.08f, 0.07f, 0.14f, 0.7f);
            }
            sr.rect(pos[0], pos[1], SLOT_S, SLOT_S);
        }

        sr.end();

        // Slot borders
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0.4f, 0.3f, 0.8f, 0.6f);
        for (int i = 0; i < totalSlots; i++) {
            final float[] pos = slotPos(i);
            if (i == hoveredIndex) {
                sr.setColor(0.8f, 0.6f, 1.0f, 1f);
            } else if (i < itemList.size()) {
                sr.setColor(0.4f, 0.3f, 0.8f, 0.8f);
            } else {
                sr.setColor(0.2f, 0.18f, 0.35f, 0.5f);
            }
            sr.rect(pos[0], pos[1], SLOT_S, SLOT_S);
        }

        // Panel border outer
        sr.setColor(0.6f, 0.4f, 1.0f, 0.8f);
        sr.rect(PNL_X, PNL_Y, PNL_W, PNL_H);
        sr.setColor(0.3f, 0.2f, 0.5f, 0.4f);
        sr.rect(PNL_X + 1, PNL_Y + 1, PNL_W - 2, PNL_H - 2);
        sr.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── SpriteBatch ────────────────────────────────────────────────────
        batch.begin();
        final GlyphLayout gl = new GlyphLayout();

        // Title
        font.setColor(0.85f, 0.65f, 1.0f, 1f);
        gl.setText(font, "BALO SINH VIEN");
        font.draw(batch, gl, PNL_X + (PNL_W - gl.width) / 2f, PNL_Y + PNL_H - 13f);

        // Item sprites + count
        for (int i = 0; i < itemList.size(); i++) {
            final float[] pos = slotPos(i);
            final InventoryItemData item = itemList.get(i);
            final int count = item.getCount();

            final Texture sprite = getSprite(item.getSpritePath());
            if (sprite != null) {
                final float pad = 4f;
                batch.setColor(Color.WHITE);
                batch.draw(sprite, pos[0] + pad, pos[1] + pad + 10f, SLOT_S - pad * 2, SLOT_S - pad * 2 - 10f);
            }

            // Hotkey number (top-left)
            font.setColor(0.9f, 0.9f, 0.5f, 1f);
            font.draw(batch, String.valueOf(i + 1), pos[0] + 4f, pos[1] + SLOT_S - 3f);

            // Count (bottom-right)
            font.setColor(Color.WHITE);
            final String countStr = "x" + count;
            gl.setText(font, countStr);
            font.draw(batch, countStr, pos[0] + SLOT_S - gl.width - 4f, pos[1] + 14f);
        }

        // Tooltip for hovered item
        if (hoveredIndex >= 0 && hoveredIndex < itemList.size()) {
            final InventoryItemData hItem = itemList.get(hoveredIndex);
            final float tipY = PNL_Y + 54f;
            font.setColor(1f, 0.9f, 0.5f, 1f);
            font.draw(batch, hItem.getName(), PNL_X + 14f, tipY);
            font.setColor(0.75f, 0.75f, 0.75f, 1f);
            font.draw(batch, hItem.getDescription(), PNL_X + 14f, tipY - 17f);
        } else if (itemList.isEmpty()) {
            font.setColor(0.5f, 0.5f, 0.5f, 1f);
            gl.setText(font, "Chua co gi o day ca...");
            font.draw(batch, gl, PNL_X + (PNL_W - gl.width) / 2f, PNL_Y + PNL_H / 2f + 10f);
        }

        // Footer hint
        font.setColor(0.55f, 0.45f, 0.7f, 1f);
        gl.setText(font, "[1-9] Su dung    [I] Dong");
        font.draw(batch, gl, PNL_X + (PNL_W - gl.width) / 2f, PNL_Y + 22f);

        font.setColor(Color.WHITE);
        batch.end();
    }

    /** Returns [x, y] bottom-left of slot i. */
    private float[] slotPos(final int i) {
        final int col = i % COLS;
        final int row = i / COLS;
        final float x = GRID_X + col * (SLOT_S + SLOT_PAD);
        final float y = GRID_Y + row * (SLOT_S + SLOT_PAD);
        return new float[] { x, y };
    }

    public void update(final int keyPressed, final InventoryUIData data, final boolean isInventoryOpen) {
        if (!isInventoryOpen)
            return;

        if (keyPressed > 0 && data != null && data.getItems() != null) {
            final List<InventoryItemData> items = data.getItems();
            final int index = keyPressed - 1;
            if (index >= 0 && index < items.size()) {
                final InventoryItemData item = items.get(index);
                if (item != null) {
                    final GameEvent<String> event = new GameEvent<>(EventType.ITEM_USED, item.getId());
                    EventDispatcher.getInstance().dispatch(event);
                }
            }
        }
    }

    public void dispose() {
    }
}
