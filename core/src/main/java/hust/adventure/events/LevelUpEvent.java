package hust.adventure.events;
 
public class LevelUpEvent {
    private final int newLevel;
 
    public LevelUpEvent(int newLevel) {
        this.newLevel = newLevel;
    }
 
    public int getNewLevel() {
        return newLevel;
    }
}
