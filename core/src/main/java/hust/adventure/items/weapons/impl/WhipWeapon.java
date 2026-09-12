package hust.adventure.items.weapons.impl;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import hust.adventure.collision.CollisionLayer;
import hust.adventure.entities.base.Direction;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.base.Damageable;
import hust.adventure.core.assets.AssetPaths;
import hust.adventure.events.EventDispatcher;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponConfig;
import hust.adventure.items.weapons.WeaponEffectVisitor;

/**
 * A whip weapon that hits enemies in a rectangular area in front of the player. Supports area scaling and burst strikes
 * in alternating directions.
 */
public class WhipWeapon extends BaseWeapon {
    private final Rectangle hitArea;
    private static final float WHIP_WIDTH = 80f;
    private static final float WHIP_HEIGHT = 40f;
    private float flashTimer = 0f;
    private static final float FLASH_DURATION = 0.15f;

    public WhipWeapon(final Player owner, final WeaponConfig config) {
        super(owner, config);
        this.hitArea = new Rectangle();
    }

    @Override
    protected void executeAttackAction() {
        final boolean isSecondStrike = (getAmount() > 1 && getShotsRemaining() == 1);
        updateHitArea(isSecondStrike);
        flashTimer = FLASH_DURATION;
        EventDispatcher.getInstance().playSfx(AssetPaths.SFX_WEAPON_WHIP);

        final Array<MapObject> targets = getOwner().getCollisionManager().getEntitiesInArea(hitArea,
                CollisionLayer.ENEMY);
        for (final MapObject target : targets) {
            if (target instanceof Damageable) {
                ((Damageable) target).takeDamage(getEffectiveDamage());
            }
        }
    }

    @Override
    public void updateTimer(final float delta) {
        super.updateTimer(delta);
        if (flashTimer > 0) {
            flashTimer -= delta;
        }
    }

    public float getFlashTimer() {
        return flashTimer;
    }

    public Rectangle getHitArea() {
        return hitArea;
    }

    private Direction getOppositeDirection(final Direction dir) {
        if (dir == null) {
            return Direction.DOWN;
        }
        switch (dir) {
        case RIGHT:
            return Direction.LEFT;
        case LEFT:
            return Direction.RIGHT;
        case UP:
            return Direction.DOWN;
        case DOWN:
            return Direction.UP;
        default:
            return Direction.DOWN;
        }
    }

    private void updateHitArea(final boolean isSecondStrike) {
        final float px = getOwner().getX();
        final float py = getOwner().getY();
        Direction dir = getOwner().getDirection();
        if (isSecondStrike) {
            dir = getOppositeDirection(dir);
        }

        final float scale = getArea();
        final float w = WHIP_WIDTH * scale;
        final float h = WHIP_HEIGHT * scale;

        switch (dir) {
        case RIGHT:
            hitArea.set(px + getOwner().getWidth() / 2f, py - h / 2f, w, h);
            break;
        case LEFT:
            hitArea.set(px - getOwner().getWidth() / 2f - w, py - h / 2f, w, h);
            break;
        case UP:
            hitArea.set(px - h / 2f, py + getOwner().getHeight() / 2f, h, w);
            break;
        case DOWN:
            hitArea.set(px - h / 2f, py - getOwner().getHeight() / 2f - w, h, w);
            break;
        }
    }

    @Override
    public void accept(final WeaponEffectVisitor visitor) {
        visitor.visit(this);
    }
}
