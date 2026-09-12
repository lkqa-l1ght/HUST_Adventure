package hust.adventure.screens.levels;

import com.badlogic.gdx.graphics.Color;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.progression.MapDirector;
import hust.adventure.screens.PlayScreen;
import hust.adventure.ui.puzzle.MemoryCardPuzzle;
import hust.adventure.ui.puzzle.PuzzleSequencer;
import hust.adventure.ui.puzzle.SimonPuzzle;
import hust.adventure.ui.puzzle.SpeedMathPuzzle;

/**
 * Behavior for the Library level (Map 3).
 * Sequence:
 * 1. Sequential Puzzle games (Simon -> Memory Card -> Speed Math) managed by PuzzleSequencer.
 * 2. Puzzle trials suppress weapon Auto-Attacks.
 * 3. Completion -> lifts attack suppression and spawns the Library Guardian boss.
 * 4. Boss defeat -> spawns "brain" (Awakened Brain) item drop.
 * 5. Securing the brain unlocks the portal to Map 4 (Computer Lab).
 */
public class LibraryBehavior implements LevelBehavior {

    // Midpoint of Library map (1344 x 768)
    private static final float CENTER_X = 672f;
    private static final float CENTER_Y = 384f;

    // Boss spawn location
    private static final float BOSS_SPAWN_X = 672f;
    private static final float BOSS_SPAWN_Y = 480f;

    private PlayScreen context;
    private PuzzleSequencer sequencer;
    private Enemy libraryBoss;

    private boolean puzzleSolved = false;
    private boolean bossSpawned = false;
    private boolean brainSpawned = false;
    private boolean libraryCleared = false;

    @Override
    public void init(final PlayScreen ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("PlayScreen context cannot be null");
        }
        this.context = ctx;

        // Create the sequencer and add the three mini-games
        sequencer = new PuzzleSequencer();
        sequencer.addPuzzle(new SimonPuzzle());
        sequencer.addPuzzle(new MemoryCardPuzzle());
        sequencer.addPuzzle(new SpeedMathPuzzle());

        sequencer.init(ctx);

        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, this);
    }

    @Override
    public void update(final PlayScreen ctx, final float delta) {
        if (libraryCleared) {
            return;
        }

        if (!puzzleSolved) {
            // Update puzzle sequencer
            sequencer.update(delta);
            if (sequencer.isAllSolved()) {
                onPuzzleSolved();
            }
        } else {
            // Combat state: Wait for boss defeat to notify director and spawn Awakened Brain
            if (bossSpawned && !brainSpawned && libraryBoss != null
                    && (libraryBoss.isDead() || libraryBoss.isDestroyed())) {
                final GameProgressContext progress = ctx.getProgressContext();
                if (progress != null && progress.getMapDirector() != null) {
                    progress.getMapDirector().onBossDefeated();
                }

                final float bx = libraryBoss.isDestroyed() ? CENTER_X : libraryBoss.getX();
                final float by = libraryBoss.isDestroyed() ? CENTER_Y : libraryBoss.getY();
                if (ctx.getEntityFactory() != null && progress != null && progress.getItemManager() != null) {
                    ctx.getEntityFactory().createItemDrop(bx, by,
                            progress.getItemManager().getItem("brain"), Color.CYAN);
                }
                brainSpawned = true;
            }
        }
    }

    @Override
    public void draw(final PlayScreen ctx) {
        if (!puzzleSolved && sequencer != null) {
            sequencer.render(ctx.getShapeRenderer(), ctx.getBatch());
        }
    }

    private void onPuzzleSolved() {
        if (puzzleSolved) {
            return;
        }
        puzzleSolved = true;

        final GameProgressContext progress = context != null ? context.getProgressContext() : null;
        if (progress != null && progress.getMapDirector() != null) {
            progress.getMapDirector().onAcademicChallengesCleared();
        }

        // Spawn Library Guardian boss via EntityFactory (SSOT)
        if (context != null && context.getEntityFactory() != null) {
            libraryBoss = (Enemy) context.getEntityFactory().createEnemy("library_boss", BOSS_SPAWN_X, BOSS_SPAWN_Y);
        }

        bossSpawned = true;
    }

    @Override
    public void onEvent(final GameEvent<?> event) {
        if (event.getType() == EventType.ITEM_PICKED_UP) {
            final ItemPickedUpEvent data = (ItemPickedUpEvent) event.getData();
            if (data != null && data.getItem() != null && "brain".equals(data.getItem().getId())) {
                libraryCleared = true;
            }
        }
    }

    @Override
    public boolean isPuzzleActive() {
        return !puzzleSolved;
    }

    @Override
    public boolean isAutoAttackAllowed() {
        return puzzleSolved;
    }


    @Override
    public void dispose(final PlayScreen ctx) {
        EventDispatcher.getInstance().removeListener(EventType.ITEM_PICKED_UP, this);
        if (sequencer != null) {
            sequencer.dispose();
            sequencer = null;
        }
    }
}
