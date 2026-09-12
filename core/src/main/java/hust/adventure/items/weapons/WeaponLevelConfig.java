package hust.adventure.items.weapons;

/**
 * Data Object representing stat overrides for a specific weapon level.
 */
public class WeaponLevelConfig {
    private float baseDamage;
    private float cooldown;
    private float area;
    private int amount;
    private int pierce;

    public float getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(final float baseDamage) {
        this.baseDamage = baseDamage;
    }

    public float getCooldown() {
        return cooldown;
    }

    public void setCooldown(final float cooldown) {
        this.cooldown = cooldown;
    }

    public float getArea() {
        return area;
    }

    public void setArea(final float area) {
        this.area = area;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public int getPierce() {
        return pierce;
    }

    public void setPierce(final int pierce) {
        this.pierce = pierce;
    }
}
