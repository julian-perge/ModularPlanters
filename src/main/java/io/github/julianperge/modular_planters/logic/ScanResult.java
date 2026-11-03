package io.github.julianperge.modular_planters.logic;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

// Parts are listed in the order the search found them, so a caller that needs a stable order must
// sort
// them by position. When a problem stopped the search early, parts holds what was found up to that
// point.
public record ScanResult<P>(Problem problem, @Nullable P problemPos, Map<PartType, List<P>> parts) {
  public ScanResult {
    Map<PartType, List<P>> copy = new EnumMap<>(PartType.class);
    parts.forEach((type, found) -> copy.put(type, List.copyOf(found)));
    parts = Collections.unmodifiableMap(copy);
  }

  public boolean formed() {
    return problem == Problem.NONE;
  }

  public int partCount() {
    return parts.values().stream().mapToInt(List::size).sum();
  }
}
