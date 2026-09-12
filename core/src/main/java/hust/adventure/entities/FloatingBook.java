package hust.adventure.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.base.LightProvider;
import hust.adventure.entities.base.GameLight;

/**
 * Environmental entity: A book that floats with a sine wave motion and emits light.
 */
public class FloatingBook extends MapObject {
    private final Texture texture;
    private final GameLight light;
    private final float baseY;

    public FloatingBook(float x, float y, Texture texture, LightProvider lightProvider) {
        super(x, y, texture.getWidth(), texture.getHeight());
        this.texture = texture;
        this.baseY = y;
        this.stateTime = MathUtils.random(10f);

        // Request light from provider
        this.light = lightProvider.createPointLight(32, new Color(1f, 0.8f, 0.4f, 0.6f), 50f, x, y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // Floating movement
        y = baseY + MathUtils.sin(stateTime * 2f) * 10f;
        updateBounds();

        // Update light position and flickering distance
        if (light != null) {
            light.setPosition(x + width / 2f, y + height / 2f);
            light.setDistance(50f + MathUtils.sin(stateTime * 4f) * 5f);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    @Override
    public void dispose() {
        if (light != null) {
            light.remove();
        }
        super.dispose();
    }
}
