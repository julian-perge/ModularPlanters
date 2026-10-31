package io.github.julianperge.modular_planters.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.julianperge.modular_planters.logic.UpgradeKind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageTest {
    static final UpgradeSet TWO_MK3 = new UpgradeSet(SPEED_MK3, SPEED_MK3);

    @Test void growingIsTheSmallerOfSeedsAndSoil() {
        assertEquals(13, Coverage.growing(64, 13));
        assertEquals(5, Coverage.growing(5, 64));
    }
    @Test void bothUpgradesPairOnTheSameSeedsFirst() {
        List<UpgradeSet> c = Coverage.assign(64, SPEED_MK3, 23, YIELD_MK1, 10);
        assertEquals(64, c.size());
        assertEquals(new UpgradeSet(SPEED_MK3, YIELD_MK1), c.get(0));
        assertEquals(new UpgradeSet(SPEED_MK3, YIELD_MK1), c.get(9));
        assertEquals(new UpgradeSet(SPEED_MK3, NONE), c.get(10));
        assertEquals(new UpgradeSet(SPEED_MK3, NONE), c.get(22));
        assertEquals(UpgradeSet.EMPTY, c.get(23));
        assertEquals(UpgradeSet.EMPTY, c.get(63));
    }
    @Test void aFullerSecondSlotCoversTheMiddleSeeds() {
        List<UpgradeSet> c = Coverage.assign(10, SPEED_MK1, 3, SPEED_MK2, 7);
        assertEquals(new UpgradeSet(SPEED_MK1, SPEED_MK2), c.get(2));
        assertEquals(new UpgradeSet(NONE, SPEED_MK2), c.get(3));
        assertEquals(new UpgradeSet(NONE, SPEED_MK2), c.get(6));
        assertEquals(UpgradeSet.EMPTY, c.get(7));
    }
    @Test void upgradeCountsAboveGrowingAreCapped() {
        List<UpgradeSet> c = Coverage.assign(13, SPEED_MK3, 64, SPEED_MK3, 64);
        assertEquals(13, c.size());
        assertTrue(c.stream().allMatch(TWO_MK3::equals));
    }
    @Test void upgradedCountsSeedsWithAnyUpgrade() {
        assertEquals(23, Coverage.upgraded(64, 23, 10));
        assertEquals(13, Coverage.upgraded(13, 64, 0));
    }
}
