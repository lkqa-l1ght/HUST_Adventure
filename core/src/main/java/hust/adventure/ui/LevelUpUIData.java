package hust.adventure.ui;

import java.util.List;

/**
 * Data Transfer Object containing a list of choices to render in the Level Up UI.
 */
public class LevelUpUIData {
    private final List<LevelUpChoiceData> choices;

    public LevelUpUIData(final List<LevelUpChoiceData> choices) {
        this.choices = choices;
    }

    public List<LevelUpChoiceData> getChoices() {
        return choices;
    }
}
