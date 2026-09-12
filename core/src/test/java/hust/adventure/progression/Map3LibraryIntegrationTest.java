package hust.adventure.progression;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import hust.adventure.core.context.ProgressContext;
import hust.adventure.entities.base.MapObject;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;
import hust.adventure.screens.PlayScreen;
import hust.adventure.screens.levels.LibraryBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for Map 3 (Library) progression, Academic Challenges, Library Guardian, and Awakened Brain gating.
 * Tests verify the seam between LibraryBehavior, EventDispatcher, ProgressContext, and MapDirector.
 */
public class Map3LibraryIntegrationTest {

    private ProgressContext progressContext;
    private MapDirector director;

    @BeforeEach
    void setUp() {
        Gdx.app = Mockito.mock(Application.class);
        EventDispatcher.resetInstance();
        final ItemManager itemManager = new ItemManager();
        progressContext = new ProgressContext(itemManager);
        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, progressContext);
        director = progressContext.getMapDirector();

        // Advance to Map 3 (Library)
        director.onDeadlinesCleared();
        director.onKeyItemCollected("note");
        director.advanceToNextMap(); // Map 2 (Floor 1)

        director.onDeadlinesCleared();
        director.onKeyItemCollected("lecture_notes");
        director.advanceToNextMap(); // Map 3 (Library)
    }

    private void dispatchBrainPickupEvent() {
        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item brainItem = new Item("brain", "Awakened Brain", "Ancient library artifact", "brain.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(brainItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));
    }

    @Test
    @DisplayName("Active stage is Map 3 (Library) and Auto-Attacks are initially suppressed")
    void activeStageIsMap3LibraryWithCombatSuppressed() {
        assertEquals(CampusMap.MAP_3_LIBRARY, director.getCurrentStage());
        assertFalse(director.isAutoAttackAllowed(), "Auto-attack must be suppressed during academic challenges");
        assertFalse(director.canTransition(), "Transitions locked on Library entry");
        assertFalse(director.isPortalActive());
    }

    @Test
    @DisplayName("Solving Academic Challenges lifts auto-attack suppression but keeps portal locked")
    void challengesSolvedLiftsSuppression() {
        director.onAcademicChallengesCleared();

        assertTrue(director.isAutoAttackAllowed(), "Auto-attack must be permitted once challenges are solved");
        assertFalse(director.canTransition(), "Portal remains locked without boss defeat and brain pickup");
        assertFalse(director.isPortalActive());
    }

    @Test
    @DisplayName("Awakened Brain pickup event unlocks portal when challenges and boss are cleared")
    void brainPickupUnlocksPortal() {
        director.onAcademicChallengesCleared();
        director.onBossDefeated();
        assertFalse(director.canTransition(), "Portal locked until Awakened Brain is secured");

        dispatchBrainPickupEvent();

        assertTrue(director.canTransition(), "Challenges, boss defeat, and brain pickup fulfilled");
        assertTrue(director.isPortalActive());
    }

    @Test
    @DisplayName("LibraryBehavior canTransition delegates directly to MapDirector")
    void libraryBehaviorDelegatesToDirector() {
        final LibraryBehavior libraryBehavior = new LibraryBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        assertFalse(libraryBehavior.canTransition(mockContext));

        director.onAcademicChallengesCleared();
        director.onBossDefeated();
        assertFalse(libraryBehavior.canTransition(mockContext));

        director.onKeyItemCollected("brain");
        assertTrue(libraryBehavior.canTransition(mockContext));
    }

    @Test
    @DisplayName("End-to-end Map 3 loop: Challenges -> Boss Defeated -> Brain Picked Up -> Portal Unlock -> Map 4 Advance")
    void endToEndMap3ProgressionLoop() {
        final LibraryBehavior libraryBehavior = new LibraryBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        // 1. Initial State in Library
        assertEquals(CampusMap.MAP_3_LIBRARY, director.getCurrentStage());
        assertFalse(director.isAutoAttackAllowed());
        assertFalse(libraryBehavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 2. Academic Challenges Solved
        director.onAcademicChallengesCleared();
        assertTrue(director.isAutoAttackAllowed(), "Combat enabled for Library Guardian encounter");
        assertFalse(libraryBehavior.canTransition(mockContext));

        // 3. Library Guardian Defeated
        director.onBossDefeated();
        assertFalse(libraryBehavior.canTransition(mockContext));

        // 4. Awakened Brain Secured
        dispatchBrainPickupEvent();

        assertTrue(libraryBehavior.canTransition(mockContext));
        assertTrue(director.isPortalActive());

        // 5. Advance to Map 4 (Computer Lab)
        final CampusMap nextStage = director.advanceToNextMap();
        assertEquals(CampusMap.MAP_4_LAB, nextStage);
        assertEquals(CampusMap.MAP_4_LAB, director.getCurrentStage());
        assertFalse(director.canTransition(), "Map 4 transitions must be reset and locked");
        assertFalse(director.isPortalActive());
    }
}
