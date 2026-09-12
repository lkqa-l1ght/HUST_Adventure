package hust.adventure.entities.factory;

import com.badlogic.gdx.graphics.Color;

import hust.adventure.entities.ExpGem;
import hust.adventure.entities.ItemDrop;
import hust.adventure.entities.Projectile;
import hust.adventure.entities.base.MapObject;
import hust.adventure.inventory.Inventory;
import hust.adventure.items.base.Item;
import hust.adventure.entities.player.Player;
import hust.adventure.entities.player.input.PlayerController;
import hust.adventure.entities.base.LightProvider;

/**
 * Abstract Factory interface for entity creation.
 */
public interface EntityFactory {
    Player createPlayer(float x, float y, Inventory inventory, PlayerController controller);

    MapObject createEnemy(String type, float x, float y);

    Projectile createProjectile(float x, float y, float vx, float vy, float damage, Color color, boolean isPlayer);

    ItemDrop createItemDrop(float x, float y, Item item, Color color);

    ExpGem createExpGem(float x, float y, float amount);

    MapObject createFloatingBook(float x, float y, LightProvider lightProvider);

    MapObject createCandle(float x, float y, LightProvider lightProvider);

    MapObject createStaticNPC(float x, float y, String name, Color color);

    void freeEntity(MapObject entity);
}
