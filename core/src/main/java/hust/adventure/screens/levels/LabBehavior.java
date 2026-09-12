package hust.adventure.screens.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.core.context.GameProgressContext;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ItemPickedUpEvent;
import hust.adventure.progression.MapDirector;
import hust.adventure.screens.PlayScreen;

/**
 * Behavior class for the Computer Lab level (Map 4).
 * Coordinates escalating Bug Deadlines, dynamic lighting and blink mechanics, and Source Code USB gating.
 */
public class LabBehavior implements LevelBehavior {
    private static final int MAX_WAVE = 5;
    // Central floor position in front of GotoBoss portal on lab.tmx (1088x928)
    private static final float SPAWN_USB_X = 550f;
    private static final float SPAWN_USB_Y = 280f;

    private static final int LIGHTS_OUT_WAVE = 4;
    private static final float LIGHTS_OUT_AMBIENT = 0.15f;
    private static final Color LIGHTS_OUT_COLOR =
            new Color(LIGHTS_OUT_AMBIENT, LIGHTS_OUT_AMBIENT, LIGHTS_OUT_AMBIENT, 1f);
    private static final float WAVE_TRANSITION_DELAY = 3f;

    private int currentWave = 1;
    private boolean waveActive = false;
    private float waveTimer = 2f;
    private boolean deadlinesCompleted = false;
    private boolean usbSpawned = false;
    private boolean usbAcquired = false;
    private OrthographicCamera uiCam;
    private final StringBuilder deadlineTextBuilder = new StringBuilder();

    // Blink effect for wave 3
    private static final float BLINK_INTERVAL = 1.0f;
    private float blinkTimer = 0f;
    private boolean blinkPhase = true;
    private PlayScreen context;

    /**
     * Initializes the Computer Lab environment and commences escalating Deadlines.
     *
     * @param context the play screen context providing subsystems
     */
    @Override
    public void init(final PlayScreen context) {
        if (context == null) {
            throw new IllegalArgumentException("PlayScreen context cannot be null");
        }
        this.context = context;
        uiCam = new OrthographicCamera();
        uiCam.setToOrtho(false, 800, 600);
        uiCam.update();
        startLabWave(context, currentWave);
        EventDispatcher.getInstance().addListener(EventType.ITEM_PICKED_UP, this);
    }

    /**
     * Updates Deadline timers, blink intervals, and USB spawn triggers.
     *
     * @param context the active play screen context
     * @param delta the elapsed frame time in seconds
     */
    @Override
    public void update(final PlayScreen context, final float delta) {
        if (usbAcquired) {
            return;
        }

        // Blink logic for wave 3
        if (currentWave == 3 && waveActive && context.getProgressContext() != null) {
            blinkTimer += delta;
            if (blinkTimer >= BLINK_INTERVAL) {
                blinkTimer = 0f;
                blinkPhase = !blinkPhase;
                context.getProgressContext().setEnemyBlinkVisible(blinkPhase);
            }
        }

        if (waveActive) {
            if (context.getEntityManager() != null && !context.getEntityManager().hasActiveEnemies()) {
                waveActive = false;
                waveTimer = WAVE_TRANSITION_DELAY;
            }
        } else {
            waveTimer -= delta;
            if (waveTimer <= 0) {
                if (currentWave < MAX_WAVE) {
                    currentWave++;
                    startLabWave(context, currentWave);
                } else if (!usbSpawned) {
                    deadlinesCompleted = true;
                    final GameProgressContext progress = context.getProgressContext();
                    if (progress != null && progress.getMapDirector() != null) {
                        progress.getMapDirector().onDeadlinesCleared();
                    }

                    if (context.getEntityFactory() != null && progress != null
                            && progress.getItemManager() != null) {
                        context.getEntityFactory().createItemDrop(SPAWN_USB_X, SPAWN_USB_Y,
                                progress.getItemManager().getItem("usb"), Color.CYAN);
                    }
                    usbSpawned = true;
                }
            }
        }
    }

    /**
     * Renders Deadline progress indicator without per-frame allocations.
     *
     * @param context the active play screen context
     */
    @Override
    public void draw(final PlayScreen context) {
        final SpriteBatch batch = context.getBatch();
        final BitmapFont font = context.getFont();
        if (batch == null || font == null || uiCam == null) {
            return;
        }

        deadlineTextBuilder.setLength(0);
        deadlineTextBuilder.append("Deadline: ").append(currentWave).append("/").append(MAX_WAVE);

        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, deadlineTextBuilder, 350, 580);
        batch.end();
    }

    private void startLabWave(final PlayScreen context, final int wave) {
        waveActive = true;
        blinkTimer = 0f;
        blinkPhase = true;

        final GameProgressContext progress = context.getProgressContext();
        if (progress != null) {
            progress.setEnemyBlinkVisible(true);
            progress.setLightsOut(wave == LIGHTS_OUT_WAVE);
        }

        if (context.getLightingManager() != null) {
            if (wave == LIGHTS_OUT_WAVE) {
                context.getLightingManager().setAmbientLight(LIGHTS_OUT_COLOR);
            } else if (context.getConfig() != null) {
                context.getLightingManager().setAmbientLight(context.getConfig().getAmbientColor());
            }
        }

        switch (wave) {
        case 1:
            for (int i = 0; i < 5; i++) {
                spawnRandomLabEnemy(context, "null_pointer", 100, 700, 300, 500);
            }
            break;
        case 2:
            for (int i = 0; i < 3; i++) {
                spawnRandomLabEnemy(context, "null_pointer", 100, 700, 300, 500);
            }
            for (int i = 0; i < 2; i++) {
                spawnRandomLabEnemy(context, "syntax_error", 100, 700, 300, 500);
            }
            break;
        case 3:
            spawnLabEnemy(context, "infinite_loop", 400, 400);
            for (int i = 0; i < 4; i++) {
                spawnRandomLabEnemy(context, "null_pointer", 100, 700, 300, 500);
            }
            break;
        case 4:
            for (int i = 0; i < 2; i++) {
                spawnRandomLabEnemy(context, "infinite_loop", 100, 700, 300, 500);
            }
            for (int i = 0; i < 2; i++) {
                spawnRandomLabEnemy(context, "syntax_error", 100, 700, 300, 500);
            }
            break;
        case 5:
            spawnLabEnemy(context, "stack_overflow", 400, 400);
            spawnLabEnemy(context, "null_pointer", 200, 400);
            spawnLabEnemy(context, "syntax_error", 600, 400);
            break;
        default:
            break;
        }
    }

    private void spawnRandomLabEnemy(final PlayScreen context, final String type,
            final float minX, final float maxX, final float minY, final float maxY) {
        spawnLabEnemy(context, type, MathUtils.random(minX, maxX), MathUtils.random(minY, maxY));
    }

    private void spawnLabEnemy(final PlayScreen context, final String type, final float x, final float y) {
        if (context.getEntityFactory() != null) {
            context.getEntityFactory().createEnemy(type, x, y);
        }
    }

    /**
     * Intercepts item pickup events to register Source Code USB acquisition.
     *
     * @param event the triggered game event
     */
    @Override
    public void onEvent(final GameEvent<?> event) {
        if (event.getType() == EventType.ITEM_PICKED_UP) {
            final ItemPickedUpEvent data = (ItemPickedUpEvent) event.getData();
            if (data != null && data.getItem() != null && "usb".equals(data.getItem().getId())) {
                usbAcquired = true;
            }
        }
    }

    /**
     * Verifies if stage transition to Final Map is permitted.
     * Requires both MapDirector fulfillment and enemy clearance.
     *
     * @param context the play screen context
     * @return true if player can transition through the portal
     */
    @Override
    public boolean canTransition(final PlayScreen context) {
        if (!LevelBehavior.super.canTransition(context)) {
            return false;
        }
        if (context.getEntityManager() != null && context.getEntityManager().hasActiveEnemies()) {
            return false;
        }
        return true;
    }

    /**
     * Cleans up listeners, lighting overrides, and context references.
     *
     * @param context the play screen context being disposed
     */
    @Override
    public void dispose(final PlayScreen context) {
        EventDispatcher.getInstance().removeListener(EventType.ITEM_PICKED_UP, this);
        if (context != null && context.getProgressContext() != null) {
            context.getProgressContext().setLightsOut(false);
            context.getProgressContext().setEnemyBlinkVisible(true);
        }
        this.context = null;
    }

    /**
     * Checks if lights-out mode is active in the lab.
     *
     * @return true if lights are currently dimmed
     */
    public boolean isLightsOut() {
        return context != null && context.getProgressContext() != null
                && context.getProgressContext().isLightsOut();
    }
}
