package io.github.julianperge.modular_planters.logic;

import io.github.julianperge.modular_planters.logic.PlanterTicker.AdvanceResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanterTickerTest {
    static final AteNumbers D = AteDefaults.NUMBERS;
    static final ModuleSet TWO_MK3 = new ModuleSet(ModuleKind.SPEED_MK3, ModuleKind.SPEED_MK3);

    static SeedBin<String> bin(int n) {
        SeedBin<String> b = new SeedBin<>(String::equals, s -> s);
        b.add("a", n);
        return b;
    }

    @Test
    void pausesTheWholePlanterWhenEnergyIsShort() {
        SeedBin<String> b = bin(4);
        List<Group> g = Grouping.of(b, Collections.nCopies(4, TWO_MK3));
        AdvanceResult r = PlanterTicker.advance(b, g, x -> 75, m -> 512, 2047);
        assertTrue(r.pausedForPower());
        assertEquals(0, r.energyUsed());
        for (int i = 0; i < 4; i++) {
            assertEquals(0, b.progress(i));
        }
    }

    @Test
    void growthTimeIsAskedOncePerGroupAndNotWhilePaused() {
        SeedBin<String> b = bin(3);
        b.add("b", 1);
        List<Group> g = Grouping.of(b, Collections.nCopies(4, ModuleSet.EMPTY));
        AtomicInteger asked = new AtomicInteger();
        PlanterTicker.advance(b, g, x -> {
            asked.incrementAndGet();
            return 10;
        }, m -> 1, 4);
        assertEquals(2, asked.get());
        PlanterTicker.advance(b, Grouping.of(b, Collections.nCopies(4, ModuleSet.EMPTY)), x -> {
            asked.incrementAndGet();
            return 10;
        }, m -> 1, 3);
        assertEquals(2, asked.get());
    }

    @Test
    void readySeedsDrawNoPower() {
        SeedBin<String> b = bin(2);
        b.markReady(0);
        AdvanceResult r = PlanterTicker.advance(b, Grouping.of(b, Collections.nCopies(2, TWO_MK3)), x -> 75, m -> 512, 512);
        assertEquals(512, r.energyUsed());
        assertEquals(1, b.progress(1));
    }

    @Test
    void seedsBecomeReadyWhenProgressReachesGrowthTime() {
        SeedBin<String> b = bin(1);
        for (int t = 0; t < 3; t++) {
            PlanterTicker.advance(b, Grouping.of(b, List.of(ModuleSet.EMPTY)), x -> 3, m -> 1, 1);
        }
        assertTrue(b.ready(0));
    }

    @Test
    void aFullPlanterBlockMatches64PoweredPlanters() {
        int harvests = assertMatchesPoweredPlanters(64, 64, ModuleKind.SPEED_MK3, 64, ModuleKind.SPEED_MK3, 64, 1.75f, 1200);
        assertEquals(1024, harvests);
    }

    @Test
    void aPartlyUpgradedShortOnSoilBlockMatchesItsPoweredPlanters() {
        assertMatchesPoweredPlanters(64, 13, ModuleKind.SPEED_MK3, 23, ModuleKind.YIELD_MK1, 10, 1.75f, 2400);
    }

    // Runs one planter block and one Powered Planter per growing seed side by side, asserts that
    // their harvests and energy agree, and returns the harvest count.
    private static int assertMatchesPoweredPlanters(int seeds, int soil, ModuleKind m1, int m1Count,
                                                    ModuleKind m2, int m2Count, float soilModifier, int ticks) {
        int growing = Coverage.growing(seeds, soil);
        List<ModuleSet> coverage = Coverage.assign(growing, m1, m1Count, m2, m2Count);
        SeedBin<String> block = bin(seeds);
        List<ReferencePoweredPlanter> planters = new ArrayList<>();
        for (int i = 0; i < growing; i++) {
            planters.add(new ReferencePoweredPlanter());
        }

        long blockEnergy = 0;
        int blockHarvests = 0;
        for (int t = 0; t < ticks; t++) {
            AdvanceResult r = PlanterTicker.advance(block, Grouping.of(block, coverage),
                    g -> GrowthMath.growthTime(D, soilModifier, g.modules(), 1f),
                    m -> GrowthMath.powerPerSeedTick(D, m), Long.MAX_VALUE);
            blockEnergy += r.energyUsed();
            for (int i = 0; i < growing; i++) {
                if (block.ready(i)) {
                    blockHarvests++;
                    block.reset(i);
                }
            }
            for (int i = 0; i < growing; i++) {
                ModuleSet modules = coverage.get(i);
                planters.get(i).tick(GrowthMath.growthTime(D, soilModifier, modules, 1f), GrowthMath.powerPerSeedTick(D, modules));
            }
        }

        int referenceHarvests = planters.stream().mapToInt(p -> p.harvests).sum();
        long referenceEnergy = planters.stream().mapToLong(p -> p.energyUsed).sum();
        assertEquals(referenceHarvests, blockHarvests);
        assertEquals(referenceEnergy, blockEnergy);
        return blockHarvests;
    }
}
