package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public final class DoorwayHandlers {
    private DoorwayHandlers() {
    }

    public static @Nullable EnergyHandler energy(CoreBlockEntity core) {
        PowerPool power = core.power();
        return power == null ? null : power.insertView();
    }

    public static @Nullable ResourceHandler<ItemResource> items(CoreBlockEntity core) {
        FertilizerPool fertilizer = core.fertilizer();
        OutputSink outputs = core.outputs();
        if (fertilizer == null || outputs == null) {
            return null;
        }
        return new CombinedResourceHandler<>(fertilizer.insertView(), outputs.extractView());
    }
}
