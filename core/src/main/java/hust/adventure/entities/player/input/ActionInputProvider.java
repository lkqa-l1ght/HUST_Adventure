package hust.adventure.entities.player.input;

/**
 * Interface for checking trigger-based gameplay and skill actions.
 */
public interface ActionInputProvider {
    boolean isSpaceJustPressed();

    boolean isQJustPressed();

    boolean isEJustPressed();

    boolean isFJustPressed();

    default boolean isAttackJustPressed() {
        return isSpaceJustPressed();
    }

    default boolean isSkillQJustPressed() {
        return isQJustPressed();
    }

    default boolean isSkillEJustPressed() {
        return isEJustPressed();
    }

    default boolean isSkillFJustPressed() {
        return isFJustPressed();
    }
}
