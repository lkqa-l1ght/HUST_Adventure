package hust.adventure.world.parsers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import hust.adventure.world.MapObjectParser;
import hust.adventure.world.MapParseResult;
import hust.adventure.world.WorldManager.Portal;

/**
 * Parser for loading portal objects from TMX layers.
 */
public class PortalParser implements MapObjectParser {
    @Override
    public void parse(final MapLayer layer, final MapParseResult result) {
        if (layer == null || result == null) {
            return;
        }
        for (final MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                final Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                final String target = getStringProperty(obj, "target", null);
                final float spawnX = getFloatProperty(obj, "spawnX", 0f);
                final float spawnY = getFloatProperty(obj, "spawnY", 0f);
                result.addPortal(new Portal(rect, target, spawnX, spawnY));
            }
        }
    }

    private float getFloatProperty(final MapObject obj, final String name, final float defaultValue) {
        final Object val = obj.getProperties().get(name);
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        if (val instanceof String) {
            try {
                return Float.parseFloat((String) val);
            } catch (final NumberFormatException e) {
                Gdx.app.error("PortalParser", "Failed to parse float property '" + name + "' value: " + val, e);
            }
        }
        return defaultValue;
    }

    private String getStringProperty(final MapObject obj, final String name, final String defaultValue) {
        final Object val = obj.getProperties().get(name);
        if (val == null) {
            return defaultValue;
        }
        return val.toString();
    }
}
