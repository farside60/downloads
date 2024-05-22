package io.reisub.devious.utils.tasks;

import net.runelite.api.GameState;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.client.Static;

public class OpenInventory extends Task {
  @Override
  public String getStatus() {
    return "Opening inventory";
  }

  @Override
  public boolean validate() {
    return Static.getClient().getGameState() == GameState.LOGGED_IN
        && !Bank.isOpen()
        && Tabs.isOpen(Tab.LOG_OUT);
  }

  @Override
  public void execute() {
    Tabs.open(Tab.INVENTORY);
    Time.sleepTick();
  }
}
