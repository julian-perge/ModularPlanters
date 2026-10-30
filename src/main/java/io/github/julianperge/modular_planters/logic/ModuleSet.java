package io.github.julianperge.modular_planters.logic;

// Each modifier multiplies the first slot and then the second in single precision, the same order Agritech: Evolved uses, so the speed modifier rounds to the same whole tick as its planter, the yield modifier to the same drop counts and the power modifier to the same draw.
public record ModuleSet(ModuleKind first, ModuleKind second) {
    public static final ModuleSet EMPTY = new ModuleSet(ModuleKind.NONE, ModuleKind.NONE);

    public float speedModifier(AteNumbers n) {
        float speed = 1.0f;
        float penalty = 1.0f;
        speed *= first.speed(n);
        penalty *= first.speedPenalty(n);
        speed *= second.speed(n);
        penalty *= second.speedPenalty(n);
        return speed * penalty;
    }

    public float powerModifier(AteNumbers n) {
        float power = 1.0f;
        power *= first.power(n);
        power *= second.power(n);
        return power;
    }

    public float yieldModifier(AteNumbers n) {
        float yield = 1.0f;
        yield *= first.yield(n);
        yield *= second.yield(n);
        return yield;
    }
}
