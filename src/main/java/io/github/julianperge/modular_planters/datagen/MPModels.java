package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;

public class MPModels extends ModelProvider {
    public MPModels(PackOutput output) {
        super(output, ModularPlanters.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialBlock(MPBlocks.CORE.get(), TexturedModel.CUBE_TOP_BOTTOM);
        blockModels.createTrivialBlock(MPBlocks.FRAME.get(), TexturedModel.CUBE_TOP_BOTTOM);
        blockModels.createTrivialBlock(MPBlocks.POWER.get(), TexturedModel.CUBE_TOP_BOTTOM);
        blockModels.createTrivialBlock(MPBlocks.FERTILIZER.get(), TexturedModel.CUBE_TOP_BOTTOM);
        blockModels.createTrivialBlock(MPBlocks.EXPORT.get(), TexturedModel.CUBE_TOP_BOTTOM);
        blockModels.createTrivialBlock(MPBlocks.PLANTER.get(), TexturedModel.CUBE_TOP_BOTTOM);
    }
}
