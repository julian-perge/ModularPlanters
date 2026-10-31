package io.github.julianperge.modular_planters.logic;

// This reduces one Advanced Planter to the numbers in Agritech: Evolved's AdvancedPlanterBlockEntity.tick,
// and it assumes the planter always has room for its outputs and enough power to run every tick.
final class ReferenceAdvancedPlanter {
    int ticks;
    long energyUsed;
    int harvests;

    void tick(int growthTime, int power) {
        energyUsed += power;
        if (++ticks >= growthTime) {
            harvests++;
            ticks = 0;
        }
    }
}
