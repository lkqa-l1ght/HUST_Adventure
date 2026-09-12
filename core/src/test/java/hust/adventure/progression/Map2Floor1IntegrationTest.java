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
import hust.adventure.screens.levels.Floor1Behavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for Map 2 (Floor 1) progression and Lecture Notes gating.
 * Tests verify the seam between Floor1Behavior, EventDispatcher, ProgressContext, and MapDirector.
 */
public class Map2Floor1IntegrationTest {

    private ProgressContext progressContext;
    private MapDirector director;

    @BeforeEach
    void setUp() {
        EventDispatcher.resetInstance();
        final ItemManager itemManager = new ItemManager();
        progressContext = new ProgressContext(itemManager);
        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, progressContext);
        director = progressContext.getMapDirector();

        // Advance past Map 1 to Map 2 (Floor 1)
        director.onDeadlinesCleared();
        director.onKeyItemCollected("note");
        director.advanceToNextMap();
    }

    @Test
    @DisplayName("Active stage is Map 2 (Floor 1) and transitions are initially locked")
    void activeStageIsMap2Floor1() {
        assertEquals(CampusMap.MAP_2_FLOOR_1, director.getCurrentStage());
        assertFalse(director.canTransition(), "Transitions must be locked on stage entry");
        assertFalse(director.isPortalActive());
    }

    @Test
    @DisplayName("Lecture Notes pickup event unlocks the portal in MapDirector")
    void lectureNotesPickupUnlocksPortal() {
        assertFalse(director.canTransition());

        director.onDeadlinesCleared();
        assertFalse(director.canTransition(), "Portal locked until Lecture Notes are collected");

        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item notesItem = new Item("lecture_notes", "Lecture Notes", "Classroom knowledge notes", "note.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(notesItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));

        assertTrue(director.canTransition(), "Both Deadlines and Lecture Notes fulfilled");
        assertTrue(director.isPortalActive());
    }

    @Test
    @DisplayName("Picking up old note does not unlock Map 2 portal")
    void noteItemDoesNotUnlockMap2Portal() {
        director.onDeadlinesCleared();
        assertFalse(director.canTransition());

        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item noteItem = new Item("note", "Admission Note", "Old note", "note.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(noteItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));

        assertFalse(director.canTransition(), "Old Admission Note must not unlock Map 2 portal");
        assertFalse(director.isPortalActive());
    }

    @Test
    @DisplayName("Floor1Behavior canTransition delegates directly to MapDirector")
    void floor1BehaviorDelegatesToDirector() {
        final Floor1Behavior floor1Behavior = new Floor1Behavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        assertFalse(floor1Behavior.canTransition(mockContext));

        director.onDeadlinesCleared();
        assertFalse(floor1Behavior.canTransition(mockContext));

        director.onKeyItemCollected("lecture_notes");
        assertTrue(floor1Behavior.canTransition(mockContext));
    }

    @Test
    @DisplayName("End-to-end Map 2 loop: Init -> Classroom Deadlines -> Notes Pickup -> Portal Unlock -> Map 3 Advance")
    void endToEndMap2ProgressionLoop() {
        final Floor1Behavior floor1Behavior = new Floor1Behavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        // 1. Initial State
        assertEquals(CampusMap.MAP_2_FLOOR_1, director.getCurrentStage());
        assertFalse(floor1Behavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 2. Classroom Deadlines Cleared
        director.onDeadlinesCleared();
        assertFalse(floor1Behavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 3. Lecture Notes Collected
        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item notesItem = new Item("lecture_notes", "Lecture Notes", "Classroom knowledge notes", "note.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(notesItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));

        assertTrue(floor1Behavior.canTransition(mockContext));
        assertTrue(director.isPortalActive());

        // 4. Advance to Map 3 (Library)
        final CampusMap nextStage = director.advanceToNextMap();
        assertEquals(CampusMap.MAP_3_LIBRARY, nextStage);
        assertEquals(CampusMap.MAP_3_LIBRARY, director.getCurrentStage());
        assertFalse(director.canTransition(), "Map 3 transitions must be reset and locked");
        assertFalse(director.isPortalActive());
    }
}
