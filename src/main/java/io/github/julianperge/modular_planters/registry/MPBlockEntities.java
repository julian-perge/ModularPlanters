package io.github.julianperge.modular_planters.registry;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.multiblock.DoorwayHandlers;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPBlockEntities {
  public static final DeferredRegister<BlockEntityType<?>> TYPES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModularPlanters.MOD_ID);

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoreBlockEntity>> CORE =
      TYPES.register(
          "core", () -> new BlockEntityType<>(CoreBlockEntity::new, MPBlocks.CORE.get()));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerBlockEntity>> POWER =
      TYPES.register(
          "power", () -> new BlockEntityType<>(PowerBlockEntity::new, MPBlocks.POWER.get()));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FertilizerBlockEntity>>
      FERTILIZER =
          TYPES.register(
              "fertilizer",
              () -> new BlockEntityType<>(FertilizerBlockEntity::new, MPBlocks.FERTILIZER.get()));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExportBlockEntity>>
      EXPORT =
          TYPES.register(
              "export", () -> new BlockEntityType<>(ExportBlockEntity::new, MPBlocks.EXPORT.get()));
  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlanterBlockEntity>>
      PLANTER =
          TYPES.register(
              "planter",
              () -> new BlockEntityType<>(PlanterBlockEntity::new, MPBlocks.PLANTER.get()));

  private MPBlockEntities() {}

  public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    event.registerBlockEntity(
        Capabilities.Energy.BLOCK, CORE.get(), (be, side) -> DoorwayHandlers.energy(be));
    event.registerBlockEntity(
        Capabilities.Item.BLOCK, CORE.get(), (be, side) -> DoorwayHandlers.items(be));
    event.registerBlockEntity(
        Capabilities.Energy.BLOCK, POWER.get(), (be, side) -> be.externalHandler());
    event.registerBlockEntity(
        Capabilities.Item.BLOCK, FERTILIZER.get(), (be, side) -> be.externalHandler());
    event.registerBlockEntity(
        Capabilities.Item.BLOCK, EXPORT.get(), (be, side) -> be.externalHandler());
  }
}
