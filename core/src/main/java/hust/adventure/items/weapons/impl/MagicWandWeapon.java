package hust.adventure.items.weapons.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import hust.adventure.collision.CollisionLayer;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.player.Player;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponConfig;

/**
 * A magic wand weapon that fires projectiles at the nearest enemy.
 */
public class MagicWandWeapon extends BaseWeapon {
    private static final float PROJECTILE_SPEED = 200f;
    private static final float MAX_RANGE = 400f;
    private final Vector2 tmpDirection = new Vector2();

    public MagicWandWeapon(final Player owner, final WeaponConfig config) {
        super(owner, config);
    }

    @Override
    protected void executeAttackAction() {
        final MapObject target = getOwner().getCollisionManager().getNearestEntity(getOwner().getX(), getOwner().getY(),
                MAX_RANGE, CollisionLayer.ENEMY);

        if (target != null) {
            fireAt(target);
        }
    }

    private void fireAt(final MapObject target) {
        final float startX = getOwner().getX();
        final float startY = getOwner().getY();

        tmpDirection.set(target.getX() - startX, target.getY() - startY).nor();
        final float vx = tmpDirection.x * PROJECTILE_SPEED;
        final float vy = tmpDirection.y * PROJECTILE_SPEED;

        spawnPlayerProjectile(startX, startY, vx, vy, Color.CYAN,
                AssetPaths.SFX_WEAPON_MAGIC_WAND);
    }

}
