package hust.adventure.progression;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import hust.adventure.core.context.ProgressContext;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.base.MapObject;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.items.base.Item;
import hust.adventure.items.base.ItemManager;
import hust.adventure.screens.PlayScreen;
import hust.adventure.screens.levels.LabBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for Map 4 (Computer Lab) progression and Source Code USB gating.
 * Tests verify the seam between LabBehavior, EventDispatcher, ProgressContext, and MapDirector.
 */
public class Map4LabIntegrationTest {

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

        advanceToMap4();
    }

    private void advanceToMap4() {
        // Map 1 -> Map 2
        director.onDeadlinesCleared();
        director.onKeyItemCollected("note");
        director.advanceToNextMap();

        // Map 2 -> Map 3
        director.onDeadlinesCleared();
        director.onKeyItemCollected("lecture_notes");
        director.advanceToNextMap();

        // Map 3 -> Map 4
        director.onAcademicChallengesCleared();
        director.onBossDefeated();
        director.onKeyItemCollected("brain");
        director.advanceToNextMap();
    }

    private void dispatchUsbPickupEvent() {
        final MapObject mockPicker = Mockito.mock(MapObject.class);
        final Item usbItem = new Item("usb", "Source Code USB", "USB containing critical patch code", "usb.png");
        final ItemPickedUpEvent payload = new ItemPickedUpEvent(usbItem, mockPicker);
        EventDispatcher.getInstance().dispatch(new GameEvent<>(EventType.ITEM_PICKED_UP, payload));
    }

    @Test
    @DisplayName("Active stage is Map 4 (Computer Lab) and transitions are initially locked")
    void activeStageIsMap4Lab() {
        assertEquals(CampusMap.MAP_4_LAB, director.getCurrentStage());
        assertFalse(director.canTransition(), "Transitions must be locked on Lab entry");
        assertFalse(director.isPortalActive());
    }

    @Test
    @DisplayName("Clearing Lab Deadlines keeps portal locked until Source Code USB is collected")
    void labDeadlinesClearedKeepsPortalLockedUntilUsbPickup() {
        director.onDeadlinesCleared();

        assertFalse(director.canTransition(), "Portal locked until Source Code USB is secured");
        assertFalse(director.isPortalActive());

        dispatchUsbPickupEvent();

        assertTrue(director.canTransition(), "Deadlines and USB fulfilled");
        assertTrue(director.isPortalActive());
    }

    @Test
    @DisplayName("LabBehavior canTransition checks both MapDirector and active enemy clearance")
    void labBehaviorChecksDirectorAndEnemies() {
        final LabBehavior labBehavior = new LabBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        final EntityManager mockEntityManager = Mockito.mock(EntityManager.class);

        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);
        Mockito.when(mockContext.getEntityManager()).thenReturn(mockEntityManager);

        // Initially locked
        assertFalse(labBehavior.canTransition(mockContext));

        // Deadlines and USB fulfilled
        director.onDeadlinesCleared();
        director.onKeyItemCollected("usb");

        // Active enemies still present -> blocked
        Mockito.when(mockEntityManager.hasActiveEnemies()).thenReturn(true);
        assertFalse(labBehavior.canTransition(mockContext));

        // Enemies cleared -> transition unlocked
        Mockito.when(mockEntityManager.hasActiveEnemies()).thenReturn(false);
        assertTrue(labBehavior.canTransition(mockContext));
    }

    @Test
    @DisplayName("End-to-end Map 4 loop: Lab Deadlines -> USB Pickup -> Portal Unlock -> Final Boss Room Advance")
    void endToEndMap4ProgressionLoop() {
        final LabBehavior labBehavior = new LabBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        // 1. Initial State in Lab
        assertEquals(CampusMap.MAP_4_LAB, director.getCurrentStage());
        assertFalse(labBehavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 2. High-intensity Lab Deadlines Survived
        director.onDeadlinesCleared();
        assertFalse(labBehavior.canTransition(mockContext));
        assertFalse(director.isPortalActive());

        // 3. Source Code USB Secured
        dispatchUsbPickupEvent();

        assertTrue(labBehavior.canTransition(mockContext));
        assertTrue(director.isPortalActive());

        // 4. Advance to Final Map (Boss Room)
        final CampusMap nextStage = director.advanceToNextMap();
        assertEquals(CampusMap.FINAL_MAP_BOSS, nextStage);
        assertEquals(CampusMap.FINAL_MAP_BOSS, director.getCurrentStage());
        assertFalse(director.canTransition(), "Final Map transitions locked until Professor T.H.T is defeated");
    }
}
