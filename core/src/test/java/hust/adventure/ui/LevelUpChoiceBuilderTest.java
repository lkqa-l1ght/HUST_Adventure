package hust.adventure.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import hust.adventure.core.data.GearDataLoader;
import hust.adventure.core.data.WeaponDataLoader;
import hust.adventure.entities.player.Player;
import hust.adventure.items.gear.Gear;
import hust.adventure.items.gear.GearManager;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponManager;
import hust.adventure.ui.components.GearUpgradeAction;
import hust.adventure.ui.components.UpgradeAction;
import hust.adventure.ui.components.WeaponUpgradeAction;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.items.weapons.WeaponFactory;
import hust.adventure.items.gear.GearFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LevelUpChoiceBuilderTest {
    private GearDataLoader mockGearLoader;
    private WeaponDataLoader mockWeaponLoader;
    private LevelUpChoiceBuilder choiceBuilder;

    private Player mockPlayer;
    private WeaponManager mockWeaponManager;
    private GearManager mockGearManager;

    @BeforeEach
    public void setUp() {
        Gdx.app = mock(Application.class);
        mockGearLoader = mock(GearDataLoader.class);
        mockWeaponLoader = mock(WeaponDataLoader.class);
        final GameProgressContext mockProgressContext = mock(GameProgressContext.class);
        final WeaponFactory mockWeaponFactory = mock(WeaponFactory.class);
        final GearFactory mockGearFactory = mock(GearFactory.class);

        choiceBuilder = LevelUpChoiceBuilder.builder()
                .gearDataManager(mockGearLoader)
                .weaponDataManager(mockWeaponLoader)
                .progressContext(mockProgressContext)
                .weaponFactory(mockWeaponFactory)
                .gearFactory(mockGearFactory)
                .build();

        mockPlayer = mock(Player.class);
        mockWeaponManager = mock(WeaponManager.class);
        mockGearManager = mock(GearManager.class);

        when(mockPlayer.getWeaponManager()).thenReturn(mockWeaponManager);
        when(mockPlayer.getGearManager()).thenReturn(mockGearManager);
    }

    @Test
    @DisplayName("Retrieves both weapon and gear unlock choices concurrently when unowned")
    public void testGetLevelUpChoicesWhenNoWeaponsOrGearsOwned() {
        // Arrange
        final Array<String> weaponIds = new Array<>();
        weaponIds.add("whip");
        when(mockWeaponLoader.getAllWeaponIds()).thenReturn(weaponIds);
        when(mockWeaponLoader.getWeaponName("whip")).thenReturn("Whip");
        when(mockWeaponLoader.getWeaponLevelDescription("whip", 1)).thenReturn("Unlock Whip");

        final Array<String> gearIds = new Array<>();
        gearIds.add("clover");
        when(mockGearLoader.getAllGearIds()).thenReturn(gearIds);
        when(mockGearLoader.getGearName("clover")).thenReturn("Clover");
        when(mockGearLoader.getGearLevelDescription("clover", 1)).thenReturn("Unlock Clover");

        // Player doesn't own them yet
        when(mockWeaponManager.getWeapons()).thenReturn(new Array<>());
        when(mockGearManager.getGear("clover")).thenReturn(null);

        // Act
        final Array<UpgradeAction> choices = choiceBuilder.getLevelUpChoices(mockPlayer);

        // Assert
        assertNotNull(choices);
        assertEquals(2, choices.size);

        boolean hasWhip = false;
        boolean hasClover = false;
        for (final UpgradeAction action : choices) {
            if (action instanceof WeaponUpgradeAction && action.getName().contains("Whip")) {
                hasWhip = true;
            }
            if (action instanceof GearUpgradeAction && action.getName().contains("Clover")) {
                hasClover = true;
            }
        }
        assertTrue(hasWhip && hasClover, "Both weapon and gear upgrades must be retrieved concurrently");
    }

    @Test
    @DisplayName("Selects level > 1 upgrades with accurate next-level descriptions")
    public void testGetLevelUpChoicesWithMultiLevelUpgradeSelection() {
        // Arrange: weapon at level 2, gear at level 3
        final Array<String> weaponIds = new Array<>();
        weaponIds.add("whip");
        when(mockWeaponLoader.getAllWeaponIds()).thenReturn(weaponIds);
        when(mockWeaponLoader.getWeaponName("whip")).thenReturn("Whip");
        when(mockWeaponLoader.getWeaponLevelDescription("whip", 3)).thenReturn("Whip Lv3 Damage +20");

        final Array<String> gearIds = new Array<>();
        gearIds.add("clover");
        when(mockGearLoader.getAllGearIds()).thenReturn(gearIds);
        when(mockGearLoader.getGearName("clover")).thenReturn("Clover");
        when(mockGearLoader.getGearLevelDescription("clover", 4)).thenReturn("Clover Lv4 Luck +15%");

        final BaseWeapon mockWeapon = mock(BaseWeapon.class);
        when(mockWeapon.getId()).thenReturn("whip");
        when(mockWeapon.getLevel()).thenReturn(2);
        final Array<BaseWeapon> ownedWeapons = new Array<>();
        ownedWeapons.add(mockWeapon);
        when(mockWeaponManager.getWeapons()).thenReturn(ownedWeapons);

        final Gear mockGear = mock(Gear.class);
        when(mockGear.getId()).thenReturn("clover");
        when(mockGear.getLevel()).thenReturn(3);
        when(mockGearManager.getGear("clover")).thenReturn(mockGear);

        // Act
        final Array<UpgradeAction> choices = choiceBuilder.getLevelUpChoices(mockPlayer);

        // Assert
        assertNotNull(choices);
        assertEquals(2, choices.size);

        boolean foundWhipLevel3 = false;
        boolean foundCloverLevel4 = false;
        for (final UpgradeAction action : choices) {
            if (action instanceof WeaponUpgradeAction && action.getName().equals("Whip (Cấp 3)")) {
                assertEquals("Whip Lv3 Damage +20", action.getDescription());
                foundWhipLevel3 = true;
            } else if (action instanceof GearUpgradeAction && action.getName().equals("Clover (Cấp 4)")) {
                assertEquals("Clover Lv4 Luck +15%", action.getDescription());
                foundCloverLevel4 = true;
            }
        }
        assertTrue(foundWhipLevel3, "Should include next level (Cấp 3) upgrade for level 2 weapon");
        assertTrue(foundCloverLevel4, "Should include next level (Cấp 4) upgrade for level 3 gear");
    }

    @Test
    @DisplayName("Falls back to heal and damage increase when all items are max level")
    public void testGetLevelUpChoicesWithFallbacksWhenEverythingIsMaxLevel() {
        // Arrange
        when(mockWeaponLoader.getAllWeaponIds()).thenReturn(new Array<>());
        when(mockGearLoader.getAllGearIds()).thenReturn(new Array<>());

        // Act
        final Array<UpgradeAction> choices = choiceBuilder.getLevelUpChoices(mockPlayer);

        // Assert
        assertNotNull(choices);
        assertFalse(choices.isEmpty());
        for (final UpgradeAction action : choices) {
            assertTrue(action.getName().contains("Heal") || action.getName().contains("Tăng sát thương"));
        }
    }
}
