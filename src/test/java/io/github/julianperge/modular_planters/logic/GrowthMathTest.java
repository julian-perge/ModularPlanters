package io.github.julianperge.modular_planters.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrowthMathTest {
    static final AteNumbers D = AteDefaults.NUMBERS;
    static final UpgradeSet TWO_MK3 = new UpgradeSet(UpgradeKind.SPEED_MK3, UpgradeKind.SPEED_MK3);

    @Test void nickelSetupMatchesTheFuelLineNote() {
        assertEquals(75, GrowthMath.growthTime(D, 1.75f, TWO_MK3, 1.0f));
        assertEquals(512, GrowthMath.powerPerSeedTick(D, TWO_MK3));
    }
    @Test void wheatOnInfusedFarmlandWithTwoMk3() {
        assertEquals(87, GrowthMath.growthTime(D, 1.5f, TWO_MK3, 1.0f));
    }
    @Test void boneMealSpeedsGrowth() {
        assertEquals(72, GrowthMath.growthTime(D, 1.5f, TWO_MK3, 1.2f));
    }
    @Test void noUpgradesUsesTheBaseNumbersWithTheCloche() {
        assertEquals(522, GrowthMath.growthTime(D, 1.0f, UpgradeSet.EMPTY, 1.0f));
        assertEquals(128, GrowthMath.powerPerSeedTick(D, UpgradeSet.EMPTY));
    }
    @Test void yieldUpgradesSlowGrowthAndAddNoPower() {
        UpgradeSet ym3 = new UpgradeSet(UpgradeKind.YIELD_MK3, UpgradeKind.NONE);
        assertEquals(696, GrowthMath.growthTime(D, 1.0f, ym3, 1.0f));
        assertEquals(128, GrowthMath.powerPerSeedTick(D, ym3));
        assertEquals(2.0f, ym3.yieldModifier(D));
    }
    @Test void mixedSpeedAndYieldUpgrades() {
        UpgradeSet mix = new UpgradeSet(UpgradeKind.SPEED_MK2, UpgradeKind.YIELD_MK1);
        assertEquals(314, GrowthMath.growthTime(D, 1.0f, mix, 1.0f));
        assertEquals(224, GrowthMath.powerPerSeedTick(D, mix));
        assertEquals(1.5f, mix.yieldModifier(D));
    }
    @Test void growthTimeIsAtLeastOneTick() {
        assertEquals(1, GrowthMath.growthTime(D, 10_000f, TWO_MK3, 1.0f));
    }
    @Test void upgradeItemIdsMapToKinds() {
        assertEquals(UpgradeKind.SPEED_MK1, UpgradeKind.fromItemId("agritechevolved:sm_mk1"));
        assertEquals(UpgradeKind.YIELD_MK3, UpgradeKind.fromItemId("agritechevolved:ym_mk3"));
        assertEquals(UpgradeKind.NONE, UpgradeKind.fromItemId("minecraft:dirt"));
    }
}
