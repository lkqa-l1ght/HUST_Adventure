package hust.adventure.entities.enemies;

import com.badlogic.gdx.graphics.Color;

/**
 * Data configuration object representing properties of an enemy type loaded from JSON.
 * Follows strict encapsulation and single responsibility for mapping config data.
 */
public class EnemyConfig {
    private String type;
    private String name;
    private float width;
    private float height;
    private float hitboxWidth;
    private float hitboxHeight;
    private float maxHp;
    private float speed;
    private float contactDamage;
    private boolean boss;

    // Movement & AI Behavior parameters
    private String movementBehavior;
    private String attackBehavior;
    private String subBehavior;
    private float safeDistance;
    private float vx;
    private float vy;
    private float worldWidth;
    private float worldHeight;

    // Combat/Shooting behavior parameters
    private float fireInterval;
    private float projectileSpeed;
    private float projectileDamage;

    // Death/Split behavior parameters
    private String deathBehavior;
    private String splitType;
    private int splitCount;
    private float splitOffset;

    // Sprite & Animation parameters
    private String spritePath;
    private String[] animationFrames;
    private float frameDuration;

    /**
     * Gets the unique identifier of the enemy type.
     *
     * @return the enemy type key (e.g. "syntax_error")
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the unique identifier of the enemy type.
     *
     * @param type the enemy type key
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the display name of the enemy.
     *
     * @return the display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the enemy.
     *
     * @param name the display name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the width of the enemy.
     *
     * @return the width in world coordinates
     */
    public float getWidth() {
        return width;
    }

    /**
     * Sets the width of the enemy.
     *
     * @param width the width in world coordinates
     */
    public void setWidth(final float width) {
        this.width = width;
    }

    /**
     * Gets the height of the enemy.
     *
     * @return the height in world coordinates
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the height of the enemy.
     *
     * @param height the height in world coordinates
     */
    public void setHeight(final float height) {
        this.height = height;
    }

    /**
     * Gets the hitbox width of the enemy. Falls back to width if not specified.
     *
     * @return the hitbox width
     */
    public float getHitboxWidth() {
        return hitboxWidth > 0f ? hitboxWidth : width;
    }

    /**
     * Sets the hitbox width of the enemy.
     *
     * @param hitboxWidth the hitbox width
     */
    public void setHitboxWidth(final float hitboxWidth) {
        this.hitboxWidth = hitboxWidth;
    }

    /**
     * Gets the hitbox height of the enemy. Falls back to height if not specified.
     *
     * @return the hitbox height
     */
    public float getHitboxHeight() {
        return hitboxHeight > 0f ? hitboxHeight : height;
    }

    /**
     * Sets the hitbox height of the enemy.
     *
     * @param hitboxHeight the hitbox height
     */
    public void setHitboxHeight(final float hitboxHeight) {
        this.hitboxHeight = hitboxHeight;
    }

    /**
     * Gets the maximum health of the enemy.
     *
     * @return the maximum health
     */
    public float getMaxHp() {
        return maxHp;
    }

    /**
     * Sets the maximum health of the enemy.
     *
     * @param maxHp the maximum health
     */
    public void setMaxHp(final float maxHp) {
        this.maxHp = maxHp;
    }

    /**
     * Gets the movement speed of the enemy.
     *
     * @return the movement speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets the movement speed of the enemy.
     *
     * @param speed the movement speed
     */
    public void setSpeed(final float speed) {
        this.speed = speed;
    }

    /**
     * Gets the contact damage dealt by this enemy to the player.
     *
     * @return the contact damage
     */
    public float getContactDamage() {
        return contactDamage;
    }

    /**
     * Sets the contact damage dealt by this enemy to the player.
     *
     * @param contactDamage the contact damage
     */
    public void setContactDamage(final float contactDamage) {
        this.contactDamage = contactDamage;
    }

    /**
     * Resolves the color of the enemy.
     *
     * @return the libGDX Color representation
     */
    public Color getColor() {
        return Color.ORANGE;
    }

    /**
     * Checks if this enemy is classified as a boss.
     *
     * @return true if boss, false otherwise
     */
    public boolean isBoss() {
        return boss;
    }

    /**
     * Sets whether this enemy is classified as a boss.
     *
     * @param boss true if boss, false otherwise
     */
    public void setBoss(final boolean boss) {
        this.boss = boss;
    }

    /**
     * Gets the identifier of the movement/AI behavior.
     *
     * @return the movement behavior type key (e.g. "chase")
     */
    public String getMovementBehavior() {
        return movementBehavior;
    }

    /**
     * Sets the identifier of the movement/AI behavior.
     *
     * @param movementBehavior the movement behavior type key
     */
    public void setMovementBehavior(final String movementBehavior) {
        this.movementBehavior = movementBehavior;
    }

    /**
     * Gets the identifier of the attack behavior.
     *
     * @return the attack behavior type key (e.g. "shooting")
     */
    public String getAttackBehavior() {
        return attackBehavior;
    }

    /**
     * Sets the identifier of the attack behavior.
     *
     * @param attackBehavior the attack behavior type key
     */
    public void setAttackBehavior(final String attackBehavior) {
        this.attackBehavior = attackBehavior;
    }

    /**
     * Gets the identifier of the nested/sub behavior (e.g., movement inside shooting).
     *
     * @return the sub-behavior type key
     */
    public String getSubBehavior() {
        return subBehavior;
    }

    /**
     * Sets the identifier of the nested/sub behavior.
     *
     * @param subBehavior the sub-behavior type key
     */
    public void setSubBehavior(final String subBehavior) {
        this.subBehavior = subBehavior;
    }

    /**
     * Gets the safe distance threshold for flee behaviors.
     *
     * @return the safe distance
     */
    public float getSafeDistance() {
        return safeDistance;
    }

    /**
     * Sets the safe distance threshold for flee behaviors.
     *
     * @param safeDistance the safe distance
     */
    public void setSafeDistance(final float safeDistance) {
        this.safeDistance = safeDistance;
    }

    /**
     * Gets the horizontal velocity parameter for bouncing behavior.
     *
     * @return the horizontal velocity
     */
    public float getVx() {
        return vx;
    }

    /**
     * Sets the horizontal velocity parameter for bouncing behavior.
     *
     * @param vx the horizontal velocity
     */
    public void setVx(final float vx) {
        this.vx = vx;
    }

    /**
     * Gets the vertical velocity parameter for bouncing behavior.
     *
     * @return the vertical velocity
     */
    public float getVy() {
        return vy;
    }

    /**
     * Sets the vertical velocity parameter for bouncing behavior.
     *
     * @param vy the vertical velocity
     */
    public void setVy(final float vy) {
        this.vy = vy;
    }

    /**
     * Gets the world width boundary for bouncing behavior.
     *
     * @return the world width
     */
    public float getWorldWidth() {
        return worldWidth;
    }

    /**
     * Sets the world width boundary for bouncing behavior.
     *
     * @param worldWidth the world width
     */
    public void setWorldWidth(final float worldWidth) {
        this.worldWidth = worldWidth;
    }

    /**
     * Gets the world height boundary for bouncing behavior.
     *
     * @return the world height
     */
    public float getWorldHeight() {
        return worldHeight;
    }

    /**
     * Sets the world height boundary for bouncing behavior.
     *
     * @param worldHeight the world height
     */
    public void setWorldHeight(final float worldHeight) {
        this.worldHeight = worldHeight;
    }

    /**
     * Gets the interval in seconds between weapon discharges for shooting behavior.
     *
     * @return the firing interval
     */
    public float getFireInterval() {
        return fireInterval;
    }

    /**
     * Sets the interval in seconds between weapon discharges for shooting behavior.
     *
     * @param fireInterval the firing interval
     */
    public void setFireInterval(final float fireInterval) {
        this.fireInterval = fireInterval;
    }

    /**
     * Gets the projectile velocity/speed for shooting behavior.
     *
     * @return the projectile speed
     */
    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    /**
     * Sets the projectile velocity/speed for shooting behavior.
     *
     * @param projectileSpeed the projectile speed
     */
    public void setProjectileSpeed(final float projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    /**
     * Gets the damage dealt by the projectiles fired by this enemy.
     *
     * @return the projectile damage
     */
    public float getProjectileDamage() {
        return projectileDamage;
    }

    /**
     * Sets the damage dealt by the projectiles fired by this enemy.
     *
     * @param projectileDamage the projectile damage
     */
    public void setProjectileDamage(final float projectileDamage) {
        this.projectileDamage = projectileDamage;
    }

    /**
     * Gets the identifier of the death behavior.
     *
     * @return the death behavior key (e.g. "split")
     */
    public String getDeathBehavior() {
        return deathBehavior;
    }

    /**
     * Sets the identifier of the death behavior.
     *
     * @param deathBehavior the death behavior key
     */
    public void setDeathBehavior(final String deathBehavior) {
        this.deathBehavior = deathBehavior;
    }

    /**
     * Gets the enemy type spawned upon split.
     *
     * @return the split child enemy type key
     */
    public String getSplitType() {
        return splitType;
    }

    /**
     * Sets the enemy type spawned upon split.
     *
     * @param splitType the split child enemy type key
     */
    public void setSplitType(final String splitType) {
        this.splitType = splitType;
    }

    /**
     * Gets the number of children spawned on split.
     *
     * @return the number of split children
     */
    public int getSplitCount() {
        return splitCount;
    }

    /**
     * Sets the number of children spawned on split.
     *
     * @param splitCount the number of split children
     */
    public void setSplitCount(final int splitCount) {
        this.splitCount = splitCount;
    }

    /**
     * Gets the offset distance applied when spawning split children.
     *
     * @return the split offset distance
     */
    public float getSplitOffset() {
        return splitOffset;
    }

    /**
     * Sets the offset distance applied when spawning split children.
     *
     * @param splitOffset the split offset distance
     */
    public void setSplitOffset(final float splitOffset) {
        this.splitOffset = splitOffset;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public void setSpritePath(final String spritePath) {
        this.spritePath = spritePath;
    }

    public String[] getAnimationFrames() {
        return animationFrames;
    }

    public void setAnimationFrames(final String[] animationFrames) {
        this.animationFrames = animationFrames;
    }

    public float getFrameDuration() {
        return frameDuration;
    }

    public void setFrameDuration(final float frameDuration) {
        this.frameDuration = frameDuration;
    }
}
