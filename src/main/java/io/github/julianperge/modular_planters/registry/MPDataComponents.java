package io.github.julianperge.modular_planters.registry;

import com.mojang.serialization.Codec;
import io.github.julianperge.modular_planters.ModularPlanters;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPDataComponents {
  public static final DeferredRegister.DataComponents COMPONENTS =
      DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModularPlanters.MOD_ID);

  public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>>
      STORED_ENERGY =
          COMPONENTS.registerComponentType(
              "stored_energy",
              builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

  private MPDataComponents() {}
}
