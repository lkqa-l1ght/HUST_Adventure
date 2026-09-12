package hust.adventure.progression;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import hust.adventure.core.context.ProgressContext;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;
import hust.adventure.items.base.ItemManager;
import hust.adventure.screens.PlayScreen;
import hust.adventure.screens.levels.BossFightBehavior;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for Final Map (Boss Room) defense against Professor T.H.T,
 * Graduation victory flow, Academic Suspension on death, and gear persistence.
 */
public class Map5BossGraduationIntegrationTest {

    private ProgressContext progressContext;
    private MapDirector director;

    @BeforeEach
    void setUp() {
        Gdx.app = Mockito.mock(Application.class);
        EventDispatcher.resetInstance();
        final ItemManager itemManager = new ItemManager();
        progressContext = new ProgressContext(itemManager);
        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, progressContext);
        EventDispatcher.getInstance().addListener(EventType.PLAYER_DIED, progressContext);
        EventDispatcher.getInstance().addListener(EventType.ACADEMIC_SUSPENSION, progressContext);
        director = progressContext.getMapDirector();

        advanceToFinalMap();
    }

    private void advanceToFinalMap() {
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

        // Map 4 -> Final Map
        director.onDeadlinesCleared();
        director.onKeyItemCollected("usb");
        director.advanceToNextMap();
    }

    @Test
    @DisplayName("Active stage is Final Map (Boss Room) and transitions are initially locked")
    void activeStageIsFinalMapBoss() {
        assertEquals(CampusMap.FINAL_MAP_BOSS, director.getCurrentStage());
        assertFalse(director.canTransition(), "Transitions locked until Professor T.H.T is defeated");
    }

    @Test
    @DisplayName("Defeating Professor T.H.T triggers Graduation event via BossFightBehavior and unlocks victory flow")
    void defeatingProfessorThtTriggersGraduation() {
        final AtomicBoolean graduationEventFired = new AtomicBoolean(false);
        final EventListener listener = event -> {
            if (event.getType() == EventType.GRADUATION) {
                graduationEventFired.set(true);
            }
        };
        EventDispatcher.getInstance().addListener(EventType.GRADUATION, listener);

        final BossFightBehavior bossFightBehavior = new BossFightBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        assertFalse(director.canTransition());
        assertFalse(bossFightBehavior.canTransition(mockContext));

        // BossFightBehavior triggers graduation upon defeating Professor T.H.T
        bossFightBehavior.onBossDefeated(mockContext);

        assertTrue(graduationEventFired.get(), "Graduation event must be dispatched on thesis defense victory");
        assertTrue(director.canTransition(), "Victory condition fulfilled on Final Map");
        assertTrue(bossFightBehavior.canTransition(mockContext));

        EventDispatcher.getInstance().removeListener(EventType.GRADUATION, listener);
    }

    @Test
    @DisplayName("Student Health reaching 0 fires Academic Suspension event and cleanly terminates the run")
    void studentHealthZeroFiresAcademicSuspensionAndTerminatesRun() {
        final AtomicBoolean suspensionFired = new AtomicBoolean(false);
        final EventListener listener = event -> {
            if (event.getType() == EventType.ACADEMIC_SUSPENSION) {
                suspensionFired.set(true);
            }
        };
        EventDispatcher.getInstance().addListener(EventType.ACADEMIC_SUSPENSION, listener);

        // Currently on Final Map
        assertEquals(CampusMap.FINAL_MAP_BOSS, director.getCurrentStage());

        // Reduce student health to 0
        progressContext.getPlayerStats().setHp(0f);

        assertTrue(suspensionFired.get(), "Academic Suspension event must be dispatched when student Health reaches 0");
        assertEquals(CampusMap.MAP_1_OUTSIDE, director.getCurrentStage(),
                "Active run must be terminated and reset back to Map 1 upon Academic Suspension");

        EventDispatcher.getInstance().removeListener(EventType.ACADEMIC_SUSPENSION, listener);
    }

    @Test
    @DisplayName("Weapons and Gears persist across all map transitions within the run")
    void weaponsAndGearsPersistAcrossAllTransitions() {
        director.resetRun();

        // Equip and upgrade weapon and gear
        progressContext.getWeaponLevels().put("bun_dau", 3);
        progressContext.getWeaponLevels().put("whip", 2);
        progressContext.getGearLevels().put("coffee_den", 2);

        // Advance through all stages
        for (int i = 0; i < 4; i++) {
            final CampusMap current = director.getCurrentStage();
            if (current == CampusMap.MAP_3_LIBRARY) {
                director.onAcademicChallengesCleared();
                director.onBossDefeated();
                director.onKeyItemCollected("brain");
            } else {
                director.onDeadlinesCleared();
                if (current == CampusMap.MAP_1_OUTSIDE) {
                    director.onKeyItemCollected("note");
                } else if (current == CampusMap.MAP_2_FLOOR_1) {
                    director.onKeyItemCollected("lecture_notes");
                } else if (current == CampusMap.MAP_4_LAB) {
                    director.onKeyItemCollected("usb");
                }
            }
            director.advanceToNextMap();

            // Verify persistence after each transition
            assertEquals(3, progressContext.getWeaponLevels().get("bun_dau"));
            assertEquals(2, progressContext.getWeaponLevels().get("whip"));
            assertEquals(2, progressContext.getGearLevels().get("coffee_den"));
        }

        assertEquals(CampusMap.FINAL_MAP_BOSS, director.getCurrentStage());
    }

    @Test
    @DisplayName("Complete end-to-end run cycle: Map 1 through Graduation")
    void completeRunCycleFromMap1ThroughGraduation() {
        director.resetRun();

        final AtomicBoolean graduationTriggered = new AtomicBoolean(false);
        final EventListener listener = event -> {
            if (event.getType() == EventType.GRADUATION) {
                graduationTriggered.set(true);
            }
        };
        EventDispatcher.getInstance().addListener(EventType.GRADUATION, listener);

        // Stage 1: Map 1 (Outside)
        assertEquals(CampusMap.MAP_1_OUTSIDE, director.getCurrentStage());
        director.onDeadlinesCleared();
        director.onKeyItemCollected("note");
        assertTrue(director.canTransition());
        director.advanceToNextMap();

        // Stage 2: Map 2 (Floor 1)
        assertEquals(CampusMap.MAP_2_FLOOR_1, director.getCurrentStage());
        director.onDeadlinesCleared();
        director.onKeyItemCollected("lecture_notes");
        assertTrue(director.canTransition());
        director.advanceToNextMap();

        // Stage 3: Map 3 (Library)
        assertEquals(CampusMap.MAP_3_LIBRARY, director.getCurrentStage());
        director.onAcademicChallengesCleared();
        director.onBossDefeated();
        director.onKeyItemCollected("brain");
        assertTrue(director.canTransition());
        director.advanceToNextMap();

        // Stage 4: Map 4 (Computer Lab)
        assertEquals(CampusMap.MAP_4_LAB, director.getCurrentStage());
        director.onDeadlinesCleared();
        director.onKeyItemCollected("usb");
        assertTrue(director.canTransition());
        director.advanceToNextMap();

        // Stage 5: Final Map (Thesis Defense vs Professor T.H.T)
        assertEquals(CampusMap.FINAL_MAP_BOSS, director.getCurrentStage());
        assertFalse(director.canTransition());

        final BossFightBehavior bossFightBehavior = new BossFightBehavior();
        final PlayScreen mockContext = Mockito.mock(PlayScreen.class);
        Mockito.when(mockContext.getProgressContext()).thenReturn(progressContext);

        assertFalse(bossFightBehavior.canTransition(mockContext));

        // Professor T.H.T Defeated through BossFightBehavior
        bossFightBehavior.onBossDefeated(mockContext);

        assertTrue(director.canTransition());
        assertTrue(bossFightBehavior.canTransition(mockContext));
        assertTrue(graduationTriggered.get(), "Complete run cycle must end in Graduation!");

        EventDispatcher.getInstance().removeListener(EventType.GRADUATION, listener);
    }
}
