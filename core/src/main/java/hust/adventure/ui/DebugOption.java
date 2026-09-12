package hust.adventure.ui;

/**
 * Represents a debug option in the selection panel (e.g., a map, an item, or an enemy to spawn).
 */
public class DebugOption {
    private final String id;
    private final String displayName;

    /**
     * Constructs a new DebugOption.
     *
     * @param id          the identifier of the option
     * @param displayName the name to display on the UI
     */
    public DebugOption(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
