package hust.adventure.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import hust.adventure.HustGame;

public abstract class BaseScreen implements Screen {
    protected HustGame game;
    protected SpriteBatch batch;
    protected ShapeRenderer shapeRenderer;
    protected BitmapFont font;

    public BaseScreen(HustGame game) {
        this.game = game;
        this.batch = game.getSpriteBatch();
        this.shapeRenderer = game.getShapeRenderer();
        this.font = game.getFont();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
