package hust.adventure.behavior.ai;

import com.badlogic.gdx.math.Vector2;
import hust.adventure.entities.EntityManager;
import hust.adventure.entities.enemies.Enemy;
import hust.adventure.entities.player.Player;

/**
 * AI movement behavior where the enemy pauses, shows a warning line directed towards the player, and then dashes at
 * high speed in that straight line.
 */
public class TelegraphedChargeBehavior implements TelegraphedBehavior {
    private enum State {
        CHASE, TELEGRAPH, CHARGE
    }

    private State state = State.CHASE;
    private float stateTimer = 0.5f; // Wait a bit before first telegraph
    private final Vector2 chargeDirection = new Vector2();

    private final float telegraphDuration = 1.0f; // Warning duration in seconds
    private final float chargeDuration = 0.6f; // Dash duration in seconds
    private float normalSpeed;
    private float chargeSpeed;
    private boolean initialized = false;

    private boolean telegraphedCharging = false;
    private float telegraphTargetX;
    private float telegraphTargetY;

    public TelegraphedChargeBehavior() {
    }

    @Override
    public void execute(final Enemy enemy, final float delta, final Player player, final EntityManager entityManager) {
        if (player == null || enemy == null) {
            return;
        }

        if (!initialized) {
            this.normalSpeed = enemy.getSpeed();
            this.chargeSpeed = this.normalSpeed * 5.0f; // 5 times faster when charging
            initialized = true;
        }

        stateTimer -= delta;

        switch (state) {
        case CHASE:
            // Move towards player slowly/normally
            final float dx = player.getX() - enemy.getX();
            final float dy = player.getY() - enemy.getY();
            final float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                enemy.setX(enemy.getX() + (dx / dist) * enemy.getSpeed() * delta);
                enemy.setY(enemy.getY() + (dy / dist) * enemy.getSpeed() * delta);
            }

            final float dashDistance = chargeSpeed * chargeDuration - 20f;
            // If close enough and cooldown is finished, start telegraphing
            if (dist < dashDistance && stateTimer <= 0f) {
                state = State.TELEGRAPH;
                stateTimer = telegraphDuration;
                enemy.setSpeed(0f); // Freeze enemy in place

                // Lock the charge direction
                chargeDirection.set(dx / dist, dy / dist);
                telegraphedCharging = true;

                // The line is drawn 500 units long in that direction
                telegraphTargetX = enemy.getX() + chargeDirection.x * 500f;
                telegraphTargetY = enemy.getY() + chargeDirection.y * 500f;
            }
            break;

        case TELEGRAPH:
            if (stateTimer <= 0f) {
                state = State.CHARGE;
                stateTimer = chargeDuration;
                enemy.setSpeed(chargeSpeed);
                telegraphedCharging = false;
            }
            break;

        case CHARGE:
            // Move in the locked straight line at high speed
            enemy.setX(enemy.getX() + chargeDirection.x * enemy.getSpeed() * delta);
            enemy.setY(enemy.getY() + chargeDirection.y * enemy.getSpeed() * delta);

            if (stateTimer <= 0f) {
                state = State.CHASE;
                stateTimer = 1.5f; // Cooldown of 1.5 seconds before charging again
                enemy.setSpeed(normalSpeed);
                telegraphedCharging = false;
            }
            break;
        }
    }

    @Override
    public boolean isTelegraphed() {
        return telegraphedCharging;
    }

    @Override
    public float getTelegraphTimer() {
        return state == State.TELEGRAPH ? stateTimer : 0f;
    }

    @Override
    public float getTelegraphTargetX() {
        return telegraphTargetX;
    }

    @Override
    public float getTelegraphTargetY() {
        return telegraphTargetY;
    }
}
