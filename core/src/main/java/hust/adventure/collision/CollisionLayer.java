package hust.adventure.collision;

public class CollisionLayer {
    public static final int PLAYER = 0x01; // 00001
    public static final int ENEMY = 0x02; // 00010
    public static final int PLAYER_BULLET = 0x04; // 00100
    public static final int ENEMY_BULLET = 0x08; // 01000
    public static final int WALL = 0x10; // 10000
    public static final int ITEM = 0x20; // 00100000

    public static final int ALL = 0xFF;
}
