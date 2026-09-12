package hust.adventure.items.weapons.impl;

import com.badlogic.gdx.utils.Array;
import hust.adventure.collision.CollisionLayer;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.base.Damageable;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponConfig;
import hust.adventure.items.weapons.WeaponEffectVisitor;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;

/**
 * A garlic weapon that creates an aura damaging all nearby enemies periodically. Renders three rotating concentric
 * dashed rings around the player.
 */
public class GarlicAuraWeapon extends BaseWeapon {
    private float rotationAngle = 0f;

    public GarlicAuraWeapon(final Player owner, final WeaponConfig config) {
        super(owner, config);
    }

    @Override
    protected void executeAttackAction() {
        final float radius = getArea(); // Using 'area' stat as radius
        final Array<MapObject> targets = getOwner().getCollisionManager().getEntitiesInRadius(getOwner().getX(),
                getOwner().getY(), radius, CollisionLayer.ENEMY);

        for (final MapObject target : targets) {
            if (target instanceof Damageable) {
                ((Damageable) target).takeDamage(getEffectiveDamage());
            }
        }
        EventDispatcher.getInstance().playSfx(AssetPaths.SFX_WEAPON_GARLIC);
    }

    @Override
    public void updateTimer(final float delta) {
        super.updateTimer(delta);
        rotationAngle += delta * 45f; // rotate 45 degrees per second
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    @Override
    public void accept(final WeaponEffectVisitor visitor) {
        visitor.visit(this);
    }
}
