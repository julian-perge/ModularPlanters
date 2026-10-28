package io.github.julianperge.modular_planters.logic;

import java.util.List;

public record Group(ModuleSet modules, List<Integer> seeds) {
    public int size() {
        return seeds.size();
    }

    public int first() {
        return seeds.getFirst();
    }
}
