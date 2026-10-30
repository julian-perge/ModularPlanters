package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import io.github.julianperge.modular_planters.block.PlanterBlock;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class MPModels extends ModelProvider {
    public MPModels(PackOutput output) {
        super(output, ModularPlanters.MOD_ID);
    }

    private static Material texture(String name) {
        return new Material(Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "block/" + name));
    }

    private static TexturedModel.Provider cubeTopBottom(String top, String side, String bottom) {
        return TexturedModel.CUBE_TOP_BOTTOM.updateTexture(mapping -> {
            mapping.put(TextureSlot.TOP, texture(top));
            mapping.put(TextureSlot.SIDE, texture(side));
            mapping.put(TextureSlot.BOTTOM, texture(bottom));
        });
    }

    private static TextureMapping planterTextures(String side) {
        return new TextureMapping()
                .put(TextureSlot.TOP, texture("planter_top"))
                .put(TextureSlot.SIDE, texture(side))
                .put(TextureSlot.BOTTOM, texture(side));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialBlock(MPBlocks.CORE.get(), cubeTopBottom("core_top", "core_side", "core_side"));
        blockModels.createTrivialBlock(MPBlocks.FRAME.get(), TexturedModel.CUBE.updateTexture(mapping -> mapping.put(TextureSlot.ALL, texture("frame_side"))));
        blockModels.createTrivialBlock(MPBlocks.POWER.get(), cubeTopBottom("power_top", "power_side", "power_bottom"));
        blockModels.createTrivialBlock(MPBlocks.FERTILIZER.get(), cubeTopBottom("fertilizer_top", "fertilizer_side", "fertilizer_side"));
        blockModels.createTrivialBlock(MPBlocks.EXPORT.get(), cubeTopBottom("export_top", "export_side", "export_side"));
        createPlanter(blockModels, MPBlocks.PLANTER.get());
        blockModels.createTrivialBlock(Ae2Integration.ME_BLOCK.get(), cubeTopBottom("frame_side", "me_side", "frame_side"));
    }

    private static void createPlanter(BlockModelGenerators blockModels, Block planter) {
        Identifier off = ModelTemplates.CUBE_BOTTOM_TOP.create(planter, planterTextures("planter_side"), blockModels.modelOutput);
        Identifier on = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(planter, "_on", planterTextures("planter_side_on"), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(planter)
                .with(BlockModelGenerators.createBooleanModelDispatch(PlanterBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(on), BlockModelGenerators.plainVariant(off))));
    }
}
