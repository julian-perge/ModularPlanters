package io.github.julianperge.modular_planters.logic;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

// Parts are listed in the order the search found them, so a caller that needs a stable order must sort
// them by position. When a problem stopped the search early, parts holds what was found up to that point.
public record ScanResult<P>(Problem problem, @Nullable P problemPos, Map<PartType, List<P>> parts) {
    public boolean formed() {
        return problem == Problem.NONE;
    }

    public int partCount() {
        return parts.values().stream().mapToInt(List::size).sum();
    }
}
