package hust.adventure.entities.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import hust.adventure.collision.CollisionManager;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.behavior.BehaviorRegistry;
import hust.adventure.behavior.EnemyBehaviors;
import hust.adventure.behavior.ai.EnemySeparationResolver;
import hust.adventure.behavior.attack.AttackBehavior;
import hust.adventure.behavior.death.DeathBehavior;
import hust.adventure.collision.Collider;

import hust.adventure.entities.EntityManager;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.base.Damageable;

/**
 * Base class for all enemy types. Inherits core living entity logic from BaseActor.
 */
public class Enemy extends Character {
    private static final float HP_BAR_OFFSET_Y = 5f;
    private static final float HP_BAR_HEIGHT = 5f;
    private static final float NAME_TEXT_OFFSET_Y = 25f;
    private static final float FLASHLIGHT_RADIUS_SQ = 10000f; // 100f * 100f

    private Color color;
    private CollisionManager collisionManager;
    private EntityFactory factory;
    private AttackBehavior attackBehavior;
    private float contactDamage;
    private boolean split;
    private boolean boss;
    private DeathBehavior deathBehavior;
    private boolean checkWallCollisions = true;

    private final Player player;
    private final EntityManager entityManager;
    private final GameProgressContext progressContext;

    /**
     * Constructs a BaseEnemy from its configuration data.
     *
     * @param x                horizontal spawn position
     * @param y                vertical spawn position
     * @param collisionManager standard collision manager
     * @param config           the loaded configuration parameters
     * @param player           the player dependency
     * @param entityManager    the entity manager dependency
     */
    @lombok.Builder
    public Enemy(final float x, final float y, final CollisionManager collisionManager, final EnemyConfig config,
            final Player player, final EntityManager entityManager, final GameProgressContext progressContext) {
        super(x, y, config.getWidth(), config.getHeight(), config.getHitboxWidth(), config.getHitboxHeight(), config.getMaxHp());
        this.player = player;
        this.entityManager = entityManager;
        this.progressContext = progressContext;
        setId(config.getType());
        init(x, y, config.getWidth(), config.getHeight(), config.getHitboxWidth(), config.getHitboxHeight(), config.getMaxHp(), config.getName(), config.getColor(),
                collisionManager, config.getContactDamage());
        setSpeed(config.getSpeed());
        this.boss = config.isBoss();

        final EnemyBehaviors behaviors = BehaviorRegistry.create(config);
        setMovementBehavior(behaviors.getMovementBehavior());
        setAttackBehavior(behaviors.getAttackBehavior());
        setDeathBehavior(behaviors.getDeathBehavior());
    }

    /**
     * Sets state for the enemy.
     */
    public void init(final float x, final float y, final float w, final float h, final float hitboxW, final float hitboxH, final float maxHp, final String name,
            final Color color, final CollisionManager collisionManager, final float contactDamage) {
        this.width = w;
        this.height = h;
        this.hitboxWidth = hitboxW;
        this.hitboxHeight = hitboxH;
        this.collisionManager = collisionManager;

        float clampedX = x;
        float clampedY = y;
        if (collisionManager != null && collisionManager.getMapWidth() > 0 && !collisionManager.isInfinite()) {
            final float minX = hitboxW / 2f;
            final float maxX = collisionManager.getMapWidth() - hitboxW / 2f;
            final float minY = hitboxH / 2f;
            final float maxY = collisionManager.getMapHeight() - hitboxH / 2f;

            clampedX = Math.max(minX, Math.min(maxX, clampedX));
            clampedY = Math.max(minY, Math.min(maxY, clampedY));
        }

        this.checkWallCollisions = false;
        setX(clampedX);
        setY(clampedY);
        this.checkWallCollisions = true;
        this.setMaxHp(maxHp);
        this.setHp(maxHp);
        setName(name);
        this.color = color;
        this.contactDamage = contactDamage;
        setDestroyed(false);
    }

    @Override
    public void setCollider(Collider collider) {
        super.setCollider(collider);
        if (collider != null) {
            collider.setListener(other -> {
                if (contactDamage > 0 && other instanceof Damageable) {
                    ((Damageable) other).takeDamage(contactDamage);
                }
            });
        }
    }

    @Override
    public void update(final float delta) {
        final float enemyTimeScale = (progressContext != null) ? progressContext.getEnemyTimeScale() : 1.0f;
        final float virtualDelta = delta * enemyTimeScale;
        super.update(virtualDelta);
        if (isDead()) {
            destroy();
            return;
        }

        if (attackBehavior != null) {
            attackBehavior.execute(this, virtualDelta, player, entityManager);
        }

        EnemySeparationResolver.resolve(this, entityManager);

        // Clamp to map boundaries after movement updates
        if (collisionManager != null && collisionManager.getMapWidth() > 0 && !collisionManager.isInfinite()) {
            final float minX = getHitboxWidth() / 2f;
            final float maxX = collisionManager.getMapWidth() - getHitboxWidth() / 2f;
            final float minY = getHitboxHeight() / 2f;
            final float maxY = collisionManager.getMapHeight() - getHitboxHeight() / 2f;

            this.checkWallCollisions = false;
            if (getX() < minX) {
                setX(minX);
            } else if (getX() > maxX) {
                setX(maxX);
            }

            if (getY() < minY) {
                setY(minY);
            } else if (getY() > maxY) {
                setY(maxY);
            }
            this.checkWallCollisions = true;
        }
    }

    /**
     * Sets the horizontal position of the enemy, checking for wall collisions if enabled.
     *
     * @param x the new horizontal coordinate
     */
    @Override
    public void setX(final float x) {
        if (checkWallCollisions && collisionManager != null && !collisionManager.canMove(this, x, getY())) {
            return;
        }
        super.setX(x);
    }

    /**
     * Sets the vertical position of the enemy, checking for wall collisions if enabled.
     *
     * @param y the new vertical coordinate
     */
    @Override
    public void setY(final float y) {
        if (checkWallCollisions && collisionManager != null && !collisionManager.canMove(this, getX(), y)) {
            return;
        }
        super.setY(y);
    }

    public final Player getPlayer() {
        return player;
    }

    public final EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Sets the attack behavior strategy.
     *
     * @param attackBehavior the attack behavior strategy
     */
    public final void setAttackBehavior(final AttackBehavior attackBehavior) {
        this.attackBehavior = attackBehavior;
    }

    @Override
    public final void draw(final SpriteBatch batch) {
        if (isDead())
            return;

        // Flashlight culling in lights out mode (if showEnemiesTimer / radar is not active)
        final boolean lightsOut = (progressContext != null) ? progressContext.isLightsOut() : false;
        final float showEnemiesTimer = (progressContext != null) ? progressContext.getShowEnemiesTimer() : 0f;
        if (lightsOut && showEnemiesTimer <= 0f) {
            final Player p = player;
            if (p != null) {
                final float dx = getX() - p.getX();
                final float dy = getY() - p.getY();
                if ((dx * dx + dy * dy) > FLASHLIGHT_RADIUS_SQ) {
                    return; // Skip rendering
                }
            }
        }

        // Blink effect: skip render khi enemyBlinkVisible == false
        final boolean blinkVisible = (progressContext == null) || progressContext.isEnemyBlinkVisible();
        if (!blinkVisible)
            return;

        renderSpecific(batch);
    }

    /**
     * Renders the enemy's visual sprite or geometry. Default implementation draws a colored rectangle. Override for
     * custom sprites.
     *
     * @param batch the active SpriteBatch
     */
    protected void renderSpecific(final SpriteBatch batch) {
        if (hasSprite()) {
            TextureRegion frame = null;
            if (animation != null) {
                frame = animation.getKeyFrame(stateTime, true);
            } else if (staticSprite != null) {
                frame = staticSprite;
            }
            if (frame != null) {
                batch.draw(frame, getX() - getWidth() / 2f, getY() - getHeight() / 2f, getWidth(), getHeight());
            }
        }
    }

    public void drawDebug(final ShapeRenderer sr, final SpriteBatch batch, final BitmapFont font) {
        sr.setColor(color);
        sr.rect(getX(), getY(), getWidth(), getHeight());

        // HP Bar
        sr.setColor(Color.GREEN);
        sr.rect(getX(), getY() + getHeight() + HP_BAR_OFFSET_Y, (getHp() / getMaxHp()) * getWidth(), HP_BAR_HEIGHT);

        // Note: batch.begin()/end() should be called by the caller of this method
        // to avoid multiple flushes during batch processing of multiple entities.
        font.draw(batch, getName(), getX(), getY() + getHeight() + NAME_TEXT_OFFSET_Y);
    }

    public final Color getColor() {
        return color;
    }

    @Override
    public Color getShapeFallbackColor() {
        if (!hasSprite()) {
            return color;
        }
        return null;
    }

    public final CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public final void setFactory(final EntityFactory factory) {
        this.factory = factory;
    }

    public final EntityFactory getFactory() {
        return factory;
    }

    /**
     * Checks if this enemy is a child split of another enemy.
     *
     * @return true if it is a split child, false otherwise
     */
    public final boolean isSplit() {
        return split;
    }

    /**
     * Sets whether this enemy is a child split of another enemy.
     *
     * @param split true if split child, false otherwise
     */
    public final void setSplit(final boolean split) {
        this.split = split;
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
    public final void setBoss(final boolean boss) {
        this.boss = boss;
    }

    /**
     * Sets the death behavior for this enemy.
     *
     * @param deathBehavior the death behavior to execute on destruction
     */
    public final void setDeathBehavior(final DeathBehavior deathBehavior) {
        this.deathBehavior = deathBehavior;
    }

    @Override
    public void destroy() {
        if (isDestroyed()) {
            return;
        }
        super.destroy();
        if (deathBehavior != null) {
            deathBehavior.onDestroy(this);
        }
    }

}
