package hust.adventure.core.context;

import hust.adventure.events.EventDispatcher;
import hust.adventure.events.EventListener;
import hust.adventure.events.EventType;
import hust.adventure.events.GameEvent;
import hust.adventure.events.ExpGainedEvent;
import hust.adventure.events.LevelUpEvent;
import com.badlogic.gdx.utils.Disposable;

public class LevelManager implements EventListener, Disposable {
    private final GameProgressContext progressContext;
    private int currentLevel = 1;
    private float currentExp = 0;
    private float expToNextLevel = 3f;

    public LevelManager(final GameProgressContext progressContext) {
        if (progressContext == null) {
            throw new IllegalArgumentException("progressContext cannot be null");
        }
        this.progressContext = progressContext;
        EventDispatcher.getInstance().addListener(EventType.EXP_GAINED, this);
        this.currentLevel = progressContext.getLevel();
        this.currentExp = progressContext.getExp();
        this.expToNextLevel = progressContext.getExpToNextLevel();
    }

    @Override
    public void onEvent(GameEvent<?> event) {
        if (event.getType() == EventType.EXP_GAINED) {
            ExpGainedEvent data = (ExpGainedEvent) event.getData();
            addExp(data.getAmount());
        }
    }

    public void addExp(float amount) {
        currentExp += amount;
        while (currentExp >= expToNextLevel) {
            currentExp -= expToNextLevel;
            currentLevel++;
            expToNextLevel *= 1.5f; // simple scaling

            LevelUpEvent payload = new LevelUpEvent(currentLevel);
            GameEvent<LevelUpEvent> event = new GameEvent<>(EventType.LEVEL_UP, payload);

            EventDispatcher.getInstance().dispatch(event);
        }
        syncProgressContext();
    }

    private void syncProgressContext() {
        progressContext.setLevel(currentLevel);
        progressContext.setExp(currentExp);
        progressContext.setExpToNextLevel(expToNextLevel);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public float getCurrentExp() {
        return currentExp;
    }

    public float getExpToNextLevel() {
        return expToNextLevel;
    }

    @Override
    public void dispose() {
        EventDispatcher.getInstance().removeListener(EventType.EXP_GAINED, this);
    }
}
