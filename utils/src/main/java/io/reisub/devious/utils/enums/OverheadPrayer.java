package io.reisub.devious.utils.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OverheadPrayer {
  PROTECT_FROM_MAGIC(SluwePrayer.PROTECT_FROM_MAGIC),
  PROTECT_FROM_MISSILES(SluwePrayer.PROTECT_FROM_MISSILES),
  PROTECT_FROM_MELEE(SluwePrayer.PROTECT_FROM_MELEE),
  RETRIBUTION(SluwePrayer.RETRIBUTION),
  REDEMPTION(SluwePrayer.REDEMPTION),
  SMITE(SluwePrayer.SMITE);

  private final SluwePrayer prayer;
}
