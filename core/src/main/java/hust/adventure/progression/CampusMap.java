package hust.adventure.progression;

/**
 * Canonical campus stages sequenced according to ADR-0001.
 */
public enum CampusMap {
    /**
     * Map 1: Campus outdoor grounds where the student's journey begins.
     */
    MAP_1_OUTSIDE("FINAL_OUTSIDE", "Admission Note", "note"),

    /**
     * Map 2: First floor lecture hall building.
     */
    MAP_2_FLOOR_1("TANG_1", "Lecture Notes", "lecture_notes"),

    /**
     * Map 3: University library archives containing academic challenges and guardian boss.
     */
    MAP_3_LIBRARY("LIBRARY", "Awakened Brain", "brain"),

    /**
     * Map 4: Computer laboratory housing advanced bug deadlines and source code compilation.
     */
    MAP_4_LAB("LAB", "Source Code USB", "usb"),

    /**
     * Final Map: Thesis defense hall where Professor T.H.T conducts the ultimate exam.
     */
    FINAL_MAP_BOSS("BOSS_ROOM", null, null);

    private final String levelConfigId;
    private final String requiredKeyItemName;
    private final String requiredKeyItemId;

    CampusMap(final String levelConfigId, final String requiredKeyItemName, final String requiredKeyItemId) {
        this.levelConfigId = levelConfigId;
        this.requiredKeyItemName = requiredKeyItemName;
        this.requiredKeyItemId = requiredKeyItemId;
    }

    /**
     * Gets the identifier matching the configuration in levels.json.
     *
     * @return the string level configuration ID.
     */
    public String getLevelConfigId() {
        return levelConfigId;
    }

    /**
     * Gets the descriptive academic name of the Key Item required to exit this stage.
     *
     * @return the Key Item name, or null for the final stage.
     */
    public String getRequiredKeyItemName() {
        return requiredKeyItemName;
    }

    /**
     * Gets the inventory item ID of the Key Item required to unlock the exit portal.
     *
     * @return the item ID matching items.json, or null if no key item is required.
     */
    public String getRequiredKeyItemId() {
        return requiredKeyItemId;
    }

    /**
     * Resolves the next sequential stage in accordance with ADR-0001.
     *
     * @return the subsequent CampusMap stage, or null if this is the final stage.
     */
    public CampusMap getNextStage() {
        return switch (this) {
            case MAP_1_OUTSIDE -> MAP_2_FLOOR_1;
            case MAP_2_FLOOR_1 -> MAP_3_LIBRARY;
            case MAP_3_LIBRARY -> MAP_4_LAB;
            case MAP_4_LAB -> FINAL_MAP_BOSS;
            case FINAL_MAP_BOSS -> null;
        };
    }
}
