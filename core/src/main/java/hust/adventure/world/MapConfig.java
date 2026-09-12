package hust.adventure.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing the map layer configuration.
 */
public class MapConfig {
    private List<String> backgroundLayerNames = new ArrayList<>();
    private List<String> collisionFallbackLayerNames = new ArrayList<>();
    private String portalLayerName = "Portals";
    private String spawnLayerName = "Spawn";
    private String lightingLayerName = "LightingObjects";
    private List<String> decorLayerNames = new ArrayList<>();
    private List<String> groundLayerNames = new ArrayList<>();
    private String collisionLayerKey = "collision";

    public List<String> getBackgroundLayerNames() {
        return backgroundLayerNames;
    }

    public void setBackgroundLayerNames(final List<String> backgroundLayerNames) {
        this.backgroundLayerNames = backgroundLayerNames;
    }

    public List<String> getCollisionFallbackLayerNames() {
        return collisionFallbackLayerNames;
    }

    public void setCollisionFallbackLayerNames(final List<String> collisionFallbackLayerNames) {
        this.collisionFallbackLayerNames = collisionFallbackLayerNames;
    }

    public String getPortalLayerName() {
        return portalLayerName;
    }

    public void setPortalLayerName(final String portalLayerName) {
        this.portalLayerName = portalLayerName;
    }

    public String getSpawnLayerName() {
        return spawnLayerName;
    }

    public void setSpawnLayerName(final String spawnLayerName) {
        this.spawnLayerName = spawnLayerName;
    }

    public String getLightingLayerName() {
        return lightingLayerName;
    }

    public void setLightingLayerName(final String lightingLayerName) {
        this.lightingLayerName = lightingLayerName;
    }

    public List<String> getDecorLayerNames() {
        return decorLayerNames;
    }

    public void setDecorLayerNames(final List<String> decorLayerNames) {
        this.decorLayerNames = decorLayerNames;
    }

    public List<String> getGroundLayerNames() {
        return groundLayerNames;
    }

    public void setGroundLayerNames(final List<String> groundLayerNames) {
        this.groundLayerNames = groundLayerNames;
    }

    public String getCollisionLayerKey() {
        return collisionLayerKey;
    }

    public void setCollisionLayerKey(final String collisionLayerKey) {
        this.collisionLayerKey = collisionLayerKey;
    }
}
