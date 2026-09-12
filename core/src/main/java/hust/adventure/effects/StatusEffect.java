package hust.adventure.effects;

import hust.adventure.entities.base.Character;
import hust.adventure.entities.base.StatusFlag;

/**
 * Interface representing a status effect (buff/debuff) on a BaseActor.
 */
public interface StatusEffect {
    /**
     * Called when the effect starts.
     * 
     * @param target The actor receiving the effect.
     */
    void onStart(Character target);

    /**
     * Called every frame to update the effect logic.
     * 
     * @param target The actor receiving the effect.
     * @param delta  Time since last frame.
     */
    void update(Character target, float delta);

    /**
     * Called when the effect ends.
     * 
     * @param target The actor that had the effect.
     */
    void onEnd(Character target);

    /**
     * @return Current remaining duration of the effect.
     */
    float getDuration();

    /**
     * @return The status flag associated with this effect, or null if none.
     */
    StatusFlag getFlag();
}
