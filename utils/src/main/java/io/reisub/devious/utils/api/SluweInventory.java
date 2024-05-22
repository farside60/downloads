package io.reisub.devious.utils.api;

import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

public class SluweInventory {
  public static boolean hasHammer() {
    return hasAnyItemInventoryOrEquipped(ItemID.HAMMER, ItemID.IMCANDO_HAMMER);
  }

  public static boolean hasAnyItemInventoryOrEquipped(int... ids) {
    return hasAnyItemInventoryOrEquipped(Predicates.ids(ids));
  }

  public static boolean hasAnyItemInventoryOrEquipped(String... names) {
    return hasAnyItemInventoryOrEquipped(Predicates.names(names));
  }

  public static boolean hasAnyItemInventoryOrEquipped(Predicate<Item> filter) {
    return Inventory.contains(filter) || Equipment.contains(filter);
  }

  public static boolean hasAllItemsInventoryOrEquipped(int... ids) {
    for (int id : ids) {
      if (!hasAnyItemInventoryOrEquipped(id)) {
        return false;
      }
    }

    return true;
  }

  public static boolean hasAllItemsInventoryOrEquipped(String... names) {
    for (String name : names) {
      if (!hasAnyItemInventoryOrEquipped(name)) {
        return false;
      }
    }

    return true;
  }

  public static Item getItemInventoryOrEquipped(int... ids) {
    return getItemInventoryOrEquipped(Predicates.ids(ids));
  }

  public static Item getItemInventoryOrEquipped(String... names) {
    return getItemInventoryOrEquipped(Predicates.names(names));
  }

  public static Item getItemInventoryOrEquipped(Predicate<Item> filter) {
    final Item item = Inventory.getFirst(filter);

    if (item != null) {
      return item;
    }

    return Equipment.getFirst(filter);
  }

  public static void dropAll() {
    dropAll(Inventory.getAll());
  }

  /**
   * Drops all items while making sure not to go over the maximum amount of actions per tick (10).
   * Sleeps on item 9 and 19 to allow for an extra action right before and right after dropping.
   *
   * @param items list of items that should be dropped
   */
  public static void dropAll(List<Item> items) {
    for (int i = 0; i < items.size(); i++) {
      if (i == 9 || i == 19) {
        Time.sleepTick();
      }

      items.get(i).drop();
    }
  }
}
