package hust.adventure.ui.puzzle;

import com.badlogic.gdx.math.Rectangle;

/**
 * Representation of a single memory card in the matching puzzle grid.
 */
public class MemoryCard {
    private int pairId;
    private final Rectangle bounds;
    private boolean flipped;
    private boolean matched;

    public MemoryCard(final int pairId, final float x, final float y, final float width, final float height) {
        this.pairId = pairId;
        this.bounds = new Rectangle(x, y, width, height);
        this.flipped = false;
        this.matched = false;
    }

    public int getPairId() {
        return pairId;
    }

    public void setPairId(final int pairId) {
        this.pairId = pairId;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(final boolean flipped) {
        this.flipped = flipped;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(final boolean matched) {
        this.matched = matched;
    }
}
