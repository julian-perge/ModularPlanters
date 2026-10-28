package io.github.julianperge.modular_planters.logic;

import java.util.List;
import java.util.function.ToIntFunction;

public final class PlanterTicker {
    private PlanterTicker() {
    }

    public record AdvanceResult(long energyUsed, boolean pausedForPower) {
    }

    // A planter short on power does nothing that tick and never asks for a growth time, because the
    // Powered Planter takes its energy before it posts the processing time event.
    public static <S> AdvanceResult advance(SeedBin<S> bin, List<Group> groups, ToIntFunction<Group> growthTime,
                                            ToIntFunction<ModuleSet> powerPerSeed, long energyAvailable) {
        long draw = 0;
        for (Group group : groups) {
            if (!bin.ready(group.first())) {
                draw += (long) group.size() * powerPerSeed.applyAsInt(group.modules());
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
