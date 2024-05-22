package io.reisub.devious.utils.tasks;

import com.google.inject.Injector;
import io.reisub.devious.utils.Config;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;

public class KittenTask extends ParentTask {
  public static boolean handleKitten;

  public static KittenTask getInstance(Injector injector) {
    KittenTask instance = injector.getInstance(KittenTask.class);

    instance.addChildren(
        injector.getInstance(HandleKitten.class), injector.getInstance(PickupCat.class));

    return instance;
  }

  public static void withdrawKittenFood(Config config) {
    if (!Bank.isOpen()) {
      return;
    }

    if (config.handleKitten()) {
      if (config.kittenFoodAmount() == 0) {
        if (Bank.contains(config.kittenFood())) {
          Bank.withdrawAll(config.kittenFood(), Bank.WithdrawMode.ITEM);
        }
      } else {
        int count = config.kittenFoodAmount() - Inventory.getCount(config.kittenFood());

        Bank.withdraw(
            config.kittenFood(), count, Bank.WithdrawMode.ITEM);
      }
    }
  }

  @Subscribe
  private void onChatMessage(ChatMessage event) {
    if (event.getMessage().contains("Your kitten wants attention")
        || event.getMessage().contains("Your kitten is hungry.")) {
      handleKitten = true;
    }
  }
}
