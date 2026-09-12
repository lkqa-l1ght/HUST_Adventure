package hust.adventure.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class DamageText implements Pool.Poolable {
    private Vector2 position = new Vector2();
    private Vector2 velocity = new Vector2();
    private String text;
    private Color color = new Color();
    private float alpha;
    private float duration;
    private float timeToLive;

    public DamageText() {
    }

    public void init(float x, float y, float vx, float vy, String text, Color color, float duration) {
        this.position.set(x, y);
        this.velocity.set(vx, vy);
        this.text = text;
        this.color.set(color);
        this.alpha = 1.0f;
        this.duration = duration;
        this.timeToLive = duration;
    }

    public void update(float delta) {
        position.mulAdd(velocity, delta);
        timeToLive -= delta;
        alpha = Math.max(0, timeToLive / duration);
        color.a = alpha;
    }

    public boolean isExpired() {
        return timeToLive <= 0;
    }

    public Vector2 getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void reset() {
        position.setZero();
        velocity.setZero();
        text = null;
        color.set(Color.WHITE);
        alpha = 1.0f;
        timeToLive = 0f;
        duration = 0f;
    }
}
