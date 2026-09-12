package hust.adventure.ui;

/**
 * Interface defining access to UI components. Decouples screens and behaviors from concrete UIManager.
 */
public interface UIProvider {
    HUD getHud();

    InventoryUI getInventoryUI();

    LevelUpUI getLevelUpUI();

    DamageTextManager getDamageTextManager();

    DebugUI getDebugUI();
}
