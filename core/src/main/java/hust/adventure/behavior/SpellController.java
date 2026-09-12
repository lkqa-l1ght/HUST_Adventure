package hust.adventure.behavior;

import hust.adventure.core.context.GameProgressContext;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.player.input.PlayerController;

/**
 * Controller class to encapsulate the logic of Player spells (Q/E/F).
 */
public class SpellController {
    // Spells Constants
    private static final float SLOW_MOTION_DURATION = 3f;
    private static final float SLOW_MOTION_TIME_SCALE = 0.3f;
    private static final float STUN_DURATION = 2f;
    private static final float STUN_STAMINA_COST = 20f;
    private static final float RADAR_DURATION = 5f;
    private static final float RADAR_STAMINA_COST = 10f;

    private float slowMotionTimer = 0f;
    private float stunTimer = 0f;
    private float showEnemiesTimer = 0f;
    private final GameProgressContext progressContext;

    /**
     * Constructs a SpellController with ProgressContext.
     */
    public SpellController(final GameProgressContext progressContext) {
        this.progressContext = progressContext;
    }

    /**
     * Updates spell timers and checks input keys to trigger spells.
     *
     * @param player     The player entity.
     * @param controller The controller interface for player inputs.
     * @param delta      The delta time in seconds.
     */
    public void update(final Player player, final PlayerController controller, final float delta) {
        // Skill key checks
        if (progressContext.isHasNao() && controller.isSkillQJustPressed()) {
            slowMotionTimer = SLOW_MOTION_DURATION;
        }

        if (controller.isSkillEJustPressed() && player.getStamina() >= STUN_STAMINA_COST) {
            player.setStamina(player.getStamina() - STUN_STAMINA_COST);
            stunTimer = STUN_DURATION;
        }

        if (controller.isSkillFJustPressed() && player.getStamina() >= RADAR_STAMINA_COST) {
            player.setStamina(player.getStamina() - RADAR_STAMINA_COST);
            showEnemiesTimer = RADAR_DURATION;
        }

        // Timer updates
        if (slowMotionTimer > 0) {
            slowMotionTimer = Math.max(0f, slowMotionTimer - delta);
        }
        if (stunTimer > 0) {
            stunTimer = Math.max(0f, stunTimer - delta);
        }
        if (showEnemiesTimer > 0) {
            showEnemiesTimer = Math.max(0f, showEnemiesTimer - delta);
        }

        // Propagate active timer states to global ProgressContext
        float enemyTimeScale = 1.0f;
        if (stunTimer > 0) {
            enemyTimeScale = 0f;
        } else if (slowMotionTimer > 0) {
            enemyTimeScale = SLOW_MOTION_TIME_SCALE;
        }
        progressContext.setEnemyTimeScale(enemyTimeScale);
        progressContext.setShowEnemiesTimer(showEnemiesTimer);
    }

    /**
     * Gets the slow motion duration timer.
     *
     * @return Current slow motion timer in seconds.
     */
    public float getSlowMotionTimer() {
        return slowMotionTimer;
    }

    /**
     * Gets the stun duration timer.
     *
     * @return Current stun timer in seconds.
     */
    public float getStunTimer() {
        return stunTimer;
    }

    /**
     * Gets the show enemies radar duration timer.
     *
     * @return Current show enemies timer in seconds.
     */
    public float getShowEnemiesTimer() {
        return showEnemiesTimer;
    }
}
