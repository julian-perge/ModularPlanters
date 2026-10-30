package io.github.julianperge.modular_planters.logic;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClocheYieldTest {
    @Test void ateCountMatchesAteRounding() {
        assertEquals(1, ClocheYield.ateCount(1, 1.1f));
        assertEquals(2, ClocheYield.ateCount(1, 2.2f));
        assertEquals(2, ClocheYield.ateCount(2, 1.1f));
        assertEquals(1, ClocheYield.ateCount(1, 1.0f));
        assertEquals(1, ClocheYield.ateCount(3, 0.1f));
    }
    @Test void oneItemDropAtTheDefaultClocheGivesOneExtraEveryTenthHarvest() {
        RemainderLedger<String> ledger = new RemainderLedger<>();
        List<Integer> extraHarvests = new ArrayList<>();
        for (int harvest = 1; harvest <= 30; harvest++) {
            if (ledger.addAndTakeWhole("essence", ClocheYield.keptMicros(1, 1f, 1f, 1.1)) == 1) {
                extraHarvests.add(harvest);
            }
        }
        assertEquals(List.of(10, 20, 30), extraHarvests);
    }
    @Test void yieldMk3KeepsTwoTenths() {
        assertEquals(200_000, ClocheYield.keptMicros(1, 1f, 2f, 1.1));
    }
    @Test void neverKeepsMoreThanTheClochesShare() {
        assertEquals(120_000, ClocheYield.keptMicros(1, 1.2f, 1f, 1.1));
    }
    @Test void nothingKeptWhenAteRoundsUp() {
        assertEquals(0, ClocheYield.keptMicros(1, 1f, 1.4f, 1.1));
    }
    @Test void aClocheBelowOneKeepsNothing() {
        assertEquals(0, ClocheYield.keptMicros(1, 1f, 1f, 0.9));
    }
    @Test void ledgerKeepsASeparateRemainderPerItem() {
        RemainderLedger<String> ledger = new RemainderLedger<>();
        ledger.addAndTakeWhole("a", 600_000);
        assertEquals(0, ledger.addAndTakeWhole("b", 600_000));
        assertEquals(1, ledger.addAndTakeWhole("a", 600_000));
        assertEquals(Map.of("a", 200_000L, "b", 600_000L), ledger.snapshot());
    }
    @Test void ateCountUsesFloatMath() {
        assertEquals(104, ClocheYield.ateCount(50, 1.9f * 1.0f * (float) 1.1));
    }
    @Test void keptMicrosUsesFloatMath() {
        assertEquals(499_999L, ClocheYield.keptMicros(50, 1.9f, 1.0f, 1.1));
    }
    @Test void keptMicrosWithCountAboveOne() {
        assertEquals(400_000L, ClocheYield.keptMicros(4, 1f, 1f, 1.1));
    }
    @Test void clocheBelowOneWithPositiveRemainder() {
        assertEquals(0, ClocheYield.keptMicros(5, 1.2f, 1f, 0.9));
    }
    @Test void ledgerRoundTripsThroughLoad() {
        RemainderLedger<String> ledger = new RemainderLedger<>();
        ledger.addAndTakeWhole("a", 250_000);
        ledger.addAndTakeWhole("b", 750_000);
        Map<String, Long> saved = ledger.snapshot();
        RemainderLedger<String> loaded = new RemainderLedger<>();
        loaded.load(saved);
        assertEquals(saved, loaded.snapshot());
    }
    @Test void ledgerDropsZeroEntries() {
        RemainderLedger<String> ledger = new RemainderLedger<>();
        assertEquals(1, ledger.addAndTakeWhole("a", RemainderLedger.ONE_ITEM));
        assertEquals(Map.of(), ledger.snapshot());
    }
    @Test void ledgerRejectsNegativeMicros() {
        RemainderLedger<String> ledger = new RemainderLedger<>();
        assertThrows(IllegalArgumentException.class, () -> ledger.addAndTakeWhole("a", -1));
    }
    @Test void ledgerSkipsNegativeLoadedValues() {
        RemainderLedger<String> ledger = new RemainderLedger<>();
        ledger.load(Map.of("a", -5L));
        assertEquals(Map.of(), ledger.snapshot());
    }
}
