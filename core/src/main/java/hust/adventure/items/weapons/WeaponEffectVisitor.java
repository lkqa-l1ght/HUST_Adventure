package hust.adventure.items.weapons;

import hust.adventure.items.weapons.impl.GarlicAuraWeapon;
import hust.adventure.items.weapons.impl.WhipWeapon;

/**
 * Visitor for rendering weapon-specific visual effects.
 * Implemented in the presentation layer to keep domain free of graphics dependencies.
 *
 * <p>Default methods are no-op so new weapon types don't require immediate visitor changes.</p>
 */
public interface WeaponEffectVisitor {
    /**
     * Visits a GarlicAuraWeapon to render its visual effect.
     *
     * @param weapon the garlic aura weapon
     */
    default void visit(final GarlicAuraWeapon weapon) {}

    /**
     * Visits a WhipWeapon to render its visual effect.
     *
     * @param weapon the whip weapon
     */
    default void visit(final WhipWeapon weapon) {}
}
