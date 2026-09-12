package hust.adventure.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import hust.adventure.entities.player.Player;
import hust.adventure.items.weapons.WeaponEffectVisitor;
import hust.adventure.items.weapons.impl.GarlicAuraWeapon;
import hust.adventure.items.weapons.impl.WhipWeapon;

/**
 * Presentation-layer implementation of weapon effect rendering.
 * Centralizes all ShapeDrawUtils calls for weapon visuals.
 */
public class GameWeaponRenderer implements WeaponEffectVisitor {
    private static final Color GARLIC_RING_INNER = new Color(0.85f, 0.95f, 0.75f, 0.3f);
    private static final Color GARLIC_RING_MID = new Color(0.85f, 0.95f, 0.75f, 0.25f);
    private static final Color GARLIC_RING_OUTER = new Color(0.85f, 0.95f, 0.75f, 0.15f);
    private static final Color WHIP_FLASH_COLOR = new Color(1, 1, 1, 0.5f);

    private SpriteBatch batch;
    private Player owner;

    /**
     * Prepares renderer for current frame. Call before iterating weapons.
     *
     * @param batch active SpriteBatch
     * @param owner weapon owner (player)
     */
    public void begin(final SpriteBatch batch, final Player owner) {
        this.batch = batch;
        this.owner = owner;
    }

    @Override
    public void visit(final GarlicAuraWeapon weapon) {
        final float px = owner.getX();
        final float py = owner.getY();
        final float baseRadius = weapon.getArea();
        final float rotationAngle = weapon.getRotationAngle();
        ShapeDrawUtils.drawDashedCircle(batch, px, py, baseRadius * 0.6f, rotationAngle, GARLIC_RING_INNER);
        ShapeDrawUtils.drawDashedCircle(batch, px, py, baseRadius * 0.8f, -rotationAngle * 0.7f, GARLIC_RING_MID);
        ShapeDrawUtils.drawDashedCircle(batch, px, py, baseRadius * 1.0f, rotationAngle * 0.4f, GARLIC_RING_OUTER);
    }

    @Override
    public void visit(final WhipWeapon weapon) {
        if (weapon.getFlashTimer() > 0) {
            final com.badlogic.gdx.math.Rectangle hitArea = weapon.getHitArea();
            com.badlogic.gdx.graphics.Texture weaponTexture = null;
            if (owner != null && owner.getProgressContext() != null) {
                // Fallback texture grab via progress context
                try {
                    // Let's use HustGame assetManager if we can find it
                    final hust.adventure.HustGame game = (hust.adventure.HustGame) com.badlogic.gdx.Gdx.app.getApplicationListener();
                    weaponTexture = game.getAssetManager().getTexture("items/giai_tich.png");
                } catch (Exception e) {
                    // Safe catch
                }
            }

            if (weaponTexture != null) {
                batch.draw(weaponTexture, hitArea.x, hitArea.y, hitArea.width, hitArea.height);
            } else {
                ShapeDrawUtils.drawRect(batch, hitArea.x, hitArea.y, hitArea.width, hitArea.height, WHIP_FLASH_COLOR);
            }
        }
    }
}
