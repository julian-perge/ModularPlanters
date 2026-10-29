package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class MPLoot extends BlockLootSubProvider {
    public MPLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(MPBlocks.CORE.get());
        dropSelf(MPBlocks.FRAME.get());
        dropSelf(MPBlocks.POWER.get());
        dropSelf(MPBlocks.FERTILIZER.get());
        dropSelf(MPBlocks.EXPORT.get());
        dropSelf(MPBlocks.PLANTER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MPBlocks.BLOCKS.getEntries().stream().map(Holder::value).toList();
    }
}
