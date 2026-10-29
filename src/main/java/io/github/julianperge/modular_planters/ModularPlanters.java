package io.github.julianperge.modular_planters;

import io.github.julianperge.modular_planters.gametest.MPTestRegistration;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPCreativeTab;
import io.github.julianperge.modular_planters.registry.MPItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.gametest.GameTestHooks;

@Mod(ModularPlanters.MOD_ID)
public class ModularPlanters {
    public static final String MOD_ID = "modular_planters";

    public ModularPlanters(IEventBus modEventBus, ModContainer modContainer) {
        MPBlocks.BLOCKS.register(modEventBus);
        MPItems.ITEMS.register(modEventBus);
        MPCreativeTab.TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MPConfig.SPEC);
        if (GameTestHooks.isGametestEnabled()) {
            MPTestRegistration.register(modEventBus);
        }
    }
}
