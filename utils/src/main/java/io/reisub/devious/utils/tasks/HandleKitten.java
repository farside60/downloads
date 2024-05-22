package io.reisub.devious.utils.tasks;

import io.reisub.devious.utils.Config;
import javax.inject.Inject;
import net.runelite.api.DialogOption;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.client.Static;

public class HandleKitten extends Task {
  @Inject private Config config;

  private NPC kitten;
  private int lastAte;
  private int lastStroke;
  private int strokesDone;

  @Override
  public String getStatus() {
    return "Taking care of kitten";
  }

  @Override
  public boolean validate() {
    kitten =
        NPCs.getNearest(
            n ->
                n.getName().equals("Kitten")
                    && n.getInteracting() != null
                    && n.getInteracting() == Players.getLocal()
                    && n.hasAction("Pick-up"));

    return KittenTask.handleKitten
        && Inventory.contains(config.kittenFood())
        && kitten != null
        && Reachable.isInteractable(kitten);
  }

  @Override
  public void execute() {
    feedKitten();
    Time.sleepTick();
    strokeKitten();

    KittenTask.handleKitten = false;
  }

  private void feedKitten() {
    if (Static.getClient().getTickCount() - lastAte < 100) {
      return;
    }

    final int count = Inventory.getCount(config.kittenFood());
    final Item food = Inventory.getFirst(config.kittenFood());

    if (food == null) {
      return;
    }

    food.useOn(kitten);

    if (Time.sleepTicksUntil(() -> Inventory.getCount(config.kittenFood()) < count, 15)) {
      lastAte = Static.getClient().getTickCount();
    }
  }

  private void strokeKitten() {
    if (Static.getClient().getTickCount() - lastStroke < 100) {
      return;
    }

    int strokesToDo = strokesDone == 0 ? 2 : 1;

    for (int i = 0; i < strokesToDo; i++) {
      kitten.interact("Interact");

      if (!Time.sleepTicksUntil(Dialog::isViewingOptions, 10)) {
        return;
      }

      Dialog.invokeDialog(DialogOption.CHAT_OPTION_ONE);
      strokesDone++;

      Time.sleepTicksUntil(Dialog::canContinuePlayer, 10);
    }

    if (strokesDone == 2) {
      strokesDone = 0;
      lastStroke = Static.getClient().getTickCount();
    }
  }
}
