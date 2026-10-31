package io.github.julianperge.modular_planters.jade;

import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PlanterTooltipProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        JadeInfo.planterLines(accessor.getServerData()).forEach(tooltip::add);
    }

    @Override
    public Identifier getUid() {
        return PlanterProvider.ID;
    }
}
