package hust.adventure.screens.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.player.Player;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;
import hust.adventure.progression.CampusMap;
import hust.adventure.progression.MapDirector;
import hust.adventure.screens.PlayScreen;
import hust.adventure.wave.WaveEntry;
import hust.adventure.wave.WaveManager;

/**
 * Behavior class for Floor 1 (Map 2 / Tang 1).
 * Coordinates lecture hall classroom Deadlines, coffee items, and Lecture Notes gating.
 */
public class Floor1Behavior implements LevelBehavior {
    private static final String COMPLETION_MESSAGE =
            "Đã vượt qua tất cả các Deadline!\nHãy nhặt Bài Giảng và tiến vào Map 3.";

    private WaveManager waveManager;
    private boolean messageTriggered = false;
    private final LevelNotificationBanner banner = new LevelNotificationBanner();
    private boolean notesSpawned = false;

    @Override
    public void init(final PlayScreen context) {
        if (context == null) {
            throw new IllegalArgumentException("PlayScreen context cannot be null");
        }

        final GameProgressContext progress = context.getProgressContext();
        final ItemManager im = progress != null ? progress.getItemManager() : null;

        if (im != null && context.getEntityFactory() != null) {
            // Create ambient Coffee Items in lecture hall
            context.getEntityFactory().createItemDrop(300f, 150f, im.getItem("coffee_den"), Color.BROWN);
            context.getEntityFactory().createItemDrop(350f, 180f, im.getItem("coffee_sua"), Color.YELLOW);

            // Create ambient NPCs
            context.getEntityFactory().createStaticNPC(200f, 300f, "Guard", Color.BLUE);
            context.getEntityFactory().createStaticNPC(600f, 300f, "Staff", Color.CYAN);
        }

        final String levelId = context.getConfig().getLevelId();
        final Array<WaveEntry> waves = context.getGame().getWaveDataManager().getWaves(levelId);
        if (waves != null && waves.size > 0) {
            this.waveManager = new WaveManager(waves, context.getEntityFactory());
            if (context.getUIManager() != null && context.getUIManager().getHud() != null) {
                context.getUIManager().getHud().setTimeProvider(this.waveManager);
            }
        }

        if (context.getGame() != null && context.getGame().getAssetManager() != null) {
            this.banner.setTexture(context.getGame().getAssetManager().getTexture(AssetPaths.UI_TEXT_BOX));
        }
    }

    @Override
    public void update(final PlayScreen context, final float delta) {
        if (waveManager != null) {
            waveManager.update(delta, context.getCamera());
            if (waveManager.isFinished() && !context.getEntityManager().hasActiveEnemies() && !messageTriggered) {
                messageTriggered = true;
                banner.show(COMPLETION_MESSAGE);

                final GameProgressContext progress = context.getProgressContext();
                if (progress != null && progress.getMapDirector() != null) {
                    progress.getMapDirector().onDeadlinesCleared();
                }

                if (!notesSpawned && context.getEntityFactory() != null && progress != null) {
                    notesSpawned = true;
                    final ItemManager itemManager = progress.getItemManager();
                    if (itemManager != null) {
                        final String keyItemId = resolveRequiredKeyItemId(progress);
                        final Item notesItem = itemManager.getItem(keyItemId);
                        if (notesItem != null) {
                            final Player player = progress.getPlayer();
                            final float spawnX = player != null ? player.getX() + 100f : 500f;
                            final float spawnY = player != null ? player.getY() + 60f : 300f;
                            context.getEntityFactory().createItemDrop(spawnX, spawnY, notesItem, Color.WHITE);
                        }
                    }
                }
            }
        }

        banner.update(delta);
    }

    /**
     * Resolves the required key item identifier for this behavior, preferring the progress context.
     *
     * @param progress the game progress context, or null.
     * @return the resolved key item identifier.
     */
    public String resolveRequiredKeyItemId(final GameProgressContext progress) {
        if (progress != null) {
            final String stageItemId = progress.getCurrentStageKeyItemId();
            if (stageItemId != null) {
                return stageItemId;
            }
        }
        return CampusMap.MAP_2_FLOOR_1.getRequiredKeyItemId();
    }

    @Override
    public void draw(final PlayScreen context) {
        banner.draw(context);
    }

    public LevelNotificationBanner getBanner() {
        return banner;
    }

    @Override
    public boolean canTransition(final PlayScreen context) {
        if (!LevelBehavior.super.canTransition(context)) {
            return false;
        }

        if (waveManager != null && (!waveManager.isFinished() || context.getEntityManager().hasActiveEnemies())) {
            return false;
        }

        return true;
    }

    @Override
    public void dispose(final PlayScreen context) {
        if (context != null && context.getUIManager() != null && context.getUIManager().getHud() != null) {
            context.getUIManager().getHud().setTimeProvider(null);
        }
    }

    @Override
    public boolean isAutoAttackAllowed() {
        if (waveManager != null && !waveManager.isFinished()) {
            return true;
        }
        return false;
    }
}
