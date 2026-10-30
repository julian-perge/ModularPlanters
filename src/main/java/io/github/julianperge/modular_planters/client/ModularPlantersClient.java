package io.github.julianperge.modular_planters.client;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.registry.MPMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ModularPlanters.MOD_ID, value = Dist.CLIENT)
public final class ModularPlantersClient {
    private ModularPlantersClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MPMenus.FERTILIZER.get(), StorageScreen::new);
        event.register(MPMenus.EXPORT.get(), StorageScreen::new);
        event.register(MPMenus.PLANTER.get(), PlanterScreen::new);
    }
}
