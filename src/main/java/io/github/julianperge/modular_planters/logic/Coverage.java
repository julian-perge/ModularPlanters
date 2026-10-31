package io.github.julianperge.modular_planters.logic;

import java.util.ArrayList;
import java.util.List;

public final class Coverage {
    private Coverage() {
    }

    public static int growing(int seeds, int soil) {
        return Math.min(seeds, soil);
    }

    // Each upgrade slot covers the first seeds in the bin, so the two slots overlap on the same seeds
    // and the fuller slot alone covers the seeds after that.
    public static List<UpgradeSet> assign(int growing, UpgradeKind upgrade1, int upgrade1Count,
                                         UpgradeKind upgrade2, int upgrade2Count) {
        int covered1 = Math.min(upgrade1Count, growing);
        int covered2 = Math.min(upgrade2Count, growing);
        List<UpgradeSet> sets = new ArrayList<>(growing);
        for (int i = 0; i < growing; i++) {
            sets.add(new UpgradeSet(i < covered1 ? upgrade1 : UpgradeKind.NONE, i < covered2 ? upgrade2 : UpgradeKind.NONE));
        }
        return sets;
    }

    public static int upgraded(int growing, int upgrade1Count, int upgrade2Count) {
        return Math.min(growing, Math.max(upgrade1Count, upgrade2Count));
    }
}
