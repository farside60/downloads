package io.reisub.devious.utils.api;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.runelite.api.NullItemID;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.items.DepositBox;
import net.unethicalite.api.widgets.Widgets;

public class SluweDepositBox {
  private static final int EMPTY_SLOT_ID = NullItemID.NULL_6512;

  public static List<Widget> getAll(Predicate<Widget> filter) {
    return getAll().stream().filter(filter).collect(Collectors.toList());
  }

  public static List<Widget> getAll() {
    if (!DepositBox.isOpen()) {
      return new LinkedList<>();
    }

    return Widgets.getChildren(
        WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER,
        w -> w.getItemId() != SluweDepositBox.EMPTY_SLOT_ID);
  }

  public static void depositAll(Widget widget) {
    widget.interact("Deposit-All");
  }

  public static void depositAll(int... ids) {
    depositAll(w -> Arrays.stream(ids).anyMatch(id -> w.getItemId() == id));
  }

  public static void depositAll(String... names) {
    depositAll(
        w -> Arrays.stream(names).anyMatch(name -> Text.standardize(w.getName()).equals(name)));
  }

  public static void depositAll(Predicate<Widget> filter) {
    Set<Widget> items =
        getAll(filter).stream()
            .filter(SluwePredicates.distinctByProperty(Widget::getItemId))
            .collect(Collectors.toSet());

    items.forEach(SluweDepositBox::depositAll);
  }

  public static void depositAllExcept(int... ids) {
    depositAllExcept(w -> Arrays.stream(ids).anyMatch(id -> w.getItemId() == id));
  }

  public static void depositAllExcept(String... names) {
    depositAllExcept(
        w -> Arrays.stream(names).anyMatch(name -> Text.standardize(w.getName()).equals(name)));
  }

  public static void depositAllExcept(Predicate<Widget> filter) {
    depositAll(filter.negate());
  }
}
