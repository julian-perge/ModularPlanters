package io.github.julianperge.modular_planters.jade;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum PlanterProvider implements IServerDataProvider<BlockAccessor> {
  INSTANCE;

  static final Identifier ID = Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "planter");

  @Override
  public void appendServerData(CompoundTag data, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof PlanterBlockEntity blockEntity) {
      data.merge(JadeInfo.planter(blockEntity));
    }
  }

  @Override
  public Identifier getUid() {
    return ID;
  }
}
