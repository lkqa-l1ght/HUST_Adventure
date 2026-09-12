package hust.adventure.entities.base;

/**
 * Interface for entities that can take damage and have HP.
 */
public interface Damageable {
    void takeDamage(float damage);
    void takeDamage(float damage, boolean isCrit);
    float getHp();
    float getMaxHp();
    boolean isDead();
}
