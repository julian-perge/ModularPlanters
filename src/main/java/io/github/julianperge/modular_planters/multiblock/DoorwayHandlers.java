package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class DoorwayHandlers {
  private DoorwayHandlers() {}

  public static @Nullable EnergyHandler energy(CoreBlockEntity core) {
    PowerPool power = core.power();
    return power == null ? null : power.insertView();
  }

  public static @Nullable ResourceHandler<ItemResource> items(CoreBlockEntity core) {
    return core.items();
  }

  // The core builds this once each time it rebuilds its pools, and a removed fertilizer or export
  // block changes the slot count, so until that rebuild each call combines the views again.
  public static ResourceHandler<ItemResource> items(FertilizerPool fertilizer, OutputSink outputs) {
    ResourceHandler<ItemResource> whole =
        new CombinedResourceHandler<>(fertilizer.insertView(), outputs.extractView());
    return new DelegatingResourceHandler<>(
        () ->
            fertilizer.allLive() && outputs.allLive()
                ? whole
                : new CombinedResourceHandler<>(fertilizer.insertView(), outputs.extractView()));
  }
}
