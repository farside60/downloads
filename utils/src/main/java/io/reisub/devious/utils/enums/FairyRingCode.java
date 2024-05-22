package io.reisub.devious.utils.enums;

import java.util.function.Supplier;
import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.widgets.Widgets;

public enum FairyRingCode {
  AIQ(13, 0, 1),
  AJP(19, -1, 1);

  private final Supplier<Widget> widgetSupplier;
  /**
   * Pathfinding doesn't work when we're standing on a fairy ring, so we should move away from it
   * after teleporting.
   * -1 is west, 0 is unchanged, 1 is east
   */
  @Getter private final int defaultXoffset;
  /**
   * Pathfinding doesn't work when we're standing on a fairy ring, so we should move away from it
   * after teleporting.
   * -1 is south, 0 is unchanged, 1 is north
   */
  @Getter private final int defaultYoffset;

  FairyRingCode(int widgetId) {
    this(widgetId, -1, 0);
  }

  FairyRingCode(int widgetId, int defaultXoffset, int defaultYoffset) {
    widgetSupplier = () -> Widgets.get(381, widgetId);
    this.defaultXoffset = defaultXoffset;
    this.defaultYoffset = defaultYoffset;
  }
  
  public String getCode() {
    return name();
  }

  public Widget getWidget() {
    return widgetSupplier.get();
  }
}
