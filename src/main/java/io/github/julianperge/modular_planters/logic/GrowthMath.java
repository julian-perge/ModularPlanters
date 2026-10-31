package io.github.julianperge.modular_planters.logic;

public final class GrowthMath {
    private GrowthMath() {
    }

    // The built-in cloche always applies, and the factors multiply in the order of the Powered
    // Planter's tick so the float result rounds to the same tick.
    public static int growthTime(AteNumbers n, float soilModifier, UpgradeSet upgrades, float fertilizerSpeed) {
        float total = soilModifier * upgrades.speedModifier(n) * fertilizerSpeed * (float) n.clocheSpeed();
        return Math.max(1, Math.round(n.advancedBaseProcessingTime() / total));
    }

    public static int powerPerSeedTick(AteNumbers n, UpgradeSet upgrades) {
        return Math.round(n.basePowerConsumption() * upgrades.powerModifier(n));
    }
}
