package io.reisub.devious.pickpocket.tasks;

import com.google.common.collect.ImmutableSet;
import io.reisub.devious.pickpocket.Config;
import io.reisub.devious.pickpocket.Pickpocket;
import io.reisub.devious.pickpocket.Target;
import io.reisub.devious.utils.api.Activity;
import io.reisub.devious.utils.api.SluweBank;
import io.reisub.devious.utils.api.SluweMovement;
import io.reisub.devious.utils.tasks.BankTask;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChanges;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.plugins.itemstats.StatsChanges;
import net.runelite.client.plugins.itemstats.stats.Stats;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Rune;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.client.Static;

public class HandleBank extends BankTask {
  @Inject private ItemStatChanges statChanges;

  @Inject private Pickpocket plugin;

  @Inject private Config config;

  @Override
  public boolean validate() {
    return plugin.isCurrentActivity(Activity.IDLE)
        && Players.getLocal().getModelHeight() != 1000
        && (Inventory.isFull()
            || (!Inventory.contains(config.food())
                && Skills.getBoostedLevel(Skill.HITPOINTS) <= config.eatHp()));
  }

  @Override
  public void execute() {
    Time.sleepTick();

    if (!open()) {
      if (config.target() == Target.VALLESSIA_VON_PITT) {
        goToSepulchreBank();
      } else {
        SluweMovement.walkTo(plugin.getNearestLocation().getBankLocation(), 1);
      }

      return;
    }

    if (config.target() == Target.VALLESSIA_VON_PITT) {
      SluweBank.depositAllExcept(
          false,
          i ->
              i.getName().startsWith("Vyre noble")
                  || i.getId() == ItemID.COSMIC_RUNE
                  || i.getId() == ItemID.RUNE_POUCH
                  || i.getName().startsWith("Rogue")
                  || i.getName().contains("em bag"));
    } else {
      Bank.depositInventory();
    }

    withdrawNecklaces();

    if (config.castShadowVeil()) {
      withdrawCosmics();
    }

    Item gemBag =
        Bank.Inventory.getFirst(
            Predicates.ids(
                ImmutableSet.of(
                    ItemID.GEM_BAG,
                    ItemID.GEM_BAG_12020,
                    ItemID.GEM_BAG_25628,
                    ItemID.OPEN_GEM_BAG)));
    if (gemBag != null) {
      SluweBank.bankInventoryInteract(gemBag, "Empty");
    }

    Item food = Bank.getFirst(config.food());
    if (food == null) {
      return;
    }

    if (config.healAtBank()) {
      int quantity = Math.floorDiv(Combat.getMissingHealth(), heals(food.getId()));

      if (quantity > 0) {
        Bank.withdraw(config.food(), quantity, Bank.WithdrawMode.ITEM);

        close();

        Time.sleepTicksUntil(() -> Inventory.getCount(config.food()) >= quantity, 5);
        Time.sleepTick();

        List<Item> allFood = Inventory.getAll(config.food());

        for (Item foodPiece : allFood) {
          foodPiece.interact("Eat");
          Time.sleepTicks(3);
        }
      }
    }

    if (config.foodQuantity() > 0) {
      if (!Bank.isOpen()) {
        open();
        Time.sleepTicksUntil(Bank::isOpen, 5);
      }

      Bank.withdraw(config.food(), config.foodQuantity(), Bank.WithdrawMode.ITEM);
      Time.sleepTicksUntil(() -> Inventory.contains(config.food()), 3);
    }
  }

  private int heals(int itemId) {
    Effect effect = statChanges.get(itemId);
    if (effect != null) {
      StatsChanges statsChanges = effect.calculate(Static.getClient());
      for (StatChange statChange : statsChanges.getStatChanges()) {
        if (statChange.getStat().getName().equals(Stats.HITPOINTS.getName())) {
          return statChange.getTheoretical();
        }
      }
    }

    return 0;
  }

  private void withdrawNecklaces() {
    int necklacesToWithdraw =
        config.dodgyNecklacesQuantity()
            - Inventory.getCount(ItemID.DODGY_NECKLACE)
            - Equipment.getCount(ItemID.DODGY_NECKLACE);

    if (necklacesToWithdraw > 0 && Bank.contains(ItemID.DODGY_NECKLACE)) {
      Bank.withdraw(ItemID.DODGY_NECKLACE, necklacesToWithdraw, Bank.WithdrawMode.ITEM);

      if (Equipment.fromSlot(EquipmentInventorySlot.AMULET) == null
          || Equipment.fromSlot(EquipmentInventorySlot.AMULET).getId() != ItemID.DODGY_NECKLACE) {
        Time.sleepTicksUntil(() -> Inventory.contains(ItemID.DODGY_NECKLACE), 3);

        Item necklace = Bank.Inventory.getFirst(ItemID.DODGY_NECKLACE);

        if (necklace != null) {
          SluweBank.bankInventoryInteract(necklace, "Wear");
        }
      }
    }
  }

  private void withdrawCosmics() {
    if (Rune.COSMIC.getQuantity() >= 5) {
      return;
    }

    if (Bank.contains(ItemID.COSMIC_RUNE)) {
      Bank.withdrawAll(ItemID.COSMIC_RUNE, Bank.WithdrawMode.ITEM);
    } else if (Bank.contains(ItemID.RUNE_POUCH)) {
      Bank.withdraw(ItemID.RUNE_POUCH, 1, Bank.WithdrawMode.ITEM);
    }
  }

  private void goToSepulchreBank() {
    Inventory.getAll(i -> i.getName().startsWith("Vyre noble")).forEach(i -> i.interact("Wear"));

    WorldPoint doorLocation = new WorldPoint(3662, 3378, 0);

    TileObject door = TileObjects.getFirstAt(doorLocation, ObjectID.DOOR_39406);
    if (door != null) {
      door.interact("Open");
      Time.sleepTicksUntil(
          () -> TileObjects.getFirstAt(doorLocation.dy(-1), ObjectID.DOOR_39408) != null, 10);
    }

    Movement.walk(doorLocation.dy(-1));
    Time.sleepTicksUntil(
        () -> Players.getLocal().getWorldLocation().equals(doorLocation.dy(-1)), 10);

    door = TileObjects.getFirstAt(doorLocation.dy(-1), ObjectID.DOOR_39408);
    if (door != null) {
      door.interact("Close");
    }
    private void goToSepulchreBank() {
      // Wear Vyre noble outfit
      Inventory.getAll(i -> i.getName().startsWith("Vyre noble")).forEach(i -> i.interact("Wear"));

      WorldPoint firstDoorLocation = new WorldPoint(3662, 3378, 0);
      WorldPoint secondDoorLocation = firstDoorLocation.dy(-1);

      // Open the first door
      TileObject firstDoor = TileObjects.getFirstAt(firstDoorLocation, ObjectID.DOOR_39406);
      if (firstDoor != null) {
        firstDoor.interact("Open");
        Time.sleepTicksUntil(
                () -> TileObjects.getFirstAt(secondDoorLocation, ObjectID.DOOR_39408) != null, 10);
      }

      // Move to the position behind the first door
      Movement.walk(secondDoorLocation);
      Time.sleepTicksUntil(
              () -> Players.getLocal().getWorldLocation().equals(secondDoorLocation), 10);

      // Close the first door behind you
      TileObject secondDoor = TileObjects.getFirstAt(secondDoorLocation, ObjectID.DOOR_39408);
      if (secondDoor != null) {
        secondDoor.interact("Close");
        Time.sleepTicks(2);
      }

      // Move to the mausoleum door location
      WorldPoint mausoleumDoorLocation = new WorldPoint(3654, 3385, 0);
      Movement.walk(mausoleumDoorLocation);
      Time.sleepTicksUntil(
              () -> Players.getLocal().getWorldLocation().equals(mausoleumDoorLocation), 10);

      // Interact with the mausoleum door to enter
      TileObject mausoleumDoor = TileObjects.getFirstAt(mausoleumDoorLocation, ObjectID.38596); // Update with actual door ID if needed
      if (mausoleumDoor != null) {
        mausoleumDoor.interact("Enter");
      }

      // Walk to the bank inside the mausoleum
      SluweMovement.walkTo(config.target().getNearest().getBankLocation());
    }

