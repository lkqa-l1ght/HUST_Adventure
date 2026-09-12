package hust.adventure.entities.player.input;

import hust.adventure.input.InputReader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.entities.player.Player;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.MapTransitionData;
import hust.adventure.gamestate.PlayMode;
import hust.adventure.items.base.Item;
import hust.adventure.ui.DebugOption;
import hust.adventure.ui.DebugOptionRegistry;
import hust.adventure.ui.SelectionMode;
import hust.adventure.ui.UIManager;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponFactory;
import hust.adventure.items.gear.Gear;
import hust.adventure.items.gear.GearFactory;
import hust.adventure.events.ExpGainedEvent;
import hust.adventure.core.data.LevelConfig;
import java.util.Optional;

/**
 * Handles debug shortcut keys (F4-F8), manages selection states for debug options, and executes corresponding debug
 * actions such as map transition, item spawning, and monster spawning.
 */
public class DebugInputHandler {
    private final UIManager uiManager;
    private final EntityFactory entityFactory;
    private final GameAssetManager assetManager;
    private final InputReader inputReader;
    private final WeaponFactory weaponFactory;
    private final GearFactory gearFactory;
    private final DebugOptionRegistry debugOptionRegistry;
    private final GameProgressContext progressContext;

    // States for debug selection
    private SelectionMode activeMode = SelectionMode.NONE;
    private SelectionMode previousMode = SelectionMode.NONE;
    private int selectedIndex = 0;

    /**
     * Constructs a new DebugInputHandler.
     *
     * @param uiManager     the UI manager
     * @param entityFactory the entity factory
     * @param assetManager  the game asset manager
     * @param inputReader   the input reader
     * @param weaponFactory the weapon factory for equipping weapons
     * @param gearFactory   the gear factory for equipping gears
     */
    public DebugInputHandler(final UIManager uiManager, final EntityFactory entityFactory,
            final GameAssetManager assetManager, final InputReader inputReader, final WeaponFactory weaponFactory,
            final GearFactory gearFactory, final DebugOptionRegistry debugOptionRegistry,
            final GameProgressContext progressContext) {
        if (inputReader == null) {
            throw new IllegalArgumentException("inputReader cannot be null");
        }
        if (progressContext == null) {
            throw new IllegalArgumentException("progressContext cannot be null");
        }
        this.uiManager = uiManager;
        this.entityFactory = entityFactory;
        this.assetManager = assetManager;
        this.inputReader = inputReader;
        this.weaponFactory = weaponFactory;
        this.gearFactory = gearFactory;
        this.debugOptionRegistry = debugOptionRegistry;
        this.progressContext = progressContext;
    }

    /**
     * Handles debug input. Returns a new PlayMode if the state changes, null otherwise.
     *
     * @param player       the current player (to get spawn coordinates)
     * @param currentState the current PlayMode state
     * @return the new PlayMode if it changes, null if unchanged
     */
    public PlayMode handleDebugInput(final Player player, final PlayMode currentState) {
        if (isActive()) {
            final DebugOption selected = handleSelectionInput(player);
            if (selected != null) {
                executeDebugAction(previousMode, selected, player);
                return PlayMode.RUNNING;
            } else if (!isActive()) {
                // Cancelled selection via ESC
                return PlayMode.RUNNING;
            }
            return null; // Suppress other actions while selection is active
        }

        if (inputReader.isKeyJustPressed(Input.Keys.F4)) {
            progressContext.setGodMode(!progressContext.isGodMode());
        }
        if (inputReader.isKeyJustPressed(Input.Keys.F5)) {
            progressContext.setFastRun(!progressContext.isFastRun());
        }
        if (inputReader.isKeyJustPressed(Input.Keys.F6)) {
            startSelection(SelectionMode.MAP);
            return PlayMode.IN_UI;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.F7)) {
            startSelection(SelectionMode.ITEM);
            return PlayMode.IN_UI;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.F8)) {
            startSelection(SelectionMode.MONSTER);
            return PlayMode.IN_UI;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.F9)) {
            startSelection(SelectionMode.EQUIP);
            return PlayMode.IN_UI;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.F10)) {
            final float needed = progressContext.getExpToNextLevel() - progressContext.getExp();
            final ExpGainedEvent payload = new ExpGainedEvent(needed);
            final GameEvent<ExpGainedEvent> event = new GameEvent<>(EventType.EXP_GAINED, payload);
            EventDispatcher.getInstance().dispatch(event);
            return PlayMode.IN_UI;
        }

        return null;
    }

    /**
     * Starts the selection mode.
     *
     * @param mode the selection mode to start
     */
    public void startSelection(final SelectionMode mode) {
        this.activeMode = mode;
        this.previousMode = mode;
        this.selectedIndex = 0;
    }

    /**
     * Cancels the current selection mode.
     */
    public void cancelSelection() {
        this.activeMode = SelectionMode.NONE;
    }

    /**
     * Cancels the current debug selection mode.
     */
    public void cancelDebug() {
        cancelSelection();
    }

    /**
     * Updates selection menu input.
     *
     * @param player the player instance to get dynamic options
     * @return selected DebugOption if confirmed, null otherwise.
     */
    private DebugOption handleSelectionInput(final Player player) {
        final DebugOption[] currentOptions = getCurrentOptions(player);
        if (!isActive() || currentOptions == null || currentOptions.length == 0) {
            return null;
        }

        if (inputReader.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + currentOptions.length) % currentOptions.length;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % currentOptions.length;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.ESCAPE)) {
            cancelSelection();
            return null;
        }
        if (inputReader.isKeyJustPressed(Input.Keys.ENTER)) {
            final DebugOption selection = currentOptions[selectedIndex];
            cancelSelection();
            return selection;
        }

        return null;
    }

    /**
     * Checks if a debug selection mode is currently active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return activeMode != SelectionMode.NONE;
    }

    /**
     * Gets the current active selection mode.
     *
     * @return the active SelectionMode
     */
    public SelectionMode getActiveMode() {
        return activeMode;
    }

    /**
     * Gets the previous selection mode.
     *
     * @return the previous SelectionMode
     */
    public SelectionMode getPreviousMode() {
        return previousMode;
    }

    /**
     * Gets the current selected index.
     *
     * @return the selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Gets the array of debug options for the current active selection mode.
     *
     * @param player the player instance
     * @return an array of DebugOption
     */
    public DebugOption[] getCurrentOptions(final Player player) {
        return debugOptionRegistry.getOptions(activeMode, player);
    }

    private void executeDebugAction(final SelectionMode mode, final DebugOption option, final Player player) {
        if (mode == null || option == null) {
            return;
        }
        switch (mode) {
        case MAP:
            executeMapAction(option);
            break;
        case ITEM:
            executeItemAction(option, player);
            break;
        case MONSTER:
            executeMonsterAction(option, player);
            break;
        case EQUIP:
            executeEquipAction(option, player);
            break;
        default:
            break;
        }
    }

    private void executeMapAction(final DebugOption option) {
        final String targetMap = option.getId();
        float spawnX = 400f;
        float spawnY = 400f;
        if (debugOptionRegistry != null) {
            final Optional<LevelConfig> levelConfigOpt = debugOptionRegistry.getLevelConfigByMapPath(targetMap);
            if (levelConfigOpt.isPresent()) {
                final LevelConfig template = levelConfigOpt.get();
                spawnX = template.getSpawnX();
                spawnY = template.getSpawnY();
            } else {
                Gdx.app.error("DebugMode", "Failed to lookup LevelConfig for map: " + targetMap + ", falling back to (400, 400)");
            }
        }
        final MapTransitionData data = new MapTransitionData(targetMap, spawnX, spawnY);
        final GameEvent<MapTransitionData> event = new GameEvent<>(EventType.MAP_TRANSITION, data);
        EventDispatcher.getInstance().dispatch(event);
    }

    private void executeItemAction(final DebugOption option, final Player player) {
        final String itemId = option.getId();
        final Item item = progressContext != null && progressContext.getItemManager() != null
                ? progressContext.getItemManager().getItem(itemId)
                : null;
        if (item != null && player != null) {
            entityFactory.createItemDrop(player.getX() + 32f, player.getY(), item, Color.WHITE);
        }
    }

    private void executeMonsterAction(final DebugOption option, final Player player) {
        final String enemyId = option.getId();
        try {
            if (player != null) {
                entityFactory.createEnemy(enemyId, player.getX() + 64f, player.getY());
            }
        } catch (final Exception e) {
            Gdx.app.log("DebugMode", "Failed to spawn enemy: " + enemyId, e);
        }
    }

    private void executeEquipAction(final DebugOption option, final Player player) {
        if (player == null) {
            return;
        }
        final String optionId = option.getId();
        if (optionId.startsWith("weapon_")) {
            final String weaponId = optionId.substring("weapon_".length());
            BaseWeapon equipped = null;
            if (player.getWeaponManager() != null) {
                for (final BaseWeapon w : player.getWeaponManager().getWeapons()) {
                    if (w.getId().equalsIgnoreCase(weaponId)) {
                        equipped = w;
                        break;
                    }
                }
            }
            if (equipped != null) {
                equipped.unequip(player);
            } else {
                if (weaponFactory != null) {
                    final BaseWeapon weapon = weaponFactory.createWeapon(weaponId, player);
                    if (weapon != null) {
                        weapon.equip(player);
                    }
                }
            }
        } else if (optionId.startsWith("gear_")) {
            final String gearId = optionId.substring("gear_".length());
            final Gear equipped = player.getGearManager() != null ? player.getGearManager().getGear(gearId) : null;
            if (equipped != null) {
                equipped.unequip(player);
            } else {
                if (gearFactory != null) {
                    final Gear gear = gearFactory.createGear(gearId);
                    if (gear != null) {
                        gear.equip(player);
                    }
                }
            }
        }
    }
}
