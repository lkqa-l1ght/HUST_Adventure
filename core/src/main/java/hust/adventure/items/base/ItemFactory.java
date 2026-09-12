package hust.adventure.items.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import hust.adventure.effects.types.ConfusionEffect;
import hust.adventure.effects.types.RegenEffect;
import hust.adventure.effects.types.SpeedBoostEffect;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.player.Player;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.items.FloatingTextInfo;
import hust.adventure.items.consumable.ConsumableItem;
import hust.adventure.items.consumable.ConsumableItemConfig;
import hust.adventure.items.consumable.EffectConfig;
import hust.adventure.items.consumable.FloatingTextConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Factory class for creating Item instances from their configurations. Uses a registry-based provider pattern (similar
 * to BehaviorRegistry) for mapping effects.
 */
public class ItemFactory {
    private static final ObjectMap<String, EffectProvider> effectProviders = new ObjectMap<>();

    static {
        effectProviders.put("heal", config -> consumer -> consumer.heal(config.getValue()));
        effectProviders.put("restore_stamina", config -> consumer -> {
            if (consumer instanceof Player) {
                ((Player) consumer).restoreStamina(config.getValue());
            }
        });
        effectProviders.put("speed_boost", config -> consumer -> consumer.getStatusEffectManager()
                .addEffect(new SpeedBoostEffect(config.getDuration(), config.getMultiplier())));
        effectProviders.put("regen", config -> consumer -> consumer.getStatusEffectManager()
                .addEffect(new RegenEffect(config.getDuration(), config.getValue())));
        effectProviders.put("confusion", config -> consumer -> consumer.getStatusEffectManager()
                .addEffect(new ConfusionEffect(config.getDuration())));
        effectProviders.put("coffee", config -> consumer -> EventDispatcher.getInstance()
                .dispatch(new GameEvent<>(EventType.ITEM_USED, "coffee")));
    }

    /**
     * Creates an Item instance based on the provided configuration.
     *
     * @param config the configuration of the item
     * @return the created Item instance
     */
    public Item createItem(final ItemConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }

        if (config instanceof ConsumableItemConfig) {
            return createConsumableItem((ConsumableItemConfig) config);
        } else {
            return new Item(config.getId(), config.getName(), config.getDescription(), config.getSpritePath());
        }
    }

    private ConsumableItem createConsumableItem(final ConsumableItemConfig config) {
        final Consumer<Character> compositeEffect = buildEffect(config.getEffects());
        final List<FloatingTextInfo> floatingTexts = buildFloatingTexts(config.getFloatingTexts());
        return new ConsumableItem(config.getId(), config.getName(), config.getDescription(), config.getSpritePath(),
                compositeEffect, floatingTexts);
    }

    private Consumer<Character> buildEffect(final Array<EffectConfig> effects) {
        Consumer<Character> compositeEffect = consumer -> {
        };
        if (effects == null) {
            return compositeEffect;
        }

        for (final EffectConfig effectConfig : effects) {
            final Consumer<Character> effectConsumer = createSingleEffect(effectConfig);
            if (effectConsumer != null) {
                compositeEffect = compositeEffect.andThen(effectConsumer);
            }
        }
        return compositeEffect;
    }

    private Consumer<Character> createSingleEffect(final EffectConfig config) {
        if (config == null || config.getEffect() == null) {
            return null;
        }

        final String effectType = config.getEffect().toLowerCase();
        final EffectProvider provider = effectProviders.get(effectType);
        if (provider != null) {
            return provider.create(config);
        } else {
            Gdx.app.error("ItemFactory", "Unknown effect type: " + effectType);
            return null;
        }
    }

    private List<FloatingTextInfo> buildFloatingTexts(final Array<FloatingTextConfig> configs) {
        final List<FloatingTextInfo> list = new ArrayList<>();
        if (configs == null) {
            return list;
        }

        for (final FloatingTextConfig config : configs) {
            if (config != null) {
                Color color;
                try {
                    color = Color.valueOf(config.getColorHex());
                } catch (final IllegalArgumentException e) {
                    color = Color.WHITE;
                }
                list.add(new FloatingTextInfo(config.getText(), color, config.getDuration(), config.getOffsetX(),
                        config.getStartVy()));
            }
        }
        return list;
    }

    /**
     * Functional interface representing a provider for item effects.
     */
    public interface EffectProvider {
        /**
         * Creates a consumption effect consumer.
         *
         * @param config the configuration of the effect
         * @return the consumer representing the effect logic
         */
        Consumer<Character> create(EffectConfig config);
    }
}
