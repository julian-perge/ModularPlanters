package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.ModularPlanters;
import java.util.List;
import java.util.Set;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// The data run in build.gradle is a client data run, which fires only GatherDataEvent.Client, so every provider is added here, server data included.
@EventBusSubscriber(modid = ModularPlanters.MOD_ID, value = Dist.CLIENT)
public final class MPDataGen {
    private MPDataGen() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(MPModels::new);
        event.createProvider(MPLang::new);
        event.createProvider(MPBlockTags::new);
        event.createProvider(MPRecipes.Runner::new);
        event.createProvider((output, registries) -> new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(MPLoot::new, LootContextParamSets.BLOCK)), registries));
        event.createProvider(TestStructureProvider::new);
    }
}
