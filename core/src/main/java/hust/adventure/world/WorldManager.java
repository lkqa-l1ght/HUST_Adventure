package hust.adventure.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;

import hust.adventure.entities.WallEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game world state, including the map, collisions, and portals.
 */
public class WorldManager implements Disposable {
    private final MapConfig config;
    private final MapParseResult parseResult;
    private final ObjectMap<String, MapObjectParser> parserRegistry;
    private TiledMap currentMap;
    private InfiniteMapRenderer mapRenderer;

    /**
     * Constructs a WorldManager with the given configuration.
     *
     * @param config the MapConfig containing layer specifications
     */
    public WorldManager(final MapConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("MapConfig cannot be null");
        }
        this.config = config;
        this.parseResult = new MapParseResult();
        this.parserRegistry = new ObjectMap<>();
    }

    /**
     * Registers a parser for a specific layer name.
     *
     * @param layerName the layer name to associate with the parser
     * @param parser    the MapObjectParser to handle the layer
     */
    public void registerParser(final String layerName, final MapObjectParser parser) {
        if (layerName != null && parser != null) {
            parserRegistry.put(layerName, parser);
        }
    }

    /**
     * Loads the map data including portals and physical walls.
     *
     * @param map the TiledMap instance to load
     */
    public void loadMap(final TiledMap map) {
        if (map == null) {
            throw new IllegalArgumentException("Map cannot be null");
        }
        this.currentMap = map;
        parseResult.clear();

        // 1. Find and parse collision layers
        final List<MapLayer> collisionLayers = findCollisionLayers();
        final MapObjectParser wallParser = parserRegistry.get(config.getCollisionLayerKey());
        if (wallParser != null) {
            for (final MapLayer layer : collisionLayers) {
                wallParser.parse(layer, parseResult);
            }
        }

        // 2. Parse other layers using the registry, avoiding double parsing collision layers
        for (int i = 0; i < map.getLayers().size(); i++) {
            final MapLayer layer = map.getLayers().get(i);
            if (collisionLayers.contains(layer)) {
                continue;
            }
            final String name = layer.getName();
            if (name != null) {
                final MapObjectParser parser = parserRegistry.get(name);
                if (parser != null) {
                    final int beforeCount = parseResult.getDecorEntities().size();
                    parser.parse(layer, parseResult);
                    final int afterCount = parseResult.getDecorEntities().size();
                    for (int j = beforeCount; j < afterCount; j++) {
                        final hust.adventure.entities.base.MapObject decor = parseResult.getDecorEntities().get(j);
                        decor.setZIndex(i); // Set XML layer index as zIndex
                    }
                }
            }
        }

        // 3. Align sortingY for overlapping decor entities across all layers (using transitive connected components)
        final List<hust.adventure.entities.base.MapObject> decorEntities = new ArrayList<>(parseResult.getDecorEntities());
        final List<List<hust.adventure.entities.base.MapObject>> groups = new ArrayList<>();

        for (final hust.adventure.entities.base.MapObject obj : decorEntities) {
            final List<List<hust.adventure.entities.base.MapObject>> overlappingGroups = new ArrayList<>();
            for (final List<hust.adventure.entities.base.MapObject> group : groups) {
                for (final hust.adventure.entities.base.MapObject other : group) {
                    if (obj.getBounds().overlaps(other.getBounds())) {
                        overlappingGroups.add(group);
                        break;
                    }
                }
            }

            if (overlappingGroups.isEmpty()) {
                final List<hust.adventure.entities.base.MapObject> newGroup = new ArrayList<>();
                newGroup.add(obj);
                groups.add(newGroup);
            } else if (overlappingGroups.size() == 1) {
                overlappingGroups.get(0).add(obj);
            } else {
                final List<hust.adventure.entities.base.MapObject> mergedGroup = overlappingGroups.get(0);
                mergedGroup.add(obj);
                for (int idx = 1; idx < overlappingGroups.size(); idx++) {
                    final List<hust.adventure.entities.base.MapObject> toMerge = overlappingGroups.get(idx);
                    mergedGroup.addAll(toMerge);
                    groups.remove(toMerge);
                }
            }
        }

        for (final List<hust.adventure.entities.base.MapObject> group : groups) {
            if (group.size() <= 1) {
                continue;
            }
            float minBottomY = Float.MAX_VALUE;
            for (final hust.adventure.entities.base.MapObject obj : group) {
                final float bottomY = obj.getY() - obj.getHeight() / 2f;
                if (bottomY < minBottomY) {
                    minBottomY = bottomY;
                }
            }
            for (final hust.adventure.entities.base.MapObject obj : group) {
                obj.setSortingY(minBottomY + obj.getHeight() / 2f);
            }
        }
    }

    private List<MapLayer> findCollisionLayers() {
        final List<MapLayer> foundLayers = new ArrayList<>();
        if (currentMap == null) {
            return foundLayers;
        }

        // 1. Check map properties for a specific collision layer name
        final String customCollisionLayerName = currentMap.getProperties().get("collisionLayer", String.class);
        if (customCollisionLayerName != null) {
            final MapLayer layer = currentMap.getLayers().get(customCollisionLayerName);
            if (layer != null) {
                foundLayers.add(layer);
                return foundLayers;
            }
        }

        // 2. Scan all layers for 'collision' or 'isCollision' boolean/string property
        for (final MapLayer layer : currentMap.getLayers()) {
            Object collProp = layer.getProperties().get("collision");
            if (collProp == null) {
                collProp = layer.getProperties().get("isCollision");
            }
            if (collProp instanceof Boolean && (Boolean) collProp) {
                foundLayers.add(layer);
            } else if (collProp instanceof String
                    && ("true".equalsIgnoreCase((String) collProp) || "1".equals(collProp))) {
                foundLayers.add(layer);
            }
        }
        if (!foundLayers.isEmpty()) {
            return foundLayers;
        }

        // 3. Fallback to default known collision layer names
        final List<String> fallbackNames = config.getCollisionFallbackLayerNames();
        if (fallbackNames != null) {
            for (final String name : fallbackNames) {
                final MapLayer layer = currentMap.getLayers().get(name);
                if (layer != null) {
                    foundLayers.add(layer);
                    break; // Prioritize the first matching fallback layer
                }
            }
        }

        return foundLayers;
    }

    public void initInfiniteWorld(final InfiniteMapRenderer mapRenderer) {
        if (mapRenderer == null) {
            throw new IllegalArgumentException("InfiniteMapRenderer cannot be null");
        }
        this.mapRenderer = mapRenderer;
    }

    public void renderBackground(final SpriteBatch batch) {
        if (mapRenderer != null) {
            mapRenderer.draw(batch);
        }
    }

    public TiledMap getCurrentMap() {
        return currentMap;
    }

    public List<WallEntity> getWalls() {
        return parseResult.getWalls();
    }

    public List<Portal> getPortals() {
        return parseResult.getPortals();
    }

    public MapParseResult getParseResult() {
        return parseResult;
    }

    public List<String> getBackgroundLayerNames() {
        return config.getBackgroundLayerNames();
    }

    public List<String> getGroundLayerNames() {
        return config.getGroundLayerNames();
    }

    public List<String> getDecorLayerNames() {
        return config.getDecorLayerNames();
    }

    /**
     * Reads the spawn point of the player from the spawn layer in TMX.
     *
     * <p>Tiled Point map objects are preferred for exact positioning. Rectangle map objects are also supported
     * and will be resolved using their center coordinates (center-X and center-Y) to align with player hitbox origins.</p>
     *
     * @return the spawn point coordinates as a Vector2, or null if not found
     */
    public Vector2 getSpawnPoint() {
        if (currentMap == null) {
            return null;
        }

        String spawnLayerName = config.getSpawnLayerName();
        if (currentMap.getProperties().containsKey("spawnLayer")) {
            spawnLayerName = currentMap.getProperties().get("spawnLayer", String.class);
        }

        MapLayer spawnLayer = currentMap.getLayers().get(spawnLayerName);
        if (spawnLayer == null) {
            spawnLayer = currentMap.getLayers().get("Spawn");
        }
        if (spawnLayer == null) {
            spawnLayer = currentMap.getLayers().get("spawn");
        }

        if (spawnLayer == null) {
            // Also try scanning for any layer with a property "isSpawn" or similar
            for (final MapLayer layer : currentMap.getLayers()) {
                if ("true".equalsIgnoreCase(layer.getProperties().get("isSpawn", String.class))
                        || Boolean.TRUE.equals(layer.getProperties().get("isSpawn", Boolean.class))) {
                    spawnLayer = layer;
                    break;
                }
            }
        }

        if (spawnLayer == null) {
            return null;
        }

        for (final MapObject obj : spawnLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                final Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                return new Vector2(rect.x + rect.width / 2f, rect.y + rect.height / 2f);
            } else {
                Float x = obj.getProperties().get("x", Float.class);
                Float y = obj.getProperties().get("y", Float.class);
                Float w = obj.getProperties().get("width", Float.class);
                Float h = obj.getProperties().get("height", Float.class);
                if (x != null && y != null) {
                    float width = w != null ? w : 0;
                    float height = h != null ? h : 0;
                    return new Vector2(x + width / 2f, y + height / 2f);
                }
            }
        }
        return null;
    }

    /**
     * Analyzes map layers and classifies them into background/foreground.
     *
     * @return a 2D array where index [0] contains background layers and index [1] contains foreground layers.
     */
    public int[][] classifyLayers() {
        final IntArray bg = new IntArray();
        final IntArray fg = new IntArray();
        if (currentMap != null) {
            for (int i = 0; i < currentMap.getLayers().size(); i++) {
                final MapLayer layer = currentMap.getLayers().get(i);
                if (isBackgroundLayer(layer)) {
                    bg.add(i);
                } else {
                    fg.add(i);
                }
            }
        }
        return new int[][] { bg.toArray(), fg.toArray() };
    }

    private boolean isBackgroundLayer(final MapLayer layer) {
        if (layer == null) {
            return false;
        }
        final Object isBgProp = layer.getProperties().get("isBackground");
        if (isBgProp instanceof Boolean) {
            return (Boolean) isBgProp;
        }
        if (isBgProp instanceof String) {
            return "true".equalsIgnoreCase((String) isBgProp) || "1".equals(isBgProp);
        }
        final String name = layer.getName();
        if (name != null && config.getBackgroundLayerNames() != null) {
            final String lowerName = name.toLowerCase(java.util.Locale.ROOT);
            for (final String bgName : config.getBackgroundLayerNames()) {
                if (lowerName.equals(bgName.toLowerCase(java.util.Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        // NOTE: currentMap is owned by GameAssetManager and must NOT be disposed here.
        // Disposing it would invalidate the AssetManager's cache and crash on next map load.
        currentMap = null;
        parseResult.clear();
    }

    public static class Portal {
        private final Rectangle bounds;
        private final String targetMap;
        private final float spawnX;
        private final float spawnY;

        public Portal(final Rectangle bounds, final String targetMap, final float spawnX, final float spawnY) {
            this.bounds = bounds;
            this.targetMap = targetMap;
            this.spawnX = spawnX;
            this.spawnY = spawnY;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public String getTargetMap() {
            return targetMap;
        }

        public float getSpawnX() {
            return spawnX;
        }

        public float getSpawnY() {
            return spawnY;
        }
    }
}
