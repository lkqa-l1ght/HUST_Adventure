package hust.adventure.ui.components;

import hust.adventure.entities.player.Player;

public interface UpgradeAction {
    String getName();

    String getDescription();

    void execute(Player player);
}
