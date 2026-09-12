package hust.adventure.effects.types;

import hust.adventure.effects.StatusEffect;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.base.StatusFlag;

public class RegenEffect implements StatusEffect {
    private float duration;
    private final float amountPerSecond;

    public RegenEffect(float duration, float amountPerSecond) {
        this.duration = duration;
        this.amountPerSecond = amountPerSecond;
    }

    @Override
    public void onStart(Character target) {
    }

    @Override
    public void update(Character target, float delta) {
        if (duration > 0) {
            target.heal(amountPerSecond * delta);
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
        return StatusFlag.REGEN_HP;
    }
}
