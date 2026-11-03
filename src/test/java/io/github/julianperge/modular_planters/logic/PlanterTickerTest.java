package io.github.julianperge.modular_planters.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.julianperge.modular_planters.logic.PlanterTicker.AdvanceResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PlanterTickerTest {
  static final AteNumbers D = AteDefaults.NUMBERS;
  static final UpgradeSet TWO_MK3 = new UpgradeSet(UpgradeKind.SPEED_MK3, UpgradeKind.SPEED_MK3);

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
  void pausedResultReportsTheDrawItWanted() {
    SeedBin<String> b = bin(4);
    List<Group> g = Grouping.of(b, Collections.nCopies(4, UpgradeSet.EMPTY));
    assertEquals(new AdvanceResult(0, true), PlanterTicker.advance(b, g, x -> 75, m -> 10, 39));
  }

  @Test
  void runningResultWantsWhatItUsed() {
    SeedBin<String> b = bin(4);
    List<Group> g = Grouping.of(b, Collections.nCopies(4, UpgradeSet.EMPTY));
    assertEquals(new AdvanceResult(40, false), PlanterTicker.advance(b, g, x -> 75, m -> 10, 40));
  }

  @Test
  void growthTimeIsAskedOncePerGroupAndNotWhilePaused() {
    SeedBin<String> b = bin(3);
    b.add("b", 1);
    List<Group> g = Grouping.of(b, Collections.nCopies(4, UpgradeSet.EMPTY));
    AtomicInteger asked = new AtomicInteger();
    PlanterTicker.advance(
        b,
        g,
        x -> {
          asked.incrementAndGet();
          return 10;
        },
        m -> 1,
        4);
    assertEquals(2, asked.get());
    PlanterTicker.advance(
        b,
        Grouping.of(b, Collections.nCopies(4, UpgradeSet.EMPTY)),
        x -> {
          asked.incrementAndGet();
          return 10;
        },
        m -> 1,
        3);
    assertEquals(2, asked.get());
  }

  @Test
  void readySeedsDrawNoPower() {
    SeedBin<String> b = bin(2);
    b.markReady(0);
    AtomicInteger asked = new AtomicInteger();
    AdvanceResult r =
        PlanterTicker.advance(
            b,
            Grouping.of(b, Collections.nCopies(2, TWO_MK3)),
            x -> {
              asked.incrementAndGet();
              return 75;
            },
            m -> 512,
            512);
    assertEquals(512, r.energyUsed());
    assertEquals(1, b.progress(1));
    assertEquals(0, b.progress(0));
    assertTrue(b.ready(0));
    assertEquals(1, asked.get());
  }

  @Test
  void seedsBecomeReadyWhenProgressReachesGrowthTime() {
    SeedBin<String> b = bin(1);
    for (int t = 0; t < 3; t++) {
      PlanterTicker.advance(b, Grouping.of(b, List.of(UpgradeSet.EMPTY)), x -> 3, m -> 1, 1);
    }
    assertTrue(b.ready(0));
  }

  @Test
  void multiGroupPlanterPausesAsAWhole() {
    SeedBin<String> b = bin(2);
    b.add("b", 2);
    List<Group> g = Grouping.of(b, Collections.nCopies(4, UpgradeSet.EMPTY));
    assertEquals(2, g.size());
    AdvanceResult r = PlanterTicker.advance(b, g, x -> 10, m -> 10, 39);
    assertEquals(new AdvanceResult(0, true), r);
    for (int i = 0; i < 4; i++) {
      assertEquals(0, b.progress(i));
    }
  }

  @Test
  void emptyGroupListUsesNothing() {
    SeedBin<String> b = bin(0);
    assertEquals(
        new AdvanceResult(0, false), PlanterTicker.advance(b, List.of(), x -> 10, m -> 10, 0));
  }

  @Test
  void allReadyGroupsNeedNoEnergy() {
    SeedBin<String> b = bin(2);
    b.markReady(0);
    b.markReady(1);
    List<Group> g = Grouping.of(b, Collections.nCopies(2, UpgradeSet.EMPTY));
    assertEquals(new AdvanceResult(0, false), PlanterTicker.advance(b, g, x -> 10, m -> 10, 0));
  }

  @Test
  void groupsAtDifferentProgress() {
    SeedBin<String> b = bin(1);
    PlanterTicker.advance(
        b, Grouping.of(b, Collections.nCopies(1, UpgradeSet.EMPTY)), x -> 5, m -> 1, 1);
    b.add("a", 1);
    List<Group> g = Grouping.of(b, Collections.nCopies(2, UpgradeSet.EMPTY));
    assertEquals(2, g.size());
    AdvanceResult r = PlanterTicker.advance(b, g, x -> 5, m -> 3, 6);
    assertEquals(new AdvanceResult(6, false), r);
    assertEquals(2, b.progress(0));
    assertEquals(1, b.progress(1));
  }

  @Test
  void growthTimeChangingBetweenTicks() {
    SeedBin<String> b = bin(1);
    List<UpgradeSet> one = List.of(UpgradeSet.EMPTY);
    PlanterTicker.advance(b, Grouping.of(b, one), x -> 10, m -> 1, 1);
    PlanterTicker.advance(b, Grouping.of(b, one), x -> 10, m -> 1, 1);
    assertFalse(b.ready(0));
    PlanterTicker.advance(b, Grouping.of(b, one), x -> 3, m -> 1, 1);
    assertTrue(b.ready(0));
  }

  @Test
  void aFullPlanterBlockMatches64AdvancedPlanters() {
    int harvests =
        assertMatchesAdvancedPlanters(
            64, 64, UpgradeKind.SPEED_MK3, 64, UpgradeKind.SPEED_MK3, 64, 1.75f, 1200);
    assertEquals(1024, harvests);
  }

  @Test
  void aPartlyUpgradedShortOnSoilBlockMatchesItsAdvancedPlanters() {
    assertMatchesAdvancedPlanters(
        64, 13, UpgradeKind.SPEED_MK3, 23, UpgradeKind.YIELD_MK1, 10, 1.75f, 2400);
  }

  private static int assertMatchesAdvancedPlanters(
      int seeds,
      int soil,
      UpgradeKind m1,
      int m1Count,
      UpgradeKind m2,
      int m2Count,
      float soilModifier,
      int ticks) {
    int growing = Coverage.growing(seeds, soil);
    List<UpgradeSet> coverage = Coverage.assign(growing, m1, m1Count, m2, m2Count);
    SeedBin<String> block = bin(seeds);
    List<ReferenceAdvancedPlanter> planters = new ArrayList<>();
    for (int i = 0; i < growing; i++) {
      planters.add(new ReferenceAdvancedPlanter());
    }

    long blockEnergy = 0;
    int blockHarvests = 0;
    for (int t = 0; t < ticks; t++) {
      AdvanceResult r =
          PlanterTicker.advance(
              block,
              Grouping.of(block, coverage),
              g -> GrowthMath.growthTime(D, soilModifier, g.upgrades(), 1f),
              m -> GrowthMath.powerPerSeedTick(D, m),
              Long.MAX_VALUE);
      blockEnergy += r.energyUsed();
      for (int i = 0; i < growing; i++) {
        if (block.ready(i)) {
          blockHarvests++;
          block.reset(i);
        }
      }
      for (int i = 0; i < growing; i++) {
        UpgradeSet upgrades = coverage.get(i);
        planters
            .get(i)
            .tick(
                GrowthMath.growthTime(D, soilModifier, upgrades, 1f),
                GrowthMath.powerPerSeedTick(D, upgrades));
      }
    }

    int referenceHarvests = planters.stream().mapToInt(p -> p.harvests).sum();
    long referenceEnergy = planters.stream().mapToLong(p -> p.energyUsed).sum();
    assertEquals(referenceHarvests, blockHarvests);
    assertEquals(referenceEnergy, blockEnergy);
    return blockHarvests;
  }
}
