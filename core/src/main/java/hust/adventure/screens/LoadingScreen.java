package hust.adventure.screens;

import hust.adventure.HustGame;

public class LoadingScreen extends BaseScreen {

    public LoadingScreen(HustGame game) {
        super(game);
        game.getAssetManager().loadAllAssets(game.getLevelDataManager(), game.getItemDataManager(), game.getEnemyDataManager());
    }

    @Override
    public void render(float delta) {
        drawProgressBar();

        if (game.getAssetManager().update()) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    private void drawProgressBar() {
        // Implement progress bar rendering using game.getAssetManager().getProgress()
        // Mock the functionality for now
    }
}
