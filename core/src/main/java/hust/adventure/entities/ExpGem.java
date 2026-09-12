package hust.adventure.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;
import hust.adventure.collision.Collider;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.base.Targetable;
import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventType;
import hust.adventure.events.ExpGainedEvent;
import hust.adventure.events.GameEvent;

public class ExpGem extends MapObject implements Pool.Poolable {
    private float amount;
    private Targetable target;
    private float speed = 300f; // Speed when attracted

    public ExpGem() {
        super(0, 0, 10, 10);
    }

    public void init(float x, float y, float amount) {
        setX(x);
        setY(y);
        this.amount = amount;
        this.target = null;
        setDestroyed(false);
    }

    public void setTarget(Targetable target) {
        this.target = target;
    }

    @Override
    public void reset() {
        setDestroyed(true);
        amount = 0;
        target = null;
        if (getCollider() != null) {
            getCollider().setListener(null);
        }
    }

    @Override
    public void setCollider(Collider collider) {
        super.setCollider(collider);
        if (collider != null) {
            collider.setListener(other -> {
                // Check if colliding with player (Layer could be PLAYER)
                ExpGainedEvent payload = new ExpGainedEvent(amount);
                GameEvent<ExpGainedEvent> event = new GameEvent<>(EventType.EXP_GAINED, payload);

                EventDispatcher.getInstance().dispatch(event);
                destroy();
            });
        }
    }

    @Override
    public void update(float delta) {
        if (target != null && !target.isDestroyed()) {
            float dx = target.getX() - getX();
            float dy = target.getY() - getY();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist > 0) {
                float vx = (dx / dist) * speed * delta;
                float vy = (dy / dist) * speed * delta;
                setX(getX() + vx);
                setY(getY() + vy);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Rendered by GameRenderer in presentation layer
    }

    @Override
    public Color getShapeFallbackColor() {
        return Color.GREEN;
    }
}
