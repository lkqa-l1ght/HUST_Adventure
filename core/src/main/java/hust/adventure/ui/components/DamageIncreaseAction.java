package hust.adventure.ui.components;

import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.player.Player;

public class DamageIncreaseAction implements UpgradeAction {
    private final GameProgressContext progressContext;

    public DamageIncreaseAction(final GameProgressContext progressContext) {
        this.progressContext = progressContext;
    }

    @Override
    public String getName() {
        return "Tăng sát thương";
    }

    @Override
    public String getDescription() {
        return "+100% sát thương cho tất cả vũ khí";
    }

    @Override
    public void execute(Player player) {
        // Nhân đôi damageMultiplier trong ProgressContext → persist qua map transition
        float current = progressContext.getDamageMultiplier();
        progressContext.setDamageMultiplier(current * 2f);
    }
}
