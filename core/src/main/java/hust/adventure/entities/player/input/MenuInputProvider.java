package hust.adventure.entities.player.input;

/**
 * Interface for checking menu and UI related inputs.
 */
public interface MenuInputProvider {
    boolean isInventoryJustPressed();

    boolean isEnterJustPressed();

    int getJustPressedNum();
}
