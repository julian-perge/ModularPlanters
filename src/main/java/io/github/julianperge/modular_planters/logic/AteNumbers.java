package io.github.julianperge.modular_planters.logic;

public record AteNumbers(
        int advancedBaseProcessingTime,
        int basePowerConsumption,
        double speedMk1, double speedMk2, double speedMk3,
        double powerMk1, double powerMk2, double powerMk3,
        double yieldMk1, double yieldMk2, double yieldMk3,
        double yieldPenaltyMk1, double yieldPenaltyMk2, double yieldPenaltyMk3,
        double clocheSpeed,
        double clocheYield) {
}
