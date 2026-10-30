package io.github.julianperge.modular_planters.logic;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedBinTest {
    static SeedBin<String> bin() { return new SeedBin<>(String::equals, s -> s); }
    static List<ModuleSet> empty(int n) { return Collections.nCopies(n, ModuleSet.EMPTY); }

    @Test void addStopsAtCapacity() {
        SeedBin<String> b = bin();
        assertEquals(64, b.add("a", 70));
        assertEquals(0, b.add("a", 1));
    }
    @Test void takeTailReturnsTheTrailingRunOfIdenticalSeeds() {
        SeedBin<String> b = bin();
        b.add("a", 2); b.add("b", 3);
        assertEquals(List.of("b", "b", "b"), b.takeTail(64));
        assertEquals(List.of("a"), b.takeTail(1));
        assertEquals(1, b.size());
    }
    @Test void combosCountDistinctSeedsInFirstAppearanceOrder() {
        SeedBin<String> b = bin();
        b.add("a", 1); b.add("b", 1); b.add("a", 1);
        assertEquals(List.of(new SeedBin.Combo<>("a", 2), new SeedBin.Combo<>("b", 1)), b.combos());
    }
    @Test void seedsWithDifferentDataFormSeparateGroups() {
        SeedBin<String> b = bin();
        b.add("a", 3); b.add("b", 2); b.add("a", 1);
        List<Group> g = Grouping.of(b, empty(6));
        assertEquals(2, g.size());
        assertEquals(List.of(0, 1, 2, 5), g.get(0).seeds());
        assertEquals(List.of(3, 4), g.get(1).seeds());
    }
    @Test void seedsAddedMidGrowthDoNotJoinAGroupsProgress() {
        SeedBin<String> b = bin();
        b.add("a", 2);
        for (int t = 0; t < 5; t++) { b.advance(0); b.advance(1); }
        b.add("a", 1);
        List<Group> g = Grouping.of(b, empty(3));
        assertEquals(List.of(0, 1), g.get(0).seeds());
        assertEquals(List.of(2), g.get(1).seeds());
    }
    @Test void differentModuleSetsSplitGroups() {
        SeedBin<String> b = bin();
        b.add("a", 4);
        ModuleSet two = new ModuleSet(ModuleKind.SPEED_MK3, ModuleKind.SPEED_MK3);
        assertEquals(2, Grouping.of(b, List.of(two, two, ModuleSet.EMPTY, ModuleSet.EMPTY)).size());
    }
    @Test void onlyGrowingSeedsAreGrouped() {
        SeedBin<String> b = bin();
        b.add("a", 4);
        assertEquals(List.of(0, 1), Grouping.of(b, empty(2)).get(0).seeds());
    }
    @Test void restoreKeepsProgressAndReady() {
        SeedBin<String> b = bin();
        b.restore("a", 40, true);
        assertEquals(40, b.progress(0));
        assertTrue(b.ready(0));
    }
    @Test void resetFromNegativeResetsAll() {
        SeedBin<String> b = bin();
        b.add("a", 2);
        b.advance(0); b.advance(1); b.markReady(1);
        b.resetFrom(-1);
        assertEquals(0, b.progress(0));
        assertEquals(0, b.progress(1));
        assertFalse(b.ready(1));
    }
    @Test void resetAllZeroesProgress() {
        SeedBin<String> b = bin();
        b.add("a", 2);
        b.advance(0); b.advance(0); b.advance(1); b.markReady(1);
        b.resetAll();
        assertEquals(0, b.progress(0));
        assertEquals(0, b.progress(1));
        assertFalse(b.ready(1));
    }
    @Test void replaceSeedKeepsOrder() {
        SeedBin<String> b = bin();
        b.add("a", 1); b.add("b", 1); b.add("c", 1);
        b.replaceSeed(1, "x");
        assertEquals("a", b.seed(0));
        assertEquals("x", b.seed(1));
        assertEquals("c", b.seed(2));
    }
    @Test void markReadyThenClear() {
        SeedBin<String> b = bin();
        b.add("a", 2);
        b.markReady(0);
        assertTrue(b.ready(0));
        b.clear();
        assertEquals(0, b.size());
        b.add("a", 1);
        assertFalse(b.ready(0));
    }
    @Test void copyOneAppliesPerSeed() {
        SeedBin<String> b = new SeedBin<>(String::equals, s -> s + "'");
        b.add("a", 2);
        assertEquals("a'", b.seed(0));
        assertEquals("a'", b.seed(1));
        b.restore("z", 0, false);
        assertEquals("z", b.seed(2));
    }
    @Test void groupingSplitsOnReady() {
        SeedBin<String> b = bin();
        b.add("a", 3);
        b.markReady(1);
        List<Group> g = Grouping.of(b, empty(3));
        assertEquals(2, g.size());
        assertEquals(List.of(0, 2), g.get(0).seeds());
        assertEquals(List.of(1), g.get(1).seeds());
    }
    @Test void moduleKindWithCountZeroIsUnmodified() {
        assertEquals(empty(3), Coverage.assign(3, ModuleKind.SPEED_MK3, 0, ModuleKind.YIELD_MK1, 0));
    }
}
