package io.github.julianperge.modular_planters.logic;

import java.util.ArrayList;
import java.util.List;

public final class Coverage {
    private Coverage() {
    }

    public static int growing(int seeds, int soil) {
        return Math.min(seeds, soil);
    }

    // Each module slot covers the first seeds in the bin, so the two slots overlap on the same seeds
    // and the fuller slot alone covers the seeds after that.
    public static List<ModuleSet> assign(int growing, ModuleKind module1, int module1Count,
                                         ModuleKind module2, int module2Count) {
        int covered1 = Math.min(module1Count, growing);
        int covered2 = Math.min(module2Count, growing);
        List<ModuleSet> sets = new ArrayList<>(growing);
        for (int i = 0; i < growing; i++) {
            sets.add(new ModuleSet(i < covered1 ? module1 : ModuleKind.NONE, i < covered2 ? module2 : ModuleKind.NONE));
        }
        return sets;
    }

    public static int upgraded(int growing, int module1Count, int module2Count) {
        return Math.min(growing, Math.max(module1Count, module2Count));
    }
}
