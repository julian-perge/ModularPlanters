package io.github.julianperge.modular_planters.logic;

// One Powered Planter reduced to the numbers in Agritech: Evolved's AdvancedPlanterBlockEntity.tick.
// A planter that has reached its growth time harvests in that same tick and starts over.
final class ReferencePoweredPlanter {
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
