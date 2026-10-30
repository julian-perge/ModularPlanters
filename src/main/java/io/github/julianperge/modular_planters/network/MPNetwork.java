package io.github.julianperge.modular_planters.network;

import io.github.julianperge.modular_planters.menu.PlanterMenu;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MPNetwork {
    private static final String VERSION = "1";

    private MPNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(SeedBinCombosPayload.TYPE, SeedBinCombosPayload.STREAM_CODEC, (payload, context) -> {
            if (context.player().containerMenu instanceof PlanterMenu menu && menu.containerId == payload.containerId()) {
                menu.showCombos(payload.combos());
            }
        });
    }
}
