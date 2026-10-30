package io.github.julianperge.modular_planters;

import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import io.github.julianperge.modular_planters.gametest.MPTestRegistration;
import io.github.julianperge.modular_planters.network.MPNetwork;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPCreativeTab;
import io.github.julianperge.modular_planters.registry.MPDataComponents;
import io.github.julianperge.modular_planters.registry.MPItems;
import io.github.julianperge.modular_planters.registry.MPMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.gametest.GameTestHooks;

@Mod(ModularPlanters.MOD_ID)
public class ModularPlanters {
    public static final String MOD_ID = "modular_planters";

    public ModularPlanters(IEventBus modEventBus, ModContainer modContainer) {
        if (ModList.get().isLoaded("ae2")) {
            Ae2Integration.init(modEventBus);
        }
        MPBlocks.BLOCKS.register(modEventBus);
        MPItems.ITEMS.register(modEventBus);
        MPBlockEntities.TYPES.register(modEventBus);
        MPDataComponents.COMPONENTS.register(modEventBus);
        MPMenus.MENUS.register(modEventBus);
        modEventBus.addListener(MPBlockEntities::registerCapabilities);
        modEventBus.addListener(MPNetwork::register);
        MPCreativeTab.TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MPConfig.SPEC);
        if (GameTestHooks.isGametestEnabled()) {
            MPTestRegistration.register(modEventBus);
        }
    }
}
