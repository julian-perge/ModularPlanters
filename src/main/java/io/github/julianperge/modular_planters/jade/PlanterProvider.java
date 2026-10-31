package io.github.julianperge.modular_planters.jade;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PlanterProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {
    INSTANCE;

    private static final Identifier ID = Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "planter");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof PlanterBlockEntity blockEntity) {
            data.merge(JadeInfo.planter(blockEntity));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        JadeInfo.planterLines(accessor.getServerData()).forEach(tooltip::add);
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
