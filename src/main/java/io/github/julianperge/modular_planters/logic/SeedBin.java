package io.github.julianperge.modular_planters.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

// Holds one entry per seed in insertion order. Each entry carries its own growth progress and ready
// flag, because seeds added at different times are at different points in their growth.
public final class SeedBin<S> {
    public static final int CAPACITY = 64;

    public record Combo<S>(S seed, int count) {
    }

    private static final class Entry<S> {
        S seed;
        int progress;
        boolean ready;

        Entry(S seed, int progress, boolean ready) {
            this.seed = seed;
            this.progress = progress;
            this.ready = ready;
        }
    }

    private final BiPredicate<S, S> sameData;
    private final UnaryOperator<S> copyOne;
    private final List<Entry<S>> entries = new ArrayList<>();

    public SeedBin(BiPredicate<S, S> sameData, UnaryOperator<S> copyOne) {
        this.sameData = sameData;
        this.copyOne = copyOne;
    }

    public int size() {
        return entries.size();
    }

    public S seed(int i) {
        return entries.get(i).seed;
    }

    public int progress(int i) {
        return entries.get(i).progress;
    }

    public boolean ready(int i) {
        return entries.get(i).ready;
    }

    boolean sameSeed(int a, int b) {
        return sameData.test(entries.get(a).seed, entries.get(b).seed);
    }

    public List<Combo<S>> combos() {
        List<S> seeds = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Entry<S> entry : entries) {
            int found = -1;
            for (int i = 0; i < seeds.size(); i++) {
                if (sameData.test(seeds.get(i), entry.seed)) {
                    found = i;
                    break;
                }
            }
            if (found < 0) {
                seeds.add(entry.seed);
                counts.add(1);
            } else {
                counts.set(found, counts.get(found) + 1);
            }
        }
        List<Combo<S>> combos = new ArrayList<>(seeds.size());
        for (int i = 0; i < seeds.size(); i++) {
            combos.add(new Combo<>(seeds.get(i), counts.get(i)));
        }
        return combos;
    }

    public int add(S seed, int count) {
        int added = Math.max(0, Math.min(count, CAPACITY - entries.size()));
        for (int i = 0; i < added; i++) {
            entries.add(new Entry<>(copyOne.apply(seed), 0, false));
        }
        return added;
    }

    // Restore skips CAPACITY and copyOne because it rebuilds entries that were already checked when they were saved.
    public void restore(S seed, int progress, boolean ready) {
        entries.add(new Entry<>(seed, progress, ready));
    }

    public List<S> takeTail(int max) {
        List<S> taken = new ArrayList<>();
        if (entries.isEmpty()) {
            return taken;
        }
        S tail = entries.getLast().seed;
        while (taken.size() < max && !entries.isEmpty() && sameData.test(tail, entries.getLast().seed)) {
            taken.add(entries.removeLast().seed);
        }
        return taken;
    }

    public void clear() {
        entries.clear();
    }

    public void replaceSeed(int i, S seed) {
        entries.get(i).seed = seed;
    }

    public void advance(int i) {
        entries.get(i).progress++;
    }

    public void markReady(int i) {
        entries.get(i).ready = true;
    }

    public void reset(int i) {
        Entry<S> entry = entries.get(i);
        entry.progress = 0;
        entry.ready = false;
    }

    public void resetFrom(int fromIndex) {
        for (int i = Math.max(0, fromIndex); i < entries.size(); i++) {
            reset(i);
        }
    }

    public void resetAll() {
        resetFrom(0);
    }
}
