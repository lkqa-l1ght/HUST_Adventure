package hust.adventure.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntSet;
import hust.adventure.entities.player.input.PlayerController;

/**
 * System for handling player input and mapping it to game actions. Consolidates dynamic key state tracking using an
 * internal IntSet to respect memory constraints.
 */
public class InputReader implements PlayerController, InputProcessor {
    private boolean up, down, left, right, running;
    private final IntSet justPressedKeys = new IntSet();

    /**
     * Checks if a key was just pressed in the current frame.
     *
     * @param keycode the keycode to check
     * @return true if the key was just pressed, false otherwise
     */
    public boolean isKeyJustPressed(final int keycode) {
        return justPressedKeys.contains(keycode);
    }

    public void update() {
        justPressedKeys.clear();
    }

    @Override
    public boolean isUp() {
        return up;
    }

    @Override
    public boolean isDown() {
        return down;
    }

    @Override
    public boolean isLeft() {
        return left;
    }

    @Override
    public boolean isRight() {
        return right;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isSpaceJustPressed() {
        return isKeyJustPressed(Input.Keys.SPACE);
    }

    @Override
    public boolean isQJustPressed() {
        return isKeyJustPressed(Input.Keys.Q);
    }

    @Override
    public boolean isEJustPressed() {
        return isKeyJustPressed(Input.Keys.E);
    }

    @Override
    public boolean isFJustPressed() {
        return isKeyJustPressed(Input.Keys.F);
    }

    @Override
    public boolean isInventoryJustPressed() {
        return isKeyJustPressed(Input.Keys.I);
    }

    @Override
    public boolean isEnterJustPressed() {
        return isKeyJustPressed(Input.Keys.ENTER);
    }

    @Override
    public boolean isDebugJustPressed() {
        return isKeyJustPressed(Input.Keys.F2);
    }

    @Override
    public boolean isHitboxJustPressed() {
        return isKeyJustPressed(Input.Keys.F3);
    }

    @Override
    public int getJustPressedNum() {
        for (int i = 1; i <= 5; i++) {
            if (isKeyJustPressed(Input.Keys.NUM_1 + i - 1)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean keyDown(int keycode) {
        justPressedKeys.add(keycode);
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP)
            up = true;
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN)
            down = true;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT)
            left = true;
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT)
            right = true;
        if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT)
            running = true;
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP)
            up = false;
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN)
            down = false;
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT)
            left = false;
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT)
            right = false;
        if (keycode == Input.Keys.SHIFT_LEFT || keycode == Input.Keys.SHIFT_RIGHT)
            running = false;
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
