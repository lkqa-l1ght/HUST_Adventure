package hust.adventure.behavior.ai;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TelegraphedChargeBehaviorTest {

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
    }

    @Test
    public void testTelegraphChargeLifecycle() {
        TelegraphedChargeBehavior behavior = new TelegraphedChargeBehavior();
        
        Enemy enemy = mock(Enemy.class);
        Player player = mock(Player.class);
        EntityManager entityManager = mock(EntityManager.class);

        // Mock positions
        when(enemy.getX()).thenReturn(0f);
        when(enemy.getY()).thenReturn(0f);
        when(enemy.getSpeed()).thenReturn(60f);

        when(player.getX()).thenReturn(100f);
        when(player.getY()).thenReturn(0f);

        // Execute first frame (initializes normalSpeed and chargeSpeed)
        behavior.execute(enemy, 0.1f, player, entityManager);
        verify(enemy, atLeastOnce()).getSpeed();

        // CHASE state (stateTimer starts at 0.5f - 0.1f = 0.4f remaining)
        behavior.execute(enemy, 0.5f, player, entityManager);
        
        // Transitions to TELEGRAPH
        verify(enemy).setSpeed(0f);
        assertTrue(behavior.isTelegraphed());
        assertEquals(1.0f, behavior.getTelegraphTimer(), 0.01f);
        assertEquals(500f, behavior.getTelegraphTargetX(), 0.01f);
        assertEquals(0f, behavior.getTelegraphTargetY(), 0.01f);

        // TELEGRAPH state
        behavior.execute(enemy, 0.5f, player, entityManager);
        assertTrue(behavior.isTelegraphed());
        assertEquals(0.5f, behavior.getTelegraphTimer(), 0.01f);

        // Finishes TELEGRAPH -> transitions to CHARGE
        behavior.execute(enemy, 0.6f, player, entityManager);
        verify(enemy).setSpeed(300f);

        // CHARGE state
        behavior.execute(enemy, 0.5f, player, entityManager);
        assertFalse(behavior.isTelegraphed());
        assertEquals(0f, behavior.getTelegraphTimer());

        // Finishes CHARGE -> transitions to CHASE cooldown
        behavior.execute(enemy, 0.2f, player, entityManager);
        verify(enemy).setSpeed(60f);
    }
}
