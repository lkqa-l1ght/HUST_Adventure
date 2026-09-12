package hust.adventure.ui;

/**
 * Data Transfer Object for a single choice in the Level Up UI.
 */
public class LevelUpChoiceData {
    private final String name;
    private final String description;

    public LevelUpChoiceData(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
