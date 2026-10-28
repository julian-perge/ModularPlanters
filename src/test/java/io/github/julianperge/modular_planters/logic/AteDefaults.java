package io.github.julianperge.modular_planters.logic;

public final class AteDefaults {
    // The values follow AteNumbers' field order: base processing time and base power, then the
    // MK1 to MK3 values for speed module speed, speed module power, yield module yield and yield
    // module speed penalty, then the cloche's speed and yield bonuses.
    public static final AteNumbers NUMBERS = new AteNumbers(600, 128,
            1.5, 1.75, 2.0,
            1.5, 1.75, 2.0,
            1.5, 1.75, 2.0,
            0.95, 0.90, 0.75,
            1.15, 1.10);

    private AteDefaults() {
    }
}
