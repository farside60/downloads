package io.reisub.devious.utils;

import com.google.inject.Provides;
import java.util.Collection;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Locatable;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(name = "Sluwe Utils", description = "Utilities for Sluwe scripts")
@Singleton
@Slf4j
public class Utils extends Plugin {

  public static boolean isLoggedIn() {
    return Static.getClient() != null && Game.getState() == GameState.LOGGED_IN;
  }

  public static boolean isInRegion(int... regionIds) {
    Player player = Players.getLocal();

    if (player.getWorldLocation() == null) {
      return false;
    }

    for (int regionId : regionIds) {
      if (player.getWorldLocation().getRegionID() == regionId) {
        return true;
      }
    }

    return false;
  }

  public static boolean isInRegion(Collection<Integer> regionIds) {
    return isInRegion(regionIds.stream().mapToInt(i -> i).toArray());
  }

  public static boolean isInMapRegion(int... regionIds) {
    for (int id : Static.getClient().getMapRegions()) {
      for (int regionId : regionIds) {
        if (id == regionId) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isInMapRegion(Collection<Integer> regionIds) {
    return isInMapRegion(regionIds.stream().mapToInt(i -> i).toArray());
  }

  public static WorldPoint instanceToWorld(Locatable locatable) {
    return WorldPoint.fromLocalInstance(Static.getClient(), locatable.getLocalLocation());
  }

  public static WorldPoint worldToInstance(WorldPoint worldPoint) {
    final Collection<WorldPoint> points =
        WorldPoint.toLocalInstance(Static.getClient(), worldPoint);

    if (points.isEmpty()) {
      return null;
    }

    return points.iterator().next();
  }

  @Provides
  public Config getConfig(ConfigManager configManager) {
    return configManager.getConfig(Config.class);
  }

  @Override
  public void startUp() {
    log.info(this.getName() + " started");
  }

  @Override
  protected void shutDown() {
    log.info(this.getName() + " stopped");
  }
}
