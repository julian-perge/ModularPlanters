package io.github.julianperge.modular_planters.datagen;

import com.misterd.agritechevolved.block.ATEBlocks;
import com.misterd.agritechevolved.item.ATEItems;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class MPRecipes extends RecipeProvider {
    private MPRecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.MISC, MPBlocks.FRAME)
                .pattern("PPP")
                .pattern("PIP")
                .pattern("PPP")
                .define('P', ATEBlocks.ADVANCED_PLANTER)
                .define('I', Items.IRON_BLOCK)
                .unlockedBy("has_advanced_planter", has(ATEBlocks.ADVANCED_PLANTER))
                .save(output);

        shaped(RecipeCategory.MISC, MPBlocks.PLANTER)
                .pattern("FFF")
                .pattern("FIF")
                .pattern("FFF")
                .define('F', MPBlocks.FRAME)
                .define('I', ATEBlocks.INFUSED_FARMLAND)
                .unlockedBy("has_frame", has(MPBlocks.FRAME))
                .save(output);

        shaped(RecipeCategory.MISC, MPBlocks.CORE)
                .pattern("IGI")
                .pattern("GCG")
                .pattern("IDI")
                .define('C', ATEItems.CLOCHE.get())
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Items.IRON_BLOCK)
                .define('D', Items.DIAMOND_BLOCK)
                .unlockedBy("has_cloche_dome", has(ATEItems.CLOCHE.get()))
                .save(output);

        shaped(RecipeCategory.MISC, MPBlocks.POWER)
                .pattern("IRI")
                .pattern("RCR")
                .pattern("IRI")
                .define('C', ATEBlocks.CAPACITOR_TIER_3)
                .define('R', Items.REDSTONE_BLOCK)
                .define('I', Items.IRON_BLOCK)
                .unlockedBy("has_capacitor_tier3", has(ATEBlocks.CAPACITOR_TIER_3))
                .save(output);

        shaped(RecipeCategory.MISC, MPBlocks.FERTILIZER)
                .pattern("IBI")
                .pattern("BSB")
                .pattern("IBI")
                .define('S', ATEBlocks.FERT_SPREADER)
                .define('B', Items.BONE_BLOCK)
                .define('I', Items.IRON_BLOCK)
                .unlockedBy("has_fertilizer_spreader", has(ATEBlocks.FERT_SPREADER))
                .save(output);

        shaped(RecipeCategory.MISC, MPBlocks.EXPORT)
                .pattern("IHI")
                .pattern("HSH")
                .pattern("IHI")
                .define('S', ATEBlocks.SILO)
                .define('H', Items.HOPPER)
                .define('I', Items.IRON_BLOCK)
                .unlockedBy("has_silo", has(ATEBlocks.SILO))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new MPRecipes(registries, output);
        }

        @Override
        public String getName() {
            return "Modular Planters recipes";
        }
    }
}
