package hust.adventure.gamestate;

import hust.adventure.core.context.GameProgressContext;

public interface GameState {
    void enter(GameProgressContext context);

    void update(GameProgressContext context, float delta);

    void exit(GameProgressContext context);
}
