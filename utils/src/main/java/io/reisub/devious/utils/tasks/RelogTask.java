package io.reisub.devious.utils.tasks;

import io.reisub.devious.utils.Utils;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;

public abstract class RelogTask extends Task {
  @Override
  public String getStatus() {
    return "Log out and back in";
  }

  @Override
  public void execute() {
    if (Utils.isLoggedIn()) {
      Game.logout();
      Time.sleepUntil(Game::isOnLoginScreen, 5000);
    }

    if (!Utils.isLoggedIn()) {
      Static.getClient().login(false);
      Time.sleepUntil(Utils::isLoggedIn, 5000);
    }
  }
}
