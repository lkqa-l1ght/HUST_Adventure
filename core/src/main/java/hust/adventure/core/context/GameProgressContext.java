package hust.adventure.core.context;

import java.util.Map;
import hust.adventure.gamestate.GameState;
import hust.adventure.inventory.Inventory;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.entities.player.Player;
import hust.adventure.items.base.ItemManager;
import hust.adventure.progression.MapDirector;

/**
 * Interface representing the progress context in the game, decoupling context from concrete game states.
 */
public interface GameProgressContext {
    PlayerStats getPlayerStats();

    void setGameState(GameState newState);

    void update(float delta);

    String getPreviousScreen();

    void setPreviousScreen(String previousScreen);

    float getHp();

    void setHp(float hp);

    float getMaxHp();

    void setMaxHp(float maxHp);

    float getStamina();

    void setStamina(float stamina);

    float getMaxStamina();

    void setMaxStamina(float maxStamina);

    float getMorale();

    void setMorale(float morale);

    int getLevel();

    void setLevel(int level);

    float getExp();

    void setExp(float exp);

    float getExpToNextLevel();

    void setExpToNextLevel(float expToNextLevel);

    Inventory getGlobalInventory();

    boolean isHasNao();

    void setHasNao(boolean hasNao);

    boolean isHasUsb();

    void setHasUsb(boolean hasUsb);

    int getCoffeeCount();

    void setCoffeeCount(int coffeeCount);

    boolean isLibraryCleared();

    void setLibraryCleared(boolean libraryCleared);

    boolean isLabCleared();

    void setLabCleared(boolean labCleared);

    boolean isInventoryOpen();

    void setInventoryOpen(boolean inventoryOpen);

    boolean isShowDebug();

    void setShowDebug(boolean showDebug);

    boolean isGodMode();

    void setGodMode(boolean godMode);

    boolean isFastRun();

    void setFastRun(boolean fastRun);

    boolean isShowHitbox();

    void setShowHitbox(boolean showHitbox);

    float getEnemyTimeScale();

    void setEnemyTimeScale(float enemyTimeScale);

    boolean isLightsOut();

    void setLightsOut(boolean lightsOut);

    float getShowEnemiesTimer();

    void setShowEnemiesTimer(float showEnemiesTimer);

    boolean isEnemyBlinkVisible();

    void setEnemyBlinkVisible(boolean enemyBlinkVisible);

    LevelConfig getCurrentLevelConfig();

    void setCurrentLevelConfig(LevelConfig currentLevelConfig);

    float getDamageMultiplier();

    void setDamageMultiplier(float damageMultiplier);

    Player getPlayer();

    void setPlayer(Player player);

    Map<String, Integer> getWeaponLevels();

    Map<String, Integer> getGearLevels();

    boolean isAutoAttackAllowed();

    void setAutoAttackAllowed(boolean allowed);

    void reset();

    void saveCheckpoint();

    void restoreCheckpoint();

    boolean hasCheckpoint();

    ItemManager getItemManager();

    /**
     * Gets the active campus progression director.
     *
     * @return the MapDirector managing stages and gating.
     */
    MapDirector getMapDirector();

    /**
     * Sets the campus progression director.
     *
     * @param mapDirector the director instance to set.
     */
    void setMapDirector(MapDirector mapDirector);

    /**
     * Gets the required key item identifier for the active campus stage from the progression director.
     *
     * @return the key item identifier, or null if no key item is required or map director is absent.
     */
    String getCurrentStageKeyItemId();
}
