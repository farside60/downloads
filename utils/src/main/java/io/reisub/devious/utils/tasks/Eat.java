package io.reisub.devious.utils.tasks;

import com.google.common.collect.ImmutableList;
import io.reisub.devious.utils.api.Activity;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import lombok.Setter;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.client.Static;

/**
 * This is a very simple eating task to be used in any script that requires replenishing a player's
 * health. This does not support more advanced mechanics like 1-tick eating or combining drinks and
 * food. It simply eats or drinks whatever it finds in the inventory.
 */
public class Eat extends Task {
  @Setter private int threshold;
  @Setter private boolean checkMissing;
  private int lastTick;
  private Item consumable;

  private final List<Integer> ignoreIds =
      ImmutableList.of(
          ItemID.ROCK_CAKE,
          ItemID.DWARVEN_ROCK_CAKE,
          ItemID.DWARVEN_ROCK_CAKE_7510,
          ItemID.BLOOD_PINT,
          ItemID.LAVA_EEL);

  /**
   * Create a task that eats or drinks the first item it finds in the inventory once the player's
   * health drops to 50%.
   */
  @Inject
  public Eat() {
    threshold = Skills.getLevel(Skill.HITPOINTS) / 2;
  }

  @Override
  public String getStatus() {
    return "Eating";
  }

  @Override
  public Activity getActivity() {
    return Activity.EATING;
  }

  @Override
  public boolean validate() {
    if (checkMissing) {
      if (Combat.getMissingHealth() < threshold) {
        return false;
      }
    } else {
      if (Combat.getCurrentHealth() > threshold) {
        return false;
      }
    }

    List<Item> consumables =
        Inventory.getAll(i -> i.hasAction("Eat", "Drink") && !ignoreIds.contains(i.getId()));

    if (consumables.isEmpty()) {
      return false;
    }

    // this sort makes sure that we consume the consumables with the highest ID first
    // this way we consume stuff like a slice of cake before a full cake or a 1 dose potion before
    // a 2 dose, a 2 dose before a 3 dose, et cetera
    consumables.sort(Comparator.comparingInt(Item::getId).reversed());
    consumable = consumables.get(0);

    return Static.getClient().getTickCount() - lastTick >= 3;
  }

  @Override
  public void execute() {
    lastTick = Static.getClient().getTickCount();
    consumable.interact("Eat", "Drink");
  }
}
