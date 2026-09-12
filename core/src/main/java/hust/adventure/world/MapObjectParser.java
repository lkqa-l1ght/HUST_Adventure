package hust.adventure.world;

import com.badlogic.gdx.maps.MapLayer;

/**
 * Interface for parsing map layers to extract game objects.
 */
@FunctionalInterface
public interface MapObjectParser {
    /**
     * Parses the given MapLayer and populates the MapParseResult.
     *
     * @param layer  the MapLayer to parse
     * @param result the MapParseResult to populate
     */
    void parse(final MapLayer layer, final MapParseResult result);
}
