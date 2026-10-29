package io.github.julianperge.modular_planters.ate;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.datamap.SoilModifierData;
import com.misterd.agritechevolved.recipe.ATERecipeTypes;
import com.misterd.agritechevolved.recipe.CropRecipe;
import com.misterd.agritechevolved.recipe.DropEntry;
import com.misterd.agritechevolved.recipe.TreeRecipe;
import com.misterd.agritechevolved.util.ATETags;
import io.github.julianperge.modular_planters.logic.AteNumbers;
import io.github.julianperge.modular_planters.logic.ModuleKind;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class AteBridge {
    private static Set<Item> cachedSoils = Set.of();
    private static int cachedSoilRevision = -1;

    private AteBridge() {
    }

    public record PlantRecipe(List<DropEntry> drops, Predicate<ItemStack> soilMatches) {
    }

    public static AteNumbers numbers() {
        return new AteNumbers(
                Config.getAdvancedPlanterBaseProcessingTime(), Config.getPlanterBasePowerConsumption(),
                Config.getSpeedModuleMk1Multiplier(), Config.getSpeedModuleMk2Multiplier(),
                Config.getSpeedModuleMk3Multiplier(),
                Config.getSpeedModuleMk1PowerMultiplier(), Config.getSpeedModuleMk2PowerMultiplier(),
                Config.getSpeedModuleMk3PowerMultiplier(),
                Config.getYieldModuleMk1Multiplier(), Config.getYieldModuleMk2Multiplier(),
                Config.getYieldModuleMk3Multiplier(),
                Config.getYieldModuleMk1SpeedPenalty(), Config.getYieldModuleMk2SpeedPenalty(),
                Config.getYieldModuleMk3SpeedPenalty(),
                Config.getClocheSpeedMultiplier(), Config.getClocheYieldMultiplier());
    }

    public static boolean isModule(ItemStack stack) {
        return stack.is(ATETags.Items.ATE_MODULES);
    }

    public static ModuleKind moduleKind(ItemStack stack) {
        return ModuleKind.fromItemId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    // This follows AdvancedPlanterBlockEntity.getSoilGrowthModifier, so a soil grows seeds at the same speed here as in a single planter.
    public static float soilModifier(ItemStack soil) {
        if (soil.isEmpty()) {
            return 1.0f;
        }
        SoilModifierData data = soil.getItem().builtInRegistryHolder().getData(ATEDataMaps.SOIL_MODIFIERS);
        return data != null ? data.growthModifier() : 1.0f;
    }

    public static @Nullable FertilizerData fertilizer(ItemResource resource) {
        return resource.typeHolder().getData(ATEDataMaps.FERTILIZERS);
    }

    // This follows AdvancedPlanterBlockEntity.refreshRecipeCacheIfNeeded, which tries crop recipes before tree recipes.
    public static Optional<PlantRecipe> recipe(ServerLevel level, ItemStack seed) {
        if (seed.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(seed);
        Optional<RecipeHolder<CropRecipe>> crop =
                level.recipeAccess().getRecipeFor(ATERecipeTypes.CROP_TYPE.get(), input, level);
        if (crop.isPresent()) {
            CropRecipe recipe = crop.get().value();
            return Optional.of(new PlantRecipe(recipe.getDrops(), recipe::matchesSoil));
        }
        Optional<RecipeHolder<TreeRecipe>> tree =
                level.recipeAccess().getRecipeFor(ATERecipeTypes.TREE_TYPE.get(), input, level);
        return tree.map(holder -> new PlantRecipe(holder.value().getDrops(), holder.value()::matchesSoil));
    }

    // This follows AdvancedPlanterBlockEntity.getValidSoils, whose soil set refreshes only when AgritechEvolved.RECIPE_REVISION changes.
    public static boolean isSoilForAnyRecipe(ServerLevel level, ItemStack soil) {
        if (soil.isEmpty()) {
            return false;
        }
        if (cachedSoilRevision != AgritechEvolved.RECIPE_REVISION) {
            Set<Item> soils = new HashSet<>();
            for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
                List<Ingredient> ingredients;
                if (holder.value() instanceof CropRecipe crop) {
                    ingredients = crop.getSoils();
                } else if (holder.value() instanceof TreeRecipe tree) {
                    ingredients = tree.getSoils();
                } else {
                    continue;
                }
                for (Ingredient ingredient : ingredients) {
                    ingredient.items().map(Holder::value).forEach(soils::add);
                }
            }
            cachedSoils = soils;
            cachedSoilRevision = AgritechEvolved.RECIPE_REVISION;
        }
        return cachedSoils.contains(soil.getItem());
    }

    // This follows AdvancedPlanterBlockEntity.getHarvestDrops, except that an entry whose rolled count is 0 is skipped.
    public static List<ItemStack> rollDrops(List<DropEntry> drops, RandomSource random) {
        List<ItemStack> rolled = new ArrayList<>();
        for (DropEntry entry : drops) {
            if (random.nextFloat() <= entry.chance()) {
                int count = entry.max() > entry.min()
                        ? entry.min() + random.nextInt(entry.max() - entry.min() + 1)
                        : entry.min();
                if (count > 0) {
                    rolled.add(new ItemStack(entry.item(), count));
                }
            }
        }
        return rolled;
    }
}
