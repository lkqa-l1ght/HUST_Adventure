package hust.adventure.world.parsers;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import hust.adventure.entities.WallEntity;
import hust.adventure.world.MapObjectParser;
import hust.adventure.world.MapParseResult;

/**
 * Parser for loading wall collision objects from TMX layers.
 */
public class WallParser implements MapObjectParser {
    @Override
    public void parse(final MapLayer layer, final MapParseResult result) {
        if (layer == null || result == null) {
            return;
        }
        for (final MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                final Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                result.addWall(new WallEntity(rect));
            }
        }
    }
}
