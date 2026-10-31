package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.jade.CoreProvider;
import io.github.julianperge.modular_planters.jade.PlanterProvider;
import snownee.jade.api.IComponentProvider;

// JadeTests calls this class only when Jade is loaded, because it imports Jade's API and would fail to load without Jade.
final class JadeRegistration {
    private JadeRegistration() {
    }

    // On a physical client Jade refuses the whole plugin when a data provider is also a tooltip provider, and a GameTest server never runs that check.
    static boolean dataProvidersAreServerOnly() {
        return !IComponentProvider.class.isAssignableFrom(CoreProvider.class)
                && !IComponentProvider.class.isAssignableFrom(PlanterProvider.class);
    }
}
