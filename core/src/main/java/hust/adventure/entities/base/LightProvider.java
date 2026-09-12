package hust.adventure.entities.base;

import com.badlogic.gdx.graphics.Color;

/**
 * Interface to provide lighting services to entities without tight coupling to LightingManager.
 */
public interface LightProvider {
    /**
     * Creates a point light and returns it.
     * 
     * @param rays number of rays for the light
     * @param color color of the light
     * @param distance distance of the light
     * @param x x position
     * @param y y position
     * @return the created GameLight
     */
    GameLight createPointLight(int rays, Color color, float distance, float x, float y);

    /**
     * Sets the ambient light for the current environment.
     * 
     * @param color ambient light color
     */
    void setAmbientLight(Color color);
}
