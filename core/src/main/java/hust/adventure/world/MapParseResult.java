package hust.adventure.world;

import hust.adventure.entities.WallEntity;
import hust.adventure.entities.base.MapObject;
import hust.adventure.world.WorldManager.Portal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container holding the parsed map objects (walls, portals, lighting objects, decor entities).
 */
public class MapParseResult {
    private final List<WallEntity> walls = new ArrayList<>();
    private final List<Portal> portals = new ArrayList<>();
    private final List<LightingObjectData> lightingObjects = new ArrayList<>();
    private final List<MapObject> decorEntities = new ArrayList<>();

    public List<WallEntity> getWalls() {
        return Collections.unmodifiableList(walls);
    }

    public List<Portal> getPortals() {
        return Collections.unmodifiableList(portals);
    }

    public List<LightingObjectData> getLightingObjects() {
        return Collections.unmodifiableList(lightingObjects);
    }

    public List<MapObject> getDecorEntities() {
        return Collections.unmodifiableList(decorEntities);
    }

    /**
     * Adds a wall entity to the parsed list.
     *
     * @param wall the WallEntity to add
     */
    public void addWall(final WallEntity wall) {
        if (wall != null) {
            walls.add(wall);
        }
    }

    /**
     * Adds a portal to the parsed list.
     *
     * @param portal the Portal to add
     */
    public void addPortal(final Portal portal) {
        if (portal != null) {
            portals.add(portal);
        }
    }

    /**
     * Adds a lighting object data to the parsed list.
     *
     * @param lightingObject the LightingObjectData to add
     */
    public void addLightingObject(final LightingObjectData lightingObject) {
        if (lightingObject != null) {
            lightingObjects.add(lightingObject);
        }
    }

    /**
     * Adds a decorative entity to the parsed list.
     *
     * @param decor the MapObject to add
     */
    public void addDecorEntity(final MapObject decor) {
        if (decor != null) {
            decorEntities.add(decor);
        }
    }

    /**
     * Clears all parsed objects.
     */
    public void clear() {
        walls.clear();
        portals.clear();
        lightingObjects.clear();
        decorEntities.clear();
    }
}
