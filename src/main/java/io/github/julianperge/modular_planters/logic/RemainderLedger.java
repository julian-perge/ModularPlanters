package io.github.julianperge.modular_planters.logic;

import java.util.HashMap;
import java.util.Map;

// Remainders are whole millionths of an item, so ten harvests of 0.1 add up to exactly one item with
// no floating point drift.
public final class RemainderLedger<K> {
    public static final long ONE_ITEM = 1_000_000L;

    private final Map<K, Long> remainders = new HashMap<>();

    public int addAndTakeWhole(K item, long micros) {
        if (micros < 0) {
            throw new IllegalArgumentException("micros must not be negative: " + micros);
        }
        long total = remainders.getOrDefault(item, 0L) + micros;
        long whole = total / ONE_ITEM;
        long left = total - whole * ONE_ITEM;
        if (left == 0) {
            remainders.remove(item);
        } else {
            remainders.put(item, left);
        }
        return (int) whole;
    }

    public Map<K, Long> snapshot() {
        return new HashMap<>(remainders);
    }

    public void load(Map<K, Long> saved) {
        remainders.clear();
        saved.forEach((item, micros) -> {
            if (micros > 0) {
                remainders.put(item, micros);
            }
        });
    }
}
