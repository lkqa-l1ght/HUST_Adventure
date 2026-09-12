package hust.adventure.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import hust.adventure.entities.base.MapObject;
import hust.adventure.collision.Collider;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.items.base.Item;

/**
 * Represents a physical item dropped in the world. Renders using a per-item sprite from assets/items/ when available,
 * falling back to a colored rectangle.
 */
public class ItemDrop extends MapObject {
    private Item item;
    private Color color;
    private Texture sprite;

    // ── Bob animation ────────────────────────────────────────────────────────
    private float bobTimer = 0f;
    private static final float BOB_SPEED = 2.5f;
    private static final float BOB_AMOUNT = 3f;

    public ItemDrop() {
        super(0, 0, 60, 60, 24, 24);
    }

    public ItemDrop(float x, float y, Item item, Color color, Texture sprite) {
        super(x, y, 60, 60, 24, 24);
        init(x, y, item, color, sprite);
    }

    public void init(float x, float y, Item item, Color color, Texture sprite) {
        setX(x);
        setY(y);
        this.item = item;
        this.color = color;
        this.sprite = sprite;
        this.bobTimer = (float) (Math.random() * Math.PI * 2); // random phase
        setDestroyed(false);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_DROPPED, this));
    }

    @Override
    public void setCollider(Collider collider) {
        super.setCollider(collider);
        if (collider != null) {
            collider.setListener(other -> {
                ItemPickedUpEvent payload = new ItemPickedUpEvent(item, (MapObject) other);
                GameEvent<ItemPickedUpEvent> event = new GameEvent<>(EventType.ITEM_PICKED_UP, payload);
                EventDispatcher.getInstance().dispatch(event);
                destroy();
            });
        }
    }

    @Override
    public void update(float delta) {
        bobTimer += delta;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (item == null || sprite == null)
            return;

        float bob = (float) Math.sin(bobTimer * BOB_SPEED) * BOB_AMOUNT;
        float drawX = getX() - getWidth() / 2f;
        float drawY = getY() - getHeight() / 2f + bob;

        batch.setColor(Color.WHITE);
        batch.draw(sprite, drawX, drawY, getWidth(), getHeight());
    }

    public void drawDebug(ShapeRenderer sr) {
        if (color != null) {
            sr.setColor(color);
            sr.rect(getX() - getWidth() / 2f, getY() - getHeight() / 2f, getWidth(), getHeight());
        }
    }

    @Override
    public void dispose() {
        // Static cache disposed separately
    }

    public Item getItem() {
        return item;
    }
}
