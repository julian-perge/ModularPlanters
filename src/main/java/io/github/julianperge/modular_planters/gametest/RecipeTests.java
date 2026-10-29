package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class RecipeTests {
    private static final List<DeferredBlock<? extends Block>> PARTS = List.of(
            MPBlocks.CORE, MPBlocks.FRAME, MPBlocks.POWER, MPBlocks.FERTILIZER, MPBlocks.EXPORT, MPBlocks.PLANTER);

    private RecipeTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("recipes_load", new TestCase(20, RecipeTests::recipesLoad));
        into.put("parts_drop_themselves", new TestCase(20, RecipeTests::partsDropThemselves));
    }

    private static void recipesLoad(GameTestHelper helper) {
        for (DeferredBlock<? extends Block> part : PARTS) {
            RecipeHolder<?> recipe = helper.getLevel().recipeAccess().recipeMap()
                    .byKey(ResourceKey.create(Registries.RECIPE, part.getId()));
            helper.assertTrue(recipe != null && recipe.value() instanceof ShapedRecipe,
                    "No shaped recipe " + part.getId());
            helper.assertValueEqual(((ShapedRecipe) recipe.value()).assemble(CraftingInput.EMPTY).getItem(),
                    part.asItem(), "result of " + part.getId());
        }
        helper.succeed();
    }

    private static void partsDropThemselves(GameTestHelper helper) {
        for (int i = 0; i < PARTS.size(); i++) {
            DeferredBlock<? extends Block> part = PARTS.get(i);
            BlockPos pos = new BlockPos(1 + i, 1, 1);
            helper.setBlock(pos, part.get());
            helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
            helper.assertItemEntityPresent(part.asItem(), pos, 2.0);
        }
        helper.succeed();
    }
}
