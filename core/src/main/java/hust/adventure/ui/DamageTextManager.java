package hust.adventure.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import hust.adventure.events.EntityDamagedEvent;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.utils.GamePools;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.player.Player;
import hust.adventure.items.FloatingTextInfo;
import hust.adventure.items.base.Item;
import hust.adventure.items.consumable.ConsumableItem;

public class DamageTextManager implements EventListener {
    private final Array<DamageText> activeTexts;
    private final GameProgressContext progressContext;

    public DamageTextManager(final GameProgressContext progressContext) {
        this.progressContext = progressContext;
        activeTexts = new Array<>();
        EventDispatcher.getInstance().addListener(EventType.ENTITY_DAMAGED, this);
        EventDispatcher.getInstance().addListener(EventType.ITEM_USED, this);
    }

    public void update(float delta) {
        for (int i = activeTexts.size - 1; i >= 0; i--) {
            DamageText dt = activeTexts.get(i);
            dt.update(delta);
            if (dt.isExpired()) {
                activeTexts.removeIndex(i);
                GamePools.free(dt);
            }
        }
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        for (DamageText dt : activeTexts) {
            font.setColor(dt.getColor());
            font.draw(batch, dt.getText(), dt.getPosition().x, dt.getPosition().y);
        }
        font.setColor(Color.WHITE); // Reset color
    }

    @Override
    public void onEvent(GameEvent<?> event) {
        if (event.getType() == EventType.ENTITY_DAMAGED) {
            EntityDamagedEvent data = (EntityDamagedEvent) event.getData();

            float vx = MathUtils.random(-30f, 30f);
            float vy = MathUtils.random(50f, 100f);
            Color color = data.isCrit() ? Color.YELLOW : Color.WHITE;
            String text = String.valueOf((int) data.getAmount());

            DamageText dt = GamePools.obtain(DamageText.class);
            // Spawn slightly above the entity
            dt.init(data.getEntity().getX(), data.getEntity().getY() + data.getEntity().getHeight() / 2, vx, vy, text,
                    color, 1.0f);
            activeTexts.add(dt);
        } else if (event.getType() == EventType.ITEM_USED) {
            final String itemId = (String) event.getData();
            final Item item = progressContext != null && progressContext.getItemManager() != null
                    ? progressContext.getItemManager().getItem(itemId)
                    : null;
            if (item != null) {
                final Player player = progressContext != null ? progressContext.getPlayer() : null;
                if (player != null) {
                    showFloatingText(player, "Used: " + item.getName(), Color.WHITE, 1.2f, 0f, 60f);

                    if (item instanceof ConsumableItem) {
                        for (final FloatingTextInfo ftInfo : ((ConsumableItem) item).getFloatingTexts()) {
                            showFloatingText(player, ftInfo.getText(), ftInfo.getColor(), ftInfo.getDuration(),
                                    ftInfo.getOffsetX(), ftInfo.getStartVy());
                        }
                    }
                }
            }
        }
    }

    private void showFloatingText(Player player, String text, Color color, float duration, float offsetX,
            float startVy) {
        float vx = MathUtils.random(-15f, 15f);
        float vy = startVy + MathUtils.random(-5f, 5f);
        DamageText dt = GamePools.obtain(DamageText.class);
        dt.init(player.getX() + offsetX, player.getY() + player.getHeight() / 2, vx, vy, text, color, duration);
        activeTexts.add(dt);
    }

    public void dispose() {
        EventDispatcher.getInstance().removeListener(EventType.ENTITY_DAMAGED, this);
        EventDispatcher.getInstance().removeListener(EventType.ITEM_USED, this);
        for (DamageText dt : activeTexts) {
            GamePools.free(dt);
        }
        activeTexts.clear();
    }
}
