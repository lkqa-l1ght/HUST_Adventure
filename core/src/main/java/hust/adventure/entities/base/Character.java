package hust.adventure.entities.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import hust.adventure.behavior.movement.MovementBehavior;
import hust.adventure.effects.StatusEffectManager;
import hust.adventure.events.EntityDamagedEvent;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;

/**
 * Base class for all living and moving entities. Combines positioning with health and movement logic.
 */
public abstract class Character extends MapObject implements Damageable {
    private float maxHp;
    private float hp;
    private float speed;
    private float speedMultiplier;
    private Direction direction;
    private MovementBehavior movementBehavior;
    private StatusEffectManager statusEffectManager;

    public Character() {
        super();
        this.statusEffectManager = new StatusEffectManager(this);
        this.speedMultiplier = 1.0f;
        this.direction = Direction.DOWN;
    }

    public Character(final float x, final float y, final float width, final float height, final float maxHp) {
        this(x, y, width, height, width, height, maxHp);
    }

    public Character(final float x, final float y, final float width, final float height, final float hitboxWidth, final float hitboxHeight, final float maxHp) {
        super(x, y, width, height, hitboxWidth, hitboxHeight);
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = 80f; // Default speed
        this.speedMultiplier = 1.0f;
        this.direction = Direction.DOWN;
        this.statusEffectManager = new StatusEffectManager(this);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (isDead())
            return;

        if (movementBehavior != null) {
            movementBehavior.update(this, delta);
        }

        statusEffectManager.update(delta);
    }

    @Override
    public void takeDamage(final float amount) {
        takeDamage(amount, false);
    }

    @Override
    public void takeDamage(final float amount, final boolean isCrit) {
        if (isDead())
            return;

        if (amount < 0)
            throw new IllegalArgumentException("Damage amount cannot be negative");
        setHp(getHp() - amount);

        EntityDamagedEvent eventData = new EntityDamagedEvent(this, amount, isCrit);
        GameEvent<EntityDamagedEvent> event = new GameEvent<>(EventType.ENTITY_DAMAGED, eventData);
        EventDispatcher.getInstance().dispatch(event);

        if (isDead()) {
            GameEvent<MapObject> deathEvent = new GameEvent<>(EventType.ENTITY_DIED, this);
            EventDispatcher.getInstance().dispatch(deathEvent);
            destroy();
        }
    }

    @Override
    public boolean isDead() {
        return getHp() <= 0;
    }

    @Override
    public float getHp() {
        return hp;
    }

    @Override
    public float getMaxHp() {
        return maxHp;
    }

    protected void setHp(final float hp) {
        this.hp = Math.max(0, Math.min(getMaxHp(), hp));
    }

    protected void setMaxHp(final float maxHp) {
        this.maxHp = maxHp;
        this.hp = Math.min(this.hp, this.maxHp);
    }

    public final void heal(final float amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Heal amount cannot be negative");
        setHp(getHp() + amount);
    }

    public final float getSpeed() {
        return speed * speedMultiplier;
    }

    public final float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public final void setSpeedMultiplier(final float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public final void setSpeed(final float speed) {
        this.speed = speed;
    }

    public final Direction getDirection() {
        return direction;
    }

    public final void setDirection(final Direction direction) {
        if (direction == null)
            throw new NullPointerException("Direction cannot be null");
        this.direction = direction;
    }

    public final MovementBehavior getMovementBehavior() {
        return movementBehavior;
    }

    public final void setMovementBehavior(final MovementBehavior behavior) {
        this.movementBehavior = behavior;
    }

    public final boolean hasStatus(final StatusFlag flag) {
        return statusEffectManager.hasStatus(flag);
    }

    public final StatusEffectManager getStatusEffectManager() {
        return statusEffectManager;
    }

    private static final float FEET_WIDTH_RATIO = 0.4f;
    private static final float FEET_HEIGHT_RATIO = 0.2f;

    @Override
    public Rectangle getMovementBounds(float x, float y, Rectangle out) {
        final float feetWidth = getHitboxWidth() * FEET_WIDTH_RATIO;
        final float feetHeight = getHitboxHeight() * FEET_HEIGHT_RATIO;
        return out.set(x - feetWidth / 2f, y - getHitboxHeight() / 2f, feetWidth, feetHeight);
    }

    @Override
    public abstract void draw(SpriteBatch batch);

}
