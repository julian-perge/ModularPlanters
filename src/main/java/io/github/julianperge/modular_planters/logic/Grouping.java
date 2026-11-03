package io.github.julianperge.modular_planters.logic;

import java.util.ArrayList;
import java.util.List;

public final class Grouping {
  private Grouping() {}

  // Only the first coverage.size() seeds are growing, so seeds past that index are left out.
  public static <S> List<Group> of(SeedBin<S> bin, List<UpgradeSet> coverage) {
    int growing = Math.min(bin.size(), coverage.size());
    List<List<Integer>> members = new ArrayList<>();
    for (int i = 0; i < growing; i++) {
      List<Integer> match = null;
      for (List<Integer> candidate : members) {
        if (sameGroup(bin, coverage, candidate.getFirst(), i)) {
          match = candidate;
          break;
        }
      }
      if (match == null) {
        match = new ArrayList<>();
        members.add(match);
      }
      match.add(i);
    }
    List<Group> groups = new ArrayList<>(members.size());
    for (List<Integer> seeds : members) {
      groups.add(new Group(coverage.get(seeds.getFirst()), List.copyOf(seeds)));
    }
    return groups;
  }

  private static <S> boolean sameGroup(SeedBin<S> bin, List<UpgradeSet> coverage, int a, int b) {
    return coverage.get(a).equals(coverage.get(b))
        && bin.progress(a) == bin.progress(b)
        && bin.ready(a) == bin.ready(b)
        && bin.sameSeed(a, b);
  }
}
