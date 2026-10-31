package io.github.julianperge.modular_planters.jade;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum CoreProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier ID = Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "core");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof CoreBlockEntity blockEntity) {
            data.merge(JadeInfo.core(blockEntity));
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
