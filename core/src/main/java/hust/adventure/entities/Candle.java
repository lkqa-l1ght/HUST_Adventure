package hust.adventure.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import hust.adventure.entities.base.MapObject;
import hust.adventure.entities.base.LightProvider;
import hust.adventure.entities.base.GameLight;

/**
 * Environmental entity: A static candle that emits a flickering light.
 */
public class Candle extends MapObject {
    private final Texture texture;
    private final GameLight light;

    public Candle(float x, float y, Texture texture, LightProvider lightProvider) {
        super(x, y, texture.getWidth(), texture.getHeight());
        this.texture = texture;
        this.stateTime = MathUtils.random(10f);

        // Request light from provider
        this.light = lightProvider.createPointLight(64, new Color(1f, 0.6f, 0.2f, 0.8f), 60f,
                x + texture.getWidth() / 2f, y + texture.getHeight() / 2f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // Flickering effect
        if (light != null) {
            float flicker = MathUtils.random(-3f, 3f) + (MathUtils.sin(stateTime * 15f) * 4f);
            light.setDistance(60f + flicker);
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
