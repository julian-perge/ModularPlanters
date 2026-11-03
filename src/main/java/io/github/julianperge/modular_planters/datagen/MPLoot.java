package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPDataComponents;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

public class MPLoot extends BlockLootSubProvider {
  public MPLoot(HolderLookup.Provider registries) {
    super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
  }

  @Override
  protected void generate() {
    dropSelf(MPBlocks.CORE.get());
    dropSelf(MPBlocks.FRAME.get());
    add(
        MPBlocks.POWER.get(),
        block ->
            LootTable.lootTable()
                .withPool(
                    applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(
                                LootItem.lootTableItem(block)
                                    .apply(
                                        CopyComponentsFunction.copyComponentsFromBlockEntity(
                                                LootContextParams.BLOCK_ENTITY)
                                            .include(MPDataComponents.STORED_ENERGY.get()))))));
    dropSelf(MPBlocks.FERTILIZER.get());
    dropSelf(MPBlocks.EXPORT.get());
    dropSelf(MPBlocks.PLANTER.get());
    add(
        Ae2Integration.ME_BLOCK.get(),
        block -> createSingleItemTable(block).withCondition(new ModLoadedCondition("ae2")));
  }

  @Override
  protected Iterable<Block> getKnownBlocks() {
    return MPBlocks.BLOCKS.getEntries().stream().map(Holder::value).toList();
  }
}
