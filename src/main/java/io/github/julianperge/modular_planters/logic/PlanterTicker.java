package io.github.julianperge.modular_planters.logic;

import java.util.List;
import java.util.function.ToIntFunction;

public final class PlanterTicker {
  private PlanterTicker() {}

  public record AdvanceResult(long energyUsed, boolean pausedForPower) {}

  // A planter short on power advances no seed and asks for no growth time that tick, because the
  // Advanced Planter takes its energy before it posts the processing time event, but the caller
  // still harvests the seeds that were already ready.
  public static <S> AdvanceResult advance(
      SeedBin<S> bin,
      List<Group> groups,
      ToIntFunction<Group> growthTime,
      ToIntFunction<UpgradeSet> powerPerSeed,
      long energyAvailable) {
    long draw = 0;
    for (Group group : groups) {
      if (!bin.ready(group.first())) {
        draw += (long) group.size() * powerPerSeed.applyAsInt(group.upgrades());
      }
    }
    if (draw > energyAvailable) {
      return new AdvanceResult(0, true);
    }
    for (Group group : groups) {
      if (bin.ready(group.first())) {
        continue;
      }
      int time = growthTime.applyAsInt(group);
      for (int seed : group.seeds()) {
        bin.advance(seed);
        if (bin.progress(seed) >= time) {
          bin.markReady(seed);
        }
      }
    }
    return new AdvanceResult(draw, false);
  }
}
