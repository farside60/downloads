package io.reisub.devious.pickpocket;

import com.google.inject.Provides;
import io.reisub.devious.pickpocket.tasks.CastShadowVeil;
import io.reisub.devious.pickpocket.tasks.ClearInventory;
import io.reisub.devious.pickpocket.tasks.EquipDodgyNecklace;
import io.reisub.devious.pickpocket.tasks.HandleBank;
import io.reisub.devious.pickpocket.tasks.TakeWine;
import io.reisub.devious.utils.TickScript;
import io.reisub.devious.utils.Utils;
import io.reisub.devious.utils.api.Activity;
import io.reisub.devious.utils.tasks.Eat;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.itemstats.ItemStatPlugin;
import net.unethicalite.api.entities.Players;
import org.pf4j.Extension;
import org.slf4j.Logger;

@Extension
@PluginDependency(ItemStatPlugin.class)
@PluginDependency(Utils.class)
@PluginDescriptor(
    name = "Sluwe Pickpocket",
    description = "Cor blimey mate, what are ye doing in me pockets?",
    enabledByDefault = false)
@Slf4j
public class Pickpocket extends TickScript {
  public static final Activity THIEVING = new Activity("Thieving");
  @Inject private Config config;
  @Getter private Target.Location nearestLocation;

  @Provides
  public Config getConfig(ConfigManager configManager) {
    return configManager.getConfig(Config.class);
  }

  @Override
  public Logger getLogger() {
    return log;
  }

  @Override
  protected void onStart() {
    super.onStart();

    nearestLocation = config.target().getNearest();
    Eat eatTask = injector.getInstance(Eat.class);
    eatTask.setThreshold(config.eatHp());

    addTask(ClearInventory.class);
    addTask(TakeWine.class);
    addTask(eatTask);
    addTask(CastShadowVeil.class);
    addTask(EquipDodgyNecklace.class);
    addTask(HandleBank.class);
    addTask(io.reisub.devious.pickpocket.tasks.Pickpocket.class);
  }

  @Subscribe
  private void onAnimationChanged(AnimationChanged event) {
    if (!isRunning()) {
      return;
    }

    Actor actor = event.getActor();
    if (actor == null || !actor.equals(Players.getLocal())) {
      return;
    }

    switch (Players.getLocal().getAnimation()) {
      case 388:
        setActivity(Activity.IDLE);
        break;
      default:
    }
  }

  @Subscribe
  private void onStatChanged(StatChanged event) {
    if (event.getSkill() == Skill.THIEVING) {
      setActivity(Activity.IDLE);
    }
  }
}
