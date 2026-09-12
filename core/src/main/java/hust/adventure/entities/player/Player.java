package hust.adventure.entities.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import hust.adventure.entities.ExpGem;
import hust.adventure.entities.base.Character;
import hust.adventure.entities.base.Targetable;
import hust.adventure.entities.state.MovingState;
import hust.adventure.inventory.Inventory;
import hust.adventure.items.gear.GearManager;
import hust.adventure.items.weapons.BaseWeapon;
import hust.adventure.items.weapons.WeaponManager;
import hust.adventure.behavior.SpellController;
import hust.adventure.behavior.movement.PlayerMovementBehavior;
import hust.adventure.collision.CollisionLayer;
import hust.adventure.collision.CollisionManager;
import hust.adventure.core.assets.GameAssetManager;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.core.context.PlayerStats;
import hust.adventure.entities.factory.EntityFactory;
import hust.adventure.entities.player.input.PlayerController;
import hust.adventure.entities.base.MapObject;

/**
 * Main player character class.
 */
public class Player extends Character implements Targetable {
    private final Inventory inventory;
    private final PlayerController controller;
    private final WeaponManager weaponManager;
    private final GearManager gearManager;
    private CollisionManager collisionManager;
    private EntityFactory entityFactory;
    private final PlayerStats stats;
    private final GameProgressContext progressContext;
    private final PlayerPersistenceService persistenceService;

    private Animation<TextureRegion> walkLeft, walkRight, walkDown, walkUp;
    private TextureRegion idleDown, idleUp, idleLeft, idleRight;
    private Animation<TextureRegion> lastAnim = null;
    private float stateTime = 0f;

    private float iframeTimer = 0f;
    private static final float IFRAME_DURATION = 0.5f;

    private static final float DRAW_WIDTH = 34f;
    private static final float DRAW_HEIGHT = 74f;
    private static final float HITBOX_WIDTH = 30f;
    private static final float HITBOX_HEIGHT = 70f;
    private static final float MAX_HP = 300f;

    // Delegated controllers and handlers
    private final SpellController spellController;
    private final PlayerEventHandler eventHandler;

    // Multipliers for data-driven gears
    private float powerMultiplier = 1.0f;
    private float cooldownMultiplier = 1.0f;
    private float areaMultiplier = 1.0f;
    private float magnetMultiplier = 1.0f;
    private float speedMultiplier = 1.0f;

    @lombok.Builder
    public Player(final float startX, final float startY, final Inventory inventory, final PlayerController controller,
            final CollisionManager collisionManager, final GameAssetManager assetManager, final PlayerStats stats,
            final GameProgressContext progressContext, final PlayerPersistenceService persistenceService) {
        super(startX, startY, DRAW_WIDTH, DRAW_HEIGHT, HITBOX_WIDTH, HITBOX_HEIGHT, MAX_HP);
        this.stats = stats;
        this.progressContext = progressContext;
        this.persistenceService = persistenceService;
        setId("player");
        setName("Player");
        setSpritePath("player_textures");
        this.inventory = inventory;
        this.controller = controller;
        this.collisionManager = collisionManager;
        this.weaponManager = new WeaponManager(this);
        this.gearManager = new GearManager();
        this.iframeTimer = 0f;

        // Restore weapons and gears from global context via persistence service
        if (persistenceService != null) {
            persistenceService.restore(this);
        }

        // Composition: Movement behavior
        this.setMovementBehavior(new PlayerMovementBehavior(controller, collisionManager));
        loadTextures(assetManager);

        // Delegate event handling
        this.eventHandler = new PlayerEventHandler(this);

        // Initialize spell controller
        this.spellController = new SpellController(progressContext);

        progressContext.setPlayer(this);
    }

    private void loadTextures(final GameAssetManager assetManager) {
        final Texture atlasTexture = assetManager.getTexture("character/atlas.png");
        final JsonReader reader = new JsonReader();
        final JsonValue root = reader.parse(com.badlogic.gdx.Gdx.files.internal("character/atlas.json"));
        final JsonValue framesValue = root.get("frames");

        idleDown = getAtlasRegion(atlasTexture, framesValue, "rotations/south.png");
        idleUp = getAtlasRegion(atlasTexture, framesValue, "rotations/north.png");
        idleRight = getAtlasRegion(atlasTexture, framesValue, "rotations/east.png");
        idleLeft = getAtlasRegion(atlasTexture, framesValue, "rotations/west.png");

        walkDown = createWalkAnimation(atlasTexture, framesValue, "south");
        walkUp = createWalkAnimation(atlasTexture, framesValue, "north");
        walkRight = createWalkAnimation(atlasTexture, framesValue, "east");
        walkLeft = createWalkAnimation(atlasTexture, framesValue, "west");
    }

    private TextureRegion getAtlasRegion(final Texture atlasTexture, final JsonValue framesValue, final String path) {
        final JsonValue frameVal = framesValue.get(path);
        if (frameVal == null) {
            throw new GdxRuntimeException("Frame not found in atlas: " + path);
        }
        final JsonValue f = frameVal.get("frame");
        return new TextureRegion(atlasTexture, f.getInt("x"), f.getInt("y"), f.getInt("w"), f.getInt("h"));
    }

    private Animation<TextureRegion> createWalkAnimation(final Texture atlasTexture, final JsonValue framesValue,
            final String direction) {
        final Array<TextureRegion> animFrames = new Array<>();
        for (int i = 0; i <= 5; i++) {
            final String path = "animations/Walking-c046985c/" + direction + "/frame_00" + i + ".png";
            animFrames.add(getAtlasRegion(atlasTexture, framesValue, path));
        }
        return new Animation<>(0.1f, animFrames);
    }

    @Override
    public void update(final float delta) {
        if (progressContext.isGodMode()) {
            setHp(getMaxHp());
        }
        if (iframeTimer > 0) {
            iframeTimer = Math.max(0f, iframeTimer - delta);
        }

        // Update spells through the delegated controller
        spellController.update(this, controller, delta);

        if (progressContext.getPlayer() != this) {
            progressContext.setPlayer(this);
        }

        setSpeedMultiplier(speedMultiplier);

        super.update(delta);

        // Magnetic radius for ExpGem
        if (collisionManager != null) {
            final float magnetRadius = 150f * getMagnetMultiplier();
            Array<MapObject> items = collisionManager.getEntitiesInRadius(getX(), getY(), magnetRadius,
                    CollisionLayer.ITEM);
            for (MapObject item : items) {
                if (item instanceof ExpGem) {
                    ((ExpGem) item).setTarget(this);
                }
            }
        }

        if (getState() instanceof MovingState) {
            Animation<TextureRegion> anim;
            switch (getDirection()) {
            case RIGHT:
                anim = walkRight;
                break;
            case LEFT:
                anim = walkLeft;
                break;
            case UP:
                anim = walkUp;
                break;
            default:
                anim = walkDown;
                break;
            }
            if (anim != lastAnim) {
                stateTime = 0f;
                lastAnim = anim;
            }
            stateTime += delta;
        }

        if (controller.isSpaceJustPressed()) {
            boolean allowed = true;
            if (progressContext != null) {
                allowed = progressContext.isAutoAttackAllowed();
            }
            if (allowed) {
                for (final BaseWeapon weapon : weaponManager.getWeapons()) {
                    if (!weapon.isAutoFiring()) {
                        weapon.fire();
                    }
                }
            }
        }

        weaponManager.update(delta);
    }

    @Override
    public void draw(final SpriteBatch batch) {
        TextureRegion frame;
        if (getState() instanceof MovingState && lastAnim != null) {
            frame = lastAnim.getKeyFrame(stateTime, true);
        } else {
            switch (getDirection()) {
            case RIGHT:
                frame = idleRight;
                break;
            case LEFT:
                frame = idleLeft;
                break;
            case UP:
                frame = idleUp;
                break;
            default:
                frame = idleDown;
                break;
            }
        }
        float oldAlpha = batch.getColor().a;
        if (iframeTimer > 0) {
            float alpha = 0.5f + 0.3f * (float) Math.sin(iframeTimer * 30f);
            Color color = batch.getColor();
            batch.setColor(color.r, color.g, color.b, alpha);
        }
        batch.draw(frame, getX() - getWidth() / 2f, getY() - getHeight() / 2f, getWidth(), getHeight());
        if (iframeTimer > 0) {
            Color color = batch.getColor();
            batch.setColor(color.r, color.g, color.b, oldAlpha);
        }
    }

    public void setCollisionManager(final CollisionManager newManager) {
        this.collisionManager = newManager;
        if (getMovementBehavior() instanceof PlayerMovementBehavior) {
            ((PlayerMovementBehavior) getMovementBehavior()).setCollisionManager(newManager);
        }
    }

    public final Inventory getInventory() {
        return inventory;
    }

    public final PlayerController getController() {
        return controller;
    }

    public final CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public final WeaponManager getWeaponManager() {
        return weaponManager;
    }

    public final GearManager getGearManager() {
        return gearManager;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    public float getCooldownMultiplier() {
        return cooldownMultiplier;
    }

    public float getAreaMultiplier() {
        return areaMultiplier;
    }

    public float getMagnetMultiplier() {
        return magnetMultiplier;
    }

    public void addPowerMultiplier(final float amount) {
        this.powerMultiplier += amount;
    }

    public void addCooldownMultiplier(final float amount) {
        this.cooldownMultiplier = Math.max(0.2f, this.cooldownMultiplier + amount);
    }

    public void addAreaMultiplier(final float amount) {
        this.areaMultiplier += amount;
    }

    public void addMagnetMultiplier(final float amount) {
        this.magnetMultiplier += amount;
    }

    public void addSpeedMultiplier(final float amount) {
        this.speedMultiplier += amount;
    }

    public void increaseMaxHp(final float amount) {
        final float oldMaxHp = getMaxHp();
        final float newMaxHp = oldMaxHp + amount;
        setMaxHp(newMaxHp);
        heal(amount);
    }

    @Override
    public float getHp() {
        return stats != null ? stats.getHp() : super.getHp();
    }

    @Override
    public float getMaxHp() {
        return stats != null ? stats.getMaxHp() : super.getMaxHp();
    }

    @Override
    public void setHp(final float hp) {
        if (stats != null) {
            stats.setHp(hp);
        } else {
            super.setHp(hp);
        }
    }

    @Override
    public void setMaxHp(final float maxHp) {
        if (stats != null) {
            stats.setMaxHp(maxHp);
        } else {
            super.setMaxHp(maxHp);
        }
    }

    public float getStamina() {
        return stats != null ? stats.getStamina() : 100f;
    }

    public void setStamina(final float stamina) {
        if (stats != null) {
            stats.setStamina(stamina);
        }
    }

    public float getMaxStamina() {
        return stats != null ? stats.getMaxStamina() : 100f;
    }

    public void setMaxStamina(final float maxStamina) {
        if (stats != null) {
            stats.setMaxStamina(maxStamina);
        }
    }

    public void restoreStamina(final float amount) {
        if (stats != null) {
            stats.setStamina(stats.getStamina() + amount);
        }
    }

    public void setFactory(final EntityFactory factory) {
        this.entityFactory = factory;
    }

    public EntityFactory getFactory() {
        return entityFactory;
    }

    @Override
    public void takeDamage(final float amount) {
        takeDamage(amount, false, false);
    }

    @Override
    public void takeDamage(final float amount, final boolean isCrit) {
        takeDamage(amount, isCrit, false);
    }

    public void takeDamage(final float amount, final boolean isCrit, final boolean ignoreIFrames) {
        if (progressContext.isGodMode()) {
            return;
        }
        if (!ignoreIFrames && iframeTimer > 0) {
            return;
        }
        super.takeDamage(amount, isCrit);
        if (!ignoreIFrames) {
            iframeTimer = IFRAME_DURATION;
        }
    }

    public float getIframeTimer() {
        return iframeTimer;
    }

    public void setIframeTimer(final float iframeTimer) {
        this.iframeTimer = iframeTimer;
    }

    public SpellController getSpellController() {
        return spellController;
    }

    public GameProgressContext getProgressContext() {
        return progressContext;
    }

    @Override
    public void dispose() {
        progressContext.setPlayer(null);
        if (eventHandler != null) {
            eventHandler.dispose();
        }
    }
}
