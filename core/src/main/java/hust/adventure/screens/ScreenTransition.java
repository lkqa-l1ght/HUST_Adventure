package hust.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import hust.adventure.HustGame;

public class ScreenTransition extends BaseScreen {
    private Screen nextScreen;
    private float duration;
    private float time;
    private boolean isFadingOut;
    private boolean isFadingIn;

    public ScreenTransition(HustGame game) {
        super(game);
    }

    public void fadeOut(Screen next, float durationSec) {
        this.nextScreen = next;
        this.duration = durationSec;
        this.time = 0;
        this.isFadingOut = true;
        this.isFadingIn = false;
        System.out.println("[ScreenTransition] FadeOut started towards: " + next.getClass().getSimpleName());
    }

    public void fadeIn(float durationSec) {
        this.duration = durationSec;
        this.time = 0;
        this.isFadingIn = true;
        this.isFadingOut = false;
    }

    public boolean isTransitioning() {
        return isFadingOut || isFadingIn;
    }

    public boolean update(float delta) {
        if (isFadingOut || isFadingIn) {
            time += delta;
            return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (!isFadingOut && !isFadingIn)
            return;

        update(delta);

        float progress = Math.min(time / duration, 1f);
        float alpha = isFadingOut ? progress : (1f - progress);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, alpha));
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (progress >= 1f) {
            if (isFadingOut) {
                isFadingOut = false;
                game.setScreen(nextScreen);
                fadeIn(duration); // auto fade in after fade out
            } else if (isFadingIn) {
                isFadingIn = false;
                System.out.println("[ScreenTransition] FadeIn finished.");
            }
        }
    }

    @Override
    public void dispose() {
    }
}
