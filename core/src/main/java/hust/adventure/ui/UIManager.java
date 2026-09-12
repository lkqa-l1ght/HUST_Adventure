package hust.adventure.ui;

import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import com.badlogic.gdx.utils.Disposable;

import hust.adventure.entities.player.Player;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.items.base.Item;

/**
 * Manages UI components and their visibility.
 */
public class UIManager implements UIProvider, Disposable {
    private final HUD hud;
    private final StatusEffectsHUD statusEffectsHUD;
    private final InventoryUI inventoryUI;
    private final LevelUpUI levelUpUI;
    private final DamageTextManager damageTextManager;
    private final DebugUI debugUI;
    private final GameProgressContext progressContext;

    private final List<InventoryItemData> itemDataList = new ArrayList<>();

    public UIManager(final GameProgressContext progressContext) {
        this.progressContext = progressContext;
        this.hud = new HUD();
        this.statusEffectsHUD = new StatusEffectsHUD();
        this.inventoryUI = new InventoryUI();
        this.levelUpUI = new LevelUpUI();
        this.damageTextManager = new DamageTextManager(progressContext);
        this.debugUI = new DebugUI();
    }

    public void update(final float delta, final Player player, final Consumer<Integer> choiceCallback) {
        damageTextManager.update(delta);

        final int keyPressed = (player != null && player.getController() != null)
                ? player.getController().getJustPressedNum()
                : 0;

        itemDataList.clear();
        if (player != null && player.getInventory() != null) {
            for (final Entry<Item, Integer> entry : player.getInventory().getReadOnlyItems().entrySet()) {
                final Item item = entry.getKey();
                itemDataList.add(new InventoryItemData(item.getId(), item.getName(), item.getDescription(),
                        item.getSpritePath(), entry.getValue()));
            }
        }
        final InventoryUIData invData = new InventoryUIData(itemDataList);
        inventoryUI.update(keyPressed, invData, progressContext.isInventoryOpen());

        levelUpUI.update(keyPressed, choiceCallback);
    }

    public HUD getHud() {
        return hud;
    }

    public StatusEffectsHUD getStatusEffectsHUD() {
        return statusEffectsHUD;
    }

    public InventoryUI getInventoryUI() {
        return inventoryUI;
    }

    public LevelUpUI getLevelUpUI() {
        return levelUpUI;
    }

    public DamageTextManager getDamageTextManager() {
        return damageTextManager;
    }

    public DebugUI getDebugUI() {
        return debugUI;
    }

    @Override
    public void dispose() {
        hud.dispose();
        inventoryUI.dispose();
        levelUpUI.dispose();
        damageTextManager.dispose();
    }
}
