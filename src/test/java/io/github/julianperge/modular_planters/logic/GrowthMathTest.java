package io.github.julianperge.modular_planters.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GrowthMathTest {
    static final AteNumbers D = AteDefaults.NUMBERS;
    static final ModuleSet TWO_MK3 = new ModuleSet(ModuleKind.SPEED_MK3, ModuleKind.SPEED_MK3);

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
    @Test void noModulesUsesTheBaseNumbersWithTheCloche() {
        assertEquals(522, GrowthMath.growthTime(D, 1.0f, ModuleSet.EMPTY, 1.0f));
        assertEquals(128, GrowthMath.powerPerSeedTick(D, ModuleSet.EMPTY));
    }
    @Test void yieldModulesSlowGrowthAndAddNoPower() {
        ModuleSet ym3 = new ModuleSet(ModuleKind.YIELD_MK3, ModuleKind.NONE);
        assertEquals(696, GrowthMath.growthTime(D, 1.0f, ym3, 1.0f));
        assertEquals(128, GrowthMath.powerPerSeedTick(D, ym3));
        assertEquals(2.0f, ym3.yieldModifier(D));
    }
    @Test void mixedSpeedAndYieldModules() {
        ModuleSet mix = new ModuleSet(ModuleKind.SPEED_MK2, ModuleKind.YIELD_MK1);
        assertEquals(314, GrowthMath.growthTime(D, 1.0f, mix, 1.0f));
        assertEquals(224, GrowthMath.powerPerSeedTick(D, mix));
        assertEquals(1.5f, mix.yieldModifier(D));
    }
    @Test void growthTimeIsAtLeastOneTick() {
        assertEquals(1, GrowthMath.growthTime(D, 10_000f, TWO_MK3, 1.0f));
    }
    @Test void moduleItemIdsMapToKinds() {
        assertEquals(ModuleKind.SPEED_MK1, ModuleKind.fromItemId("agritechevolved:sm_mk1"));
        assertEquals(ModuleKind.YIELD_MK3, ModuleKind.fromItemId("agritechevolved:ym_mk3"));
        assertEquals(ModuleKind.NONE, ModuleKind.fromItemId("minecraft:dirt"));
    }
}
