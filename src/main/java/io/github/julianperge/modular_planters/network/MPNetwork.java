package io.github.julianperge.modular_planters.network;

import io.github.julianperge.modular_planters.menu.CoreMenu;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MPNetwork {
  private static final String VERSION = "1";

  private MPNetwork() {}

  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar(VERSION);
    registrar.playToClient(
        SeedBinCombosPayload.TYPE,
        SeedBinCombosPayload.STREAM_CODEC,
        (payload, context) -> {
          if (context.player().containerMenu instanceof PlanterMenu menu
              && menu.containerId == payload.containerId()) {
            menu.showCombos(payload.combos());
          }
        });
    registrar.playToClient(
        CoreTablePayload.TYPE,
        CoreTablePayload.STREAM_CODEC,
        (payload, context) -> {
          if (context.player().containerMenu instanceof CoreMenu menu
              && menu.containerId == payload.containerId()) {
            menu.showTable(payload);
          }
        });
    registrar.playToServer(
        CoreRequestPayload.TYPE,
        CoreRequestPayload.STREAM_CODEC,
        (payload, context) -> {
          if (context.player() instanceof ServerPlayer player) {
            CoreMenu.handle(player, payload);
          }
        });
  }
}
