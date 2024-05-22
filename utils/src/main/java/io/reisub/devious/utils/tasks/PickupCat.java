package io.reisub.devious.utils.tasks;

import net.runelite.api.NPC;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;

public class PickupCat extends Task {
  private NPC cat;

  @Override
  public String getStatus() {
    return "Picking up cat";
  }

  @Override
  public boolean validate() {
    cat =
        NPCs.getNearest(
            n ->
                n.getName().equals("Cat")
                    && n.getInteracting() != null
                    && n.getInteracting() == Players.getLocal()
                    && n.hasAction("Pick-up"));

    return cat != null && !Inventory.isFull();
  }

  @Override
  public void execute() {
    cat.interact("Pick-up");

    Time.sleepTicksUntil(() -> Inventory.contains("Cat"), 20);
  }
}
