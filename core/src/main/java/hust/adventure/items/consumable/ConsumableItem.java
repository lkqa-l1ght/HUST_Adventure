package hust.adventure.items.consumable;

import hust.adventure.entities.base.Character;
import hust.adventure.items.FloatingTextInfo;
import hust.adventure.items.base.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A concrete item that can be consumed. Uses a Consumer function to execute its custom consumption logic.
 */
public class ConsumableItem extends Item implements Consumable {
    private final Consumer<Character> consumptionEffect;
    private final List<FloatingTextInfo> floatingTexts;

    public ConsumableItem(final String id, final String name, final String description, final String spritePath,
            final Consumer<Character> consumptionEffect) {
        this(id, name, description, spritePath, consumptionEffect, Collections.emptyList());
    }

    public ConsumableItem(final String id, final String name, final String description, final String spritePath,
            final Consumer<Character> consumptionEffect, final List<FloatingTextInfo> floatingTexts) {
        super(id, name, description, spritePath);
        if (consumptionEffect == null) {
            throw new IllegalArgumentException("Consumption effect cannot be null");
        }
        if (floatingTexts == null) {
            throw new IllegalArgumentException("Floating texts cannot be null");
        }
        this.consumptionEffect = consumptionEffect;
        this.floatingTexts = Collections.unmodifiableList(new ArrayList<>(floatingTexts));
    }

    @Override
    public void consume(final Character consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("Consumer cannot be null");
        }
        consumptionEffect.accept(consumer);
    }

    public List<FloatingTextInfo> getFloatingTexts() {
        return floatingTexts;
    }
}
