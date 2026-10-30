package io.github.julianperge.modular_planters.logic;

public final class ClocheYield {
    private ClocheYield() {
    }

    // This mirrors applyYieldModifier in ATE's AdvancedPlanterBlockEntity. The float multiplication and
    // the 1.0F shortcut are kept so the count rounds the same way ATE's does.
    public static int ateCount(int count, float modifier) {
        return modifier == 1.0F ? count : Math.max(1, Math.round(count * modifier));
    }

    // Returns the part of one drop's cloche yield bonus that ATE's rounding throws away, capped at the
    // cloche's own share, in millionths of an item. The modifier and r stay float on purpose so r rounds
    // the way ATE's count does, while the bonus math runs in double. The rule is in the "Built-in cloche" section of
    // docs/superpowers/specs/2026-10-28-modular-planters-design.md.
    public static long keptMicros(int count, float fertilizerYield, float moduleYield, double clocheYield) {
        float modifier = fertilizerYield * moduleYield * (float) clocheYield;
        int r = ateCount(count, modifier);
        double x = count * (double) fertilizerYield * (double) moduleYield;
        double e = x * clocheYield;
        double kept = Math.max(0, Math.min(e - r, x * (clocheYield - 1)));
        return Math.round(kept * RemainderLedger.ONE_ITEM);
    }
}
