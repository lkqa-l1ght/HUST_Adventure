package hust.adventure.progression;

import hust.adventure.core.context.ProgressContext;
import hust.adventure.entities.base.MapObject;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;
import hust.adventure.screens.PlayScreen;
import hust.adventure.screens.levels.OutsideBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for Map 1 (Outside) progression and Admission Note gating.
 * Tests verify the seam between event dispatching, ProgressContext, and MapDirector.
 */
public class Map1OutsideIntegrationTest {

    private ProgressContext progressContext;
    private MapDirector director;

    @BeforeEach
    void setUp() {
        EventDispatcher.resetInstance();
        final ItemManager itemManager = new ItemManager();
        progressContext = new ProgressContext(itemManager);
        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, progressContext);
        director = progressContext.getMapDirector();
    }

    @Test
    @DisplayName("Map 1 Outside is the initial active stage in ProgressContext")
    void initialStageIsMap1Outside() {
        assertEquals(CampusMap.MAP_1_OUTSIDE, director.getCurrentStage());
        assertFalse(director.canTransition());
        assertFalse(director.isPortalActive());
    }

    @Test
    @DisplayName("Admission Note pickup event registers in MapDirector via event system")
    void admissionNotePickupRegistersInDirector() {
        assertFalse(director.canTransition());

        director.onDeadlinesCleared();
        assertFalse(director.canTransition(), "Portal locked until Admission Note is collected");

        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item noteItem = new Item("note", "Admission Note", "Welcome to HUST", "note.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(noteItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));

        assertTrue(director.canTransition(), "Both Deadlines and Admission Note fulfilled");
        assertTrue(director.isPortalActive());
    }

    @Test
    @DisplayName("OutsideBehavior canTransition delegates directly to MapDirector")
    void outsideBehaviorDelegatesToDirector() {
        final OutsideBehavior outsideBehavior = new OutsideBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        assertFalse(outsideBehavior.canTransition(mockContext));

        director.onDeadlinesCleared();
        assertFalse(outsideBehavior.canTransition(mockContext));

        director.onKeyItemCollected("note");
        assertTrue(outsideBehavior.canTransition(mockContext));
    }

    @Test
    @DisplayName("End-to-end Map 1 loop: Init -> Deadlines -> Note Pickup -> Portal Unlock -> Map 2 Advance")
    void endToEndMap1ProgressionLoop() {
        final OutsideBehavior outsideBehavior = new OutsideBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        // 1. Initial State
        assertEquals(CampusMap.MAP_1_OUTSIDE, director.getCurrentStage());
        assertFalse(outsideBehavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 2. Deadlines Cleared
        director.onDeadlinesCleared();
        assertFalse(outsideBehavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 3. Admission Note Collected
        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item noteItem = new Item("note", "Admission Note", "Welcome to HUST", "note.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(noteItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));

        assertTrue(outsideBehavior.canTransition(mockContext));
        assertTrue(director.isPortalActive());

        // 4. Advance to Next Stage
        final CampusMap nextStage = director.advanceToNextMap();
        assertEquals(CampusMap.MAP_2_FLOOR_1, nextStage);
        assertEquals(CampusMap.MAP_2_FLOOR_1, director.getCurrentStage());
        assertFalse(director.canTransition(), "Map 2 transitions must be reset and locked");
        assertFalse(director.isPortalActive());
    }
}
