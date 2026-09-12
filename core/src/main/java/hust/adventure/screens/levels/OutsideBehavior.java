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
 * Behavior class for the Outside area (Map 1).
 * Loads and ticks enemy waves and handles Admission Note gating.
 */
public class OutsideBehavior implements LevelBehavior {
    private static final String COMPLETION_MESSAGE =
            "Đã vượt qua tất cả các Deadline!\nHãy nhặt Giấy Báo Nhập Học và tiến vào Map 2.";

    private WaveManager waveManager;
    private boolean messageTriggered = false;
    private final LevelNotificationBanner banner = new LevelNotificationBanner();

    @Override
    public void init(final PlayScreen context) {
        if (context == null) {
            throw new IllegalArgumentException("PlayScreen context cannot be null");
        }

        final String levelId = context.getConfig().getLevelId();
        final Array<WaveEntry> waves = context.getGame().getWaveDataManager().getWaves(levelId);
        this.waveManager = new WaveManager(waves, context.getEntityFactory());
        if (context.getUIManager() != null && context.getUIManager().getHud() != null) {
            context.getUIManager().getHud().setTimeProvider(this.waveManager);
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

                if (context.getEntityFactory() != null && progress != null && progress.getItemManager() != null) {
                    final ItemManager itemManager = progress.getItemManager();
                    final String keyItemId = resolveRequiredKeyItemId(progress);
                    final Item noteItem = itemManager.getItem(keyItemId);
                    if (noteItem != null) {
                        final Player player = progress.getPlayer();
                        final float spawnX = player != null ? player.getX() + 120f : 400f;
                        final float spawnY = player != null ? player.getY() + 80f : 400f;
                        context.getEntityFactory().createItemDrop(spawnX, spawnY, noteItem, Color.WHITE);
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
        return CampusMap.MAP_1_OUTSIDE.getRequiredKeyItemId();
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
        if (waveManager != null && (!waveManager.isFinished() || (context.getEntityManager() != null && context.getEntityManager().hasActiveEnemies()))) {
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
}
