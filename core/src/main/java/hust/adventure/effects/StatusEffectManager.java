package hust.adventure.effects;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.base.StatusFlag;

/**
 * Manages active status effects for a specific BaseActor.
 */
public class StatusEffectManager {
    private final Character owner;
    private final Array<StatusEffect> activeEffects;
    private final ObjectIntMap<StatusFlag> flagCounts;

    public StatusEffectManager(final Character owner) {
        if (owner == null)
            throw new IllegalArgumentException("Owner cannot be null");
        this.owner = owner;
        this.activeEffects = new Array<>();
        this.flagCounts = new ObjectIntMap<>();
    }

    /**
     * Adds a new effect to the actor.
     * 
     * @param effect The effect to add.
     */
    public void addEffect(final StatusEffect effect) {
        if (effect == null)
            throw new IllegalArgumentException("Effect cannot be null");

        activeEffects.add(effect);
        effect.onStart(owner);

        final StatusFlag flag = effect.getFlag();
        if (flag != null) {
            flagCounts.put(flag, flagCounts.get(flag, 0) + 1);
        }
    }

    /**
     * Updates all active effects and handles expiration.
     * 
     * @param delta Time since last frame.
     */
    public void update(final float delta) {
        for (int i = activeEffects.size - 1; i >= 0; i--) {
            final StatusEffect effect = activeEffects.get(i);
            effect.update(owner, delta);

            if (effect.getDuration() <= 0) {
                removeEffect(i);
            }
        }
    }

    private void removeEffect(final int index) {
        final StatusEffect effect = activeEffects.removeIndex(index);
        effect.onEnd(owner);

        final StatusFlag flag = effect.getFlag();
        if (flag != null) {
            final int count = flagCounts.get(flag, 0);
            if (count > 1) {
                flagCounts.put(flag, count - 1);
            } else {
                flagCounts.remove(flag, 0);
            }
        }
    }

    /**
     * Checks if a status flag is currently active.
     * 
     * @param flag The flag to check.
     * @return True if active.
     */
    public boolean hasStatus(final StatusFlag flag) {
        return flagCounts.containsKey(flag);
    }

    /**
     * Gets the count of active effects for a specific status flag.
     * 
     * @param flag The flag to check.
     * @return The count of active effects.
     */
    public int getEffectCount(final StatusFlag flag) {
        if (flag == null) {
            return 0;
        }
        return flagCounts.get(flag, 0);
    }

    /**
     * Clears all active effects. Used for object pooling reset.
     */
    public void clear() {
        // Proper cleanup: call onEnd for all effects before clearing
        for (int i = activeEffects.size - 1; i >= 0; i--) {
            activeEffects.get(i).onEnd(owner);
        }
        activeEffects.clear();
        flagCounts.clear();
    }
}
