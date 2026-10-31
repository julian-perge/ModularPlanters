package io.github.julianperge.modular_planters.jade;

import io.github.julianperge.modular_planters.block.CoreBlock;
import io.github.julianperge.modular_planters.block.PlanterBlock;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

// Only Jade loads this plugin and its four providers, so the mod runs without Jade installed. Jade rejects a data provider that is also a tooltip provider, so each block has one of each, sharing a uid.
@WailaPlugin
public class MPJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(CoreProvider.INSTANCE, CoreBlockEntity.class);
        registration.registerBlockDataProvider(PlanterProvider.INSTANCE, PlanterBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CoreTooltipProvider.INSTANCE, CoreBlock.class);
        registration.registerBlockComponent(PlanterTooltipProvider.INSTANCE, PlanterBlock.class);
    }
}
