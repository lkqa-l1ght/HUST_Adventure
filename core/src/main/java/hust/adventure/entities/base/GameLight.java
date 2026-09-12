package hust.adventure.entities.base;

/**
 * Interface representing a light source in the domain layer, decoupling entities from presentation details.
 */
public interface GameLight {
    /**
     * Set the distance range of the light source.
     * 
     * @param distance new distance
     */
    void setDistance(float distance);

    /**
     * Update the coordinates of the light source in the world.
     * 
     * @param x coordinate X
     * @param y coordinate Y
     */
    void setPosition(float x, float y);

    /**
     * Remove the light source from the system.
     */
    void remove();
}
