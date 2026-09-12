package hust.adventure.ui.components;

import hust.adventure.entities.player.Player;

public class HealAction implements UpgradeAction {
    @Override
    public String getName() {
        return "Heal";
    }

    @Override
    public String getDescription() {
        return "Restores 50 HP";
    }

    @Override
    public void execute(Player player) {
        player.heal(50f);
    }
}
