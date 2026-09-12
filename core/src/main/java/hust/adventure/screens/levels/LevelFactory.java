package hust.adventure.screens.levels;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import hust.adventure.HustGame;
import hust.adventure.core.data.LevelConfig;
import hust.adventure.screens.BaseScreen;
import hust.adventure.screens.PlayScreen;

/**
 * Factory for creating specific level screens based on configuration.
 */
public class LevelFactory {
    private static final Map<String, Supplier<LevelBehavior>> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("FINAL_OUTSIDE", OutsideBehavior::new);
        REGISTRY.put("MAP_1", OutsideBehavior::new);
        REGISTRY.put("TANG_1", Floor1Behavior::new);
        REGISTRY.put("LIBRARY", LibraryBehavior::new);
        REGISTRY.put("LAB", LabBehavior::new);
        REGISTRY.put("BOSS_ROOM", BossFightBehavior::new);
    }

    public LevelFactory() {
    }

    public BaseScreen createLevel(final HustGame game, final LevelConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("LevelConfig cannot be null");
        }
        if (game == null) {
            throw new IllegalArgumentException("HustGame cannot be null");
        }

        final String levelId = config.getLevelId();
        final Supplier<LevelBehavior> supplier = levelId != null ? REGISTRY.get(levelId.toUpperCase()) : null;
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown or unregistered LevelID: " + config.getLevelId());
        }

        return new PlayScreen(game, config, supplier.get());
    }
}
