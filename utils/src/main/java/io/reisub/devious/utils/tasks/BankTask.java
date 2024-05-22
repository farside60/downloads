package io.reisub.devious.utils.tasks;

import io.reisub.devious.utils.Constants;
import io.reisub.devious.utils.api.ConfigList;
import io.reisub.devious.utils.api.SluwePredicates;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetID;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.packets.DialogPackets;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public abstract class BankTask extends Task {
  protected Instant last = Instant.EPOCH;
  protected Instant lastStamina = Instant.EPOCH;
  @Getter @Setter private Set<WorldPoint> bankIgnoreLocations;
  @Getter @Setter private Set<WorldPoint> bankLocations;
  @Getter @Setter private String name = null;
  @Getter @Setter private boolean openMainTab = false;
  @Getter @Setter private int movingCheck = 3;
  @Getter @Setter private int waitTicks = 15;

  @Override
  public String getStatus() {
    return "Banking";
  }

  protected boolean open() {
    if (Bank.isOpen()) {
      if (openMainTab) {
        Bank.openMainTab();

        Time.sleepTicksUntil(Bank::isMainTabOpen, 2);
      }

      last = Instant.now();
      return true;
    }

    TileObject bankObject = null;
    NPC bankNpc = null;

    if (name != null) {
      if (bankLocations != null) {
        bankNpc =
            NPCs.getNearest(
                n -> n.getName().equals(name) && bankLocations.contains(n.getWorldLocation()));
      } else if (bankIgnoreLocations != null) {
        bankNpc =
            NPCs.getNearest(
                n ->
                    n.getName().equals(name)
                        && !bankIgnoreLocations.contains(n.getWorldLocation()));
      } else {
        bankNpc = NPCs.getNearest(name);
      }

      if (bankNpc == null) {
        return false;
      }
    } else {
      bankObject = getBankObject();
    }

    if (bankObject != null) {
      if (bankObject.hasAction("Bank")) {
        bankObject.interact("Bank");
      } else if (bankObject.hasAction("Use")) {
        bankObject.interact("Use");
      } else {
        bankObject.interact(0);
      }
    } else {
      if (bankNpc == null) {
        bankNpc = getBankNpc();
      }

      if (bankNpc == null) {
        return false;
      }

      if (bankNpc.hasAction("Bank")) {
        bankNpc.interact("Bank");
      } else {
        bankNpc.interact(0);
      }
    }

    if (movingCheck > 0) {
      if (!Time.sleepTicksUntil(
          () -> Bank.isOpen() || Players.getLocal().isMoving(), movingCheck)) {
        return false;
      }
    }

    Time.sleepTicksUntil(Bank::isOpen, waitTicks);

    last = Instant.now();

    if (Bank.isOpen() && openMainTab) {
      Bank.openMainTab();

      Time.sleepTicksUntil(Bank::isMainTabOpen, 2);
    }

    return Bank.isOpen();
  }

  protected boolean open(String name, int waitTicks, int movingCheck, boolean openMainTab) {
    if (Bank.isOpen()) {
      if (openMainTab) {
        Bank.openMainTab();
      }

      return true;
    }

    NPC bankNpc = NPCs.getNearest(name);
    if (bankNpc == null) {
      return false;
    }

    GameThread.invoke(() -> bankNpc.interact("Bank"));

    if (movingCheck > 0) {
      if (!Time.sleepTicksUntil(
          () ->
              Bank.isOpen()
                  || Players.getLocal().isMoving()
                  || Widgets.isVisible(Widgets.get(WidgetID.BANK_PIN_GROUP_ID, 0)),
          movingCheck)) {
        return false;
      }
    }

    Time.sleepTicksUntil(Bank::isOpen, waitTicks);

    last = Instant.now();

    if (Bank.isOpen() && openMainTab && !Bank.isMainTabOpen()) {
      Bank.openMainTab();
    }

    return Bank.isOpen();
  }

  protected void close() {
    if (Bank.isOpen()) {
      DialogPackets.closeInterface();
    }
  }

  protected void drinkStamina() {
    if (!Bank.contains(Predicates.ids(Constants.STAMINA_POTION_IDS))) {
      return;
    }

    Bank.withdraw(Predicates.ids(Constants.STAMINA_POTION_IDS), 1, Bank.WithdrawMode.ITEM);

    Item potion = null;
    int start = Static.getClient().getTickCount();

    while (potion == null && Static.getClient().getTickCount() < start + 10) {
      Time.sleepTick();
      potion = Bank.Inventory.getFirst(Predicates.ids(Constants.STAMINA_POTION_IDS));
    }

    if (potion == null) {
      return;
    }

    potion.interact("Drink");
    Time.sleepTick();

    Bank.depositAll(Predicates.ids(Constants.STAMINA_POTION_IDS));

    lastStamina = Instant.now();
  }

  protected boolean isLastBankDurationAgo(Duration duration) {
    return Duration.between(last, Instant.now()).compareTo(duration) >= 0;
  }

  protected boolean isStaminaExpiring(Duration timeLeft) {
    if (!Movement.isStaminaBoosted()) {
      return true;
    }

    if (lastStamina == Instant.EPOCH) {
      return false;
    }

    int seconds = Equipment.contains(ItemID.RING_OF_ENDURANCE) ? 240 : 120;

    timeLeft = Duration.ofSeconds(seconds).minus(timeLeft);

    return Duration.between(lastStamina, Instant.now()).compareTo(timeLeft) >= 0;
  }

  protected boolean isBankObjectAvailable() {
    return getBankObject() != null || getBankNpc() != null;
  }

  protected TileObject getBankObject() {
    if (bankLocations != null) {
      return TileObjects.getNearest(
          SluwePredicates.idsAtLocations(Constants.BANK_OBJECT_IDS, bankLocations));
    } else if (bankIgnoreLocations != null) {
      return TileObjects.getNearest(
          SluwePredicates.idsNotAtLocations(Constants.BANK_OBJECT_IDS, bankIgnoreLocations));
    } else {
      return TileObjects.getNearest(Predicates.ids(Constants.BANK_OBJECT_IDS));
    }
  }

  protected NPC getBankNpc() {
    if (bankLocations != null) {
      return NPCs.getNearest(
          SluwePredicates.idsAtLocations(Constants.BANK_NPC_IDS, bankLocations));
    } else if (bankIgnoreLocations != null) {
      return NPCs.getNearest(
          SluwePredicates.idsNotAtLocations(Constants.BANK_NPC_IDS, bankIgnoreLocations));
    } else {
      return NPCs.getNearest(Predicates.ids(Constants.BANK_NPC_IDS));
    }
  }

  protected boolean hasEverythingInInventory(ConfigList list) {
    for (Map.Entry<Integer, Integer> entry : list.getIntegers().entrySet()) {
      if (Inventory.getCount(entry.getKey()) < entry.getValue()) {
        return false;
      }
    }

    for (Map.Entry<String, Integer> entry : list.getStrings().entrySet()) {
      if (Inventory.getCount(Predicates.nameContains(entry.getKey(), false)) < entry.getValue()) {
        return false;
      }
    }

    return true;
  }
}
