package hust.adventure.screens.levels;

import hust.adventure.events.EventListener;
import hust.adventure.events.GameEvent;
import hust.adventure.progression.MapDirector;
import hust.adventure.screens.PlayScreen;

/**
 * Interface for map-specific game behaviors/scripts.
 */
public interface LevelBehavior extends EventListener {
    default void init(final PlayScreen context) {}
    default void update(final PlayScreen context, final float delta) {}
    default void draw(final PlayScreen context) {}
    default void dispose(final PlayScreen context) {}

    /**
     * Default no-op event handler. Override in behaviors that react to game events.
     *
     * @param event the game event to process
     */
    @Override
    default void onEvent(final GameEvent<?> event) {}

    /**
     * Checks if the level allows transitioning to another map.
     * Delegates to the progression MapDirector by default.
     *
     * @param context the play screen context
     * @return true if transitioning is allowed, false otherwise
     */
    default boolean canTransition(final PlayScreen context) {
        if (context != null && context.getProgressContext() != null) {
            final MapDirector director = context.getProgressContext().getMapDirector();
            if (director != null) {
                return director.canTransition();
            }
        }
        return false;
    }

    /**
     * Checks if a puzzle mini-game is currently active.
     *
     * @return true if a puzzle is active, false otherwise
     */
    default boolean isPuzzleActive() {
        return false;
    }

    /**
     * Checks if auto-attack/weapons auto-fire is allowed in this level/phase.
     *
     * @return true if auto-attack is allowed, false otherwise.
     */
    default boolean isAutoAttackAllowed() {
        return true;
    }

    default void resize(int width, int height) {}
}
