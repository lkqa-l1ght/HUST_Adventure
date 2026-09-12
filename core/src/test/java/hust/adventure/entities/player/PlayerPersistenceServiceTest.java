package hust.adventure.entities.player;

import hust.adventure.core.context.GameProgressContext;
import hust.adventure.items.gear.Gear;
import hust.adventure.items.gear.GearFactory;
import hust.adventure.items.gear.GearManager;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponFactory;
import hust.adventure.items.weapons.WeaponManager;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerPersistenceService saving and restoring weapons/gears levels
 * to and from the global GameProgressContext.
 */
public class PlayerPersistenceServiceTest {

    private GameProgressContext progressContext;
    private GearFactory gearFactory;
    private WeaponFactory weaponFactory;
    private PlayerPersistenceService persistenceService;

    private Player player;
    private WeaponManager weaponManager;
    private GearManager gearManager;

    private Map<String, Integer> weaponLevels;
    private Map<String, Integer> gearLevels;

    @BeforeEach
    public void setUp() {
        progressContext = mock(GameProgressContext.class);
        gearFactory = mock(GearFactory.class);
        weaponFactory = mock(WeaponFactory.class);
        persistenceService = new PlayerPersistenceService(progressContext, gearFactory, weaponFactory);

        player = mock(Player.class);
        weaponManager = mock(WeaponManager.class);
        gearManager = mock(GearManager.class);

        when(player.getWeaponManager()).thenReturn(weaponManager);
        when(player.getGearManager()).thenReturn(gearManager);

        weaponLevels = new HashMap<>();
        gearLevels = new HashMap<>();

        when(progressContext.getWeaponLevels()).thenReturn(weaponLevels);
        when(progressContext.getGearLevels()).thenReturn(gearLevels);
    }

    @Test
    public void testSaveNullSafety() {
        // Should not throw exceptions
        persistenceService.save(null);
        
        PlayerPersistenceService nullCtxService = new PlayerPersistenceService(null, gearFactory, weaponFactory);
        nullCtxService.save(player);
    }

    @Test
    public void testRestoreNullSafety() {
        // Should not throw exceptions
        persistenceService.restore(null);

        PlayerPersistenceService nullCtxService = new PlayerPersistenceService(null, gearFactory, weaponFactory);
        nullCtxService.restore(player);
    }

    @Test
    public void testSaveWeaponsAndGears() {
        BaseWeapon weapon1 = mock(BaseWeapon.class);
        when(weapon1.getId()).thenReturn("Bun_Dau");
        when(weapon1.getLevel()).thenReturn(3);

        BaseWeapon weapon2 = mock(BaseWeapon.class);
        when(weapon2.getId()).thenReturn("Magic_Wand");
        when(weapon2.getLevel()).thenReturn(5);

        Array<BaseWeapon> weapons = new Array<>();
        weapons.add(weapon1);
        weapons.add(weapon2);
        when(weaponManager.getWeapons()).thenReturn(weapons);

        Gear gear1 = mock(Gear.class);
        when(gear1.getId()).thenReturn("Magnet_Gear");
        when(gear1.getLevel()).thenReturn(2);

        Array<Gear> gears = new Array<>();
        gears.add(gear1);
        when(gearManager.getGears()).thenReturn(gears);

        // Prepopulate maps to ensure they are cleared
        weaponLevels.put("dummy_weapon", 1);
        gearLevels.put("dummy_gear", 1);

        persistenceService.save(player);

        assertEquals(2, weaponLevels.size());
        assertEquals(3, weaponLevels.get("bun_dau"));
        assertEquals(5, weaponLevels.get("magic_wand"));
        assertNull(weaponLevels.get("dummy_weapon"));

        assertEquals(1, gearLevels.size());
        assertEquals(2, gearLevels.get("magnet_gear"));
        assertNull(gearLevels.get("dummy_gear"));
    }

    @Test
    public void testRestoreEmptyWeaponListEquipsDefault() {
        BaseWeapon defaultWeapon = mock(BaseWeapon.class);
        when(weaponFactory.createWeapon("bun_dau", player)).thenReturn(defaultWeapon);

        persistenceService.restore(player);

        verify(weaponFactory).createWeapon("bun_dau", player);
        verify(defaultWeapon).equip(player);
        assertEquals(1, weaponLevels.get("bun_dau"));
    }

    @Test
    public void testRestoreWeaponsAndGearsWithUpgrades() {
        weaponLevels.put("bun_dau", 3);
        weaponLevels.put("magic_wand", 1);

        gearLevels.put("magnet_gear", 4);

        BaseWeapon bundauMock = mock(BaseWeapon.class);
        BaseWeapon wandMock = mock(BaseWeapon.class);
        when(weaponFactory.createWeapon("bun_dau", player)).thenReturn(bundauMock);
        when(weaponFactory.createWeapon("magic_wand", player)).thenReturn(wandMock);

        Gear magnetMock = mock(Gear.class);
        when(gearFactory.createGear("magnet_gear")).thenReturn(magnetMock);

        persistenceService.restore(player);

        // Verify bun_dau upgrades: level 3 means 2 upgrades
        verify(weaponFactory).createWeapon("bun_dau", player);
        verify(bundauMock, times(2)).upgrade(0f, 0f);
        verify(bundauMock).equip(player);

        // Verify magic_wand: level 1 means 0 upgrades
        verify(weaponFactory).createWeapon("magic_wand", player);
        verify(wandMock, never()).upgrade(anyFloat(), anyFloat());
        verify(wandMock).equip(player);

        // Verify magnet_gear upgrades: level 4 means 3 upgrades & applyEquipEffects
        verify(gearFactory).createGear("magnet_gear");
        verify(magnetMock, times(3)).upgrade();
        verify(magnetMock).equip(player);
        verify(gearFactory, times(3)).applyEquipEffect("magnet_gear", player);
    }
}
