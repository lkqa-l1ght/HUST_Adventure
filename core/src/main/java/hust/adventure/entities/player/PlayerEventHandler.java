package hust.adventure.entities.player;

import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.items.base.Item;
import hust.adventure.items.consumable.Consumable;

/**
 * Handles all events relevant to the Player, implementing the EventListener interface.
 */
public class PlayerEventHandler implements EventListener {
    private final Player player;

    /**
     * Constructs the event handler and registers it with the EventDispatcher.
     *
     * @param player The player entity instance.
     */
    public PlayerEventHandler(final Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        this.player = player;
        EventDispatcher.getInstance().addListener(EventType.PUZZLE_FAILED, this);
        EventDispatcher.getInstance().addListener(EventType.ITEM_USED, this);
        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, this);
    }

    /**
     * Handles game events relevant to the player.
     *
     * @param event The dispatched event.
     */
    @Override
    public void onEvent(final GameEvent<?> event) {
        if (event.getType() == EventType.PUZZLE_FAILED) {
            final Float damage = (Float) event.getData();
            player.takeDamage(damage);
        } else if (event.getType() == EventType.ITEM_USED) {
            final String itemId = (String) event.getData();
            final Item item = player.getProgressContext() != null
                    && player.getProgressContext().getItemManager() != null
                            ? player.getProgressContext().getItemManager().getItem(itemId)
                            : null;
            if (item instanceof Consumable) {
                if (player.getInventory().removeItem(item, 1)) {
                    ((Consumable) item).consume(player);
                }
            }
        } else if (event.getType() == EventType.ITEM_PICKED_UP) {
            final ItemPickedUpEvent data = (ItemPickedUpEvent) event.getData();
            if (data.getPicker() == player) {
                player.getInventory().addItem(data.getItem(), 1);
            }
        }
    }

    /**
     * Unregisters the handler from the EventDispatcher to prevent memory leaks.
     */
    public void dispose() {
        EventDispatcher.getInstance().removeListener(EventType.PUZZLE_FAILED, this);
        EventDispatcher.getInstance().removeListener(EventType.ITEM_USED, this);
        EventDispatcher.getInstance().removeListener(EventType.ITEM_PICKED_UP, this);
    }
}
