package hust.adventure.items.weapons.impl;

import com.badlogic.gdx.graphics.Color;

import hust.adventure.core.assets.AssetPaths;
import hust.adventure.entities.base.Direction;
import hust.adventure.entities.player.Player;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponConfig;

/**
 * A weapon that fires a projectile in the player's current looking direction when the Spacebar key is pressed.
 */
public class BunDauWeapon extends BaseWeapon {
    private static final float PROJECTILE_SPEED = 350f;
    private static final Color PROJECTILE_COLOR = Color.YELLOW;

    public BunDauWeapon(final Player owner, final WeaponConfig config) {
        super(owner, config);
    }

    @Override
    public boolean isAutoFiring() {
        return false;
    }

    @Override
    protected void executeAttackAction() {
        final Direction dir = getOwner().getDirection();
        float vx = 0f;
        float vy = 0f;

        if (dir != null) {
            switch (dir) {
            case RIGHT:
                vx = PROJECTILE_SPEED;
                break;
            case LEFT:
                vx = -PROJECTILE_SPEED;
                break;
            case UP:
                vy = PROJECTILE_SPEED;
                break;
            case DOWN:
                vy = -PROJECTILE_SPEED;
                break;
            }
        }

        spawnPlayerProjectile(getOwner().getX(), getOwner().getY(), vx, vy, PROJECTILE_COLOR,
                AssetPaths.SFX_WEAPON_BUN_DAU);
    }

}
