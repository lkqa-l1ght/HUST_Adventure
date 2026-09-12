package hust.adventure.ui;

/**
 * Data Transfer Object containing all necessary data to render active spells, status effects, and artifacts.
 */
public class StatusEffectsData {
    private boolean hasNao;
    private boolean hasUsb;
    private float enemyTimeScale;
    private float showEnemiesTimer;
    private boolean isSpeedBoosted;
    private boolean isHpRegen;
    private boolean isConfused;

    public StatusEffectsData(boolean hasNao, boolean hasUsb, float enemyTimeScale, float showEnemiesTimer,
            boolean isSpeedBoosted, boolean isHpRegen, boolean isConfused) {
        this.hasNao = hasNao;
        this.hasUsb = hasUsb;
        this.enemyTimeScale = enemyTimeScale;
        this.showEnemiesTimer = showEnemiesTimer;
        this.isSpeedBoosted = isSpeedBoosted;
        this.isHpRegen = isHpRegen;
        this.isConfused = isConfused;
    }

    public void set(boolean hasNao, boolean hasUsb, float enemyTimeScale, float showEnemiesTimer,
            boolean isSpeedBoosted, boolean isHpRegen, boolean isConfused) {
        this.hasNao = hasNao;
        this.hasUsb = hasUsb;
        this.enemyTimeScale = enemyTimeScale;
        this.showEnemiesTimer = showEnemiesTimer;
        this.isSpeedBoosted = isSpeedBoosted;
        this.isHpRegen = isHpRegen;
        this.isConfused = isConfused;
    }

    public boolean isHasNao() {
        return hasNao;
    }

    public boolean isHasUsb() {
        return hasUsb;
    }

    public float getEnemyTimeScale() {
        return enemyTimeScale;
    }

    public float getShowEnemiesTimer() {
        return showEnemiesTimer;
    }

    public boolean isSpeedBoosted() {
        return isSpeedBoosted;
    }

    public boolean isHpRegen() {
        return isHpRegen;
    }

    public boolean isConfused() {
        return isConfused;
    }
}
