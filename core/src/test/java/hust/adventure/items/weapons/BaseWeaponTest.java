package hust.adventure.items.weapons;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseWeaponTest {
    private Player mockPlayer;
    private WeaponConfig config;
    private BaseWeapon weapon;

    private static class TestWeapon extends BaseWeapon {
        public TestWeapon(Player owner, WeaponConfig config) {
            super(owner, config);
        }

        @Override
        protected void executeAttackAction() {
            // No-op for test
        }
    }

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
        mockPlayer = mock(Player.class);
        config = new WeaponConfig();
        config.setId("test_weapon");
        config.setName("Test Weapon");
        config.setDescription("A weapon for testing");
        config.setBaseDamage(20f);
        config.setCooldown(1.5f);
        config.setArea(1.0f);
        config.setMaxLevel(5);

        // Configure default player mock behavior
        when(mockPlayer.getPowerMultiplier()).thenReturn(1.0f);
        when(mockPlayer.getCooldownMultiplier()).thenReturn(1.0f);
        when(mockPlayer.getAreaMultiplier()).thenReturn(1.0f);

        weapon = new TestWeapon(mockPlayer, config);
    }

    @Test
    public void testInitialization() {
        assertEquals("test_weapon", weapon.getId());
        assertEquals("Test Weapon", weapon.getName());
        assertEquals("A weapon for testing", weapon.getDescription());
        assertEquals(1, weapon.getLevel());
        assertEquals(20f, weapon.getBaseDamage(), 0.01f);
        assertEquals(1.5f, weapon.getCooldown(), 0.01f);
        assertEquals(1.0f, weapon.getArea(), 0.01f);
    }

    @Test
    public void testGetBaseDamageWithPlayerMultiplier() {
        when(mockPlayer.getPowerMultiplier()).thenReturn(1.5f);
        assertEquals(30f, weapon.getBaseDamage(), 0.01f);
    }

    @Test
    public void testGetCooldownWithPlayerMultiplier() {
        when(mockPlayer.getCooldownMultiplier()).thenReturn(0.8f);
        assertEquals(1.2f, weapon.getCooldown(), 0.01f);
    }

    @Test
    public void testGetEffectiveDamageWithGlobalMultiplier() {
        GameProgressContext mockContext = mock(GameProgressContext.class);
        when(mockContext.getDamageMultiplier()).thenReturn(1.2f);
        when(mockPlayer.getProgressContext()).thenReturn(mockContext);

        when(mockPlayer.getPowerMultiplier()).thenReturn(1.5f);
        // 20 * 1.5 (player) * 1.2 (global) = 36
        assertEquals(36f, weapon.getEffectiveDamage(), 0.01f);
    }

    @Test
    public void testUpgradeLogic() {
        weapon.upgrade(5f, 0.2f);
        assertEquals(2, weapon.getLevel());

        // baseDamage: 20 + 5 = 25
        // player multiplier: 1.2
        when(mockPlayer.getPowerMultiplier()).thenReturn(1.2f);
        assertEquals(30f, weapon.getBaseDamage(), 0.01f);

        // cooldown: 1.5 - 0.2 = 1.3
        assertEquals(1.3f, weapon.getCooldown(), 0.01f);
    }
}
