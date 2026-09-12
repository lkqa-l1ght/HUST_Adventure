package hust.adventure.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Utility class for drawing basic shapes using SpriteBatch and a shared white pixel texture.
 * Resolves SRP violations by centralizing static textures and custom rendering logic.
 */
public final class ShapeDrawUtils {
    private static Texture whitePixel;

    private ShapeDrawUtils() {
        // Private constructor to prevent instantiation
    }

    private static Texture getWhitePixel() {
        if (whitePixel == null) {
            final Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    /**
     * Disposes the shared white pixel texture if it has been allocated.
     */
    public static void disposeStatic() {
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }

    /**
     * Draws a dashed circle outline at a specified location.
     */
    public static void drawDashedCircle(final SpriteBatch batch, final float cx, final float cy, final float r, final float angleOffset, final Color color) {
        final int numDashes = 12;
        final float dashDegrees = 15f;
        for (int i = 0; i < numDashes; i++) {
            final float midAngle = i * (360f / numDashes) + angleOffset;
            drawArcSegment(batch, cx, cy, r, midAngle - dashDegrees / 2f, midAngle + dashDegrees / 2f, 2f, color);
        }
    }

    /**
     * Draws an arc segment.
     */
    public static void drawArcSegment(final SpriteBatch batch, final float cx, final float cy, final float r, final float startAngle, final float endAngle, final float thickness, final Color color) {
        final int steps = 3;
        final float stepSize = (endAngle - startAngle) / steps;
        for (int i = 0; i < steps; i++) {
            final float a1 = startAngle + i * stepSize;
            final float a2 = a1 + stepSize;

            final float rad1 = (float) Math.toRadians(a1);
            final float rad2 = (float) Math.toRadians(a2);

            final float x1 = cx + r * (float) Math.cos(rad1);
            final float y1 = cy + r * (float) Math.sin(rad1);
            final float x2 = cx + r * (float) Math.cos(rad2);
            final float y2 = cy + r * (float) Math.sin(rad2);

            drawLine(batch, x1, y1, x2, y2, thickness, color);
        }
    }

    /**
     * Draws a single line segment between two points with a given thickness.
     */
    public static void drawLine(final SpriteBatch batch, final float x1, final float y1, final float x2, final float y2, final float thickness, final Color color) {
        final float dx = x2 - x1;
        final float dy = y2 - y1;
        final float length = (float) Math.sqrt(dx * dx + dy * dy);
        final float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        batch.setColor(color);
        batch.draw(getWhitePixel(),
            x1, y1 - thickness / 2f, // x, y
            0f, thickness / 2f,      // originX, originY (pivot at start of line)
            length, thickness,       // width, height
            1f, 1f,                  // scaleX, scaleY
            angle,                   // rotation
            0, 0, 1, 1,              // srcX, srcY, srcWidth, srcHeight
            false, false             // flipX, flipY
        );
        batch.setColor(Color.WHITE);
    }

    /**
     * Draws a solid rectangle of a specified color.
     */
    public static void drawRect(final SpriteBatch batch, final float x, final float y, final float width, final float height, final Color color) {
        batch.setColor(color);
        batch.draw(getWhitePixel(), x, y, width, height);
        batch.setColor(Color.WHITE);
    }
}
