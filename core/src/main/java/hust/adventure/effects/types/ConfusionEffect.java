package hust.adventure.effects.types;

import hust.adventure.effects.StatusEffect;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.base.StatusFlag;

public class ConfusionEffect implements StatusEffect {
    private float duration;

    public ConfusionEffect(float duration) {
        this.duration = duration;
    }

    @Override
    public void onStart(Character target) {
    }

    @Override
    public void update(Character target, float delta) {
        if (duration > 0) {
            duration -= delta;
        }
    }

    @Override
    public void onEnd(Character target) {
    }

    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    public StatusFlag getFlag() {
        return StatusFlag.CONFUSED;
    }
}
