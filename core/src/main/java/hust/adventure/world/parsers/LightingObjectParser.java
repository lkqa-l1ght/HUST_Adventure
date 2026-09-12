package hust.adventure.world.parsers;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import hust.adventure.world.LightingObjectData;
import hust.adventure.world.MapObjectParser;
import hust.adventure.world.MapParseResult;

/**
 * Parser for loading lighting objects (e.g. Candles, Books) from TMX layers.
 */
public class LightingObjectParser implements MapObjectParser {
    @Override
    public void parse(final MapLayer layer, final MapParseResult result) {
        if (layer == null || result == null) {
            return;
        }
        for (final MapObject obj : layer.getObjects()) {
            final Float x = obj.getProperties().get("x", 0f, Float.class);
            final Float y = obj.getProperties().get("y", 0f, Float.class);
            final String name = obj.getName();
            if (name != null && !name.isEmpty()) {
                result.addLightingObject(new LightingObjectData(name, x != null ? x : 0f, y != null ? y : 0f));
            }
        }
    }
}
