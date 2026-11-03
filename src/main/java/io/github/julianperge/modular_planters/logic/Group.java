package io.github.julianperge.modular_planters.logic;

import java.util.List;

public record Group(UpgradeSet upgrades, List<Integer> seeds) {
  public int size() {
    return seeds.size();
  }

  public int first() {
    return seeds.getFirst();
  }
}
