package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class MPBlockTags extends BlockTagsProvider {
    public MPBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ModularPlanters.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(MPBlocks.CORE.get())
                .add(MPBlocks.FRAME.get())
                .add(MPBlocks.POWER.get())
                .add(MPBlocks.FERTILIZER.get())
                .add(MPBlocks.EXPORT.get())
                .add(MPBlocks.PLANTER.get());
    }
}
