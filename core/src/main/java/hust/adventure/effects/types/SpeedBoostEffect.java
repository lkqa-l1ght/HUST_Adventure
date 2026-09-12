package hust.adventure.effects.types;

import hust.adventure.effects.StatusEffect;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.base.StatusFlag;

public class SpeedBoostEffect implements StatusEffect {
    private float duration;
    private final float multiplier;

    public SpeedBoostEffect(float duration, float multiplier) {
        this.duration = duration;
        this.multiplier = multiplier;
    }

    @Override
    public void onStart(Character target) {
        target.setSpeedMultiplier(multiplier);
    }

    @Override
    public void update(Character target, float delta) {
        if (duration > 0) {
            duration -= delta;
        }
    }

    @Override
    public void onEnd(Character target) {
        if (target.getStatusEffectManager().getEffectCount(StatusFlag.SPEED_BOOSTED) <= 1) {
            target.setSpeedMultiplier(1.0f);
        }
    }

    @Override
    public float getDuration() {
        return duration;
    }

    @Override
    public StatusFlag getFlag() {
        return StatusFlag.SPEED_BOOSTED;
    }
}
