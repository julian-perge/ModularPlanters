package io.github.julianperge.modular_planters.gametest;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.recipe.ATERecipeTypes;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.logic.AteNumbers;
import io.github.julianperge.modular_planters.logic.ModuleKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class AteBridgeTests {
    private AteBridgeTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("reads_live_ate_config", new TestCase(20, AteBridgeTests::readsLiveAteConfig));
        into.put("finds_wheat_recipe_and_soils", new TestCase(20, AteBridgeTests::findsWheatRecipeAndSoils));
        into.put("recipe_memo_refreshes_on_revision_change",
                new TestCase(20, AteBridgeTests::recipeMemoRefreshesOnRevisionChange));
        into.put("numbers_match_ate_config", new TestCase(20, AteBridgeTests::numbersMatchAteConfig));
        into.put("roll_drops_is_seeded", new TestCase(20, AteBridgeTests::rollDropsIsSeeded));
    }

    private static ItemStack ateItem(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("agritechevolved", path)));
    }

    private static void readsLiveAteConfig(GameTestHelper helper) {
        double saved = Config.CLOCHE_YIELD_MULTIPLIER.get();
        try {
            Config.CLOCHE_YIELD_MULTIPLIER.set(1.3);
            helper.assertValueEqual(AteBridge.numbers().clocheYield(), 1.3, "cloche yield");
        } finally {
            Config.CLOCHE_YIELD_MULTIPLIER.set(saved);
        }
        helper.succeed();
    }

    private static void findsWheatRecipeAndSoils(GameTestHelper helper) {
        AteBridge.PlantRecipe recipe = AteBridge.recipe(helper.getLevel(), new ItemStack(Items.WHEAT_SEEDS)).orElse(null);
        helper.assertTrue(recipe != null, "No recipe for wheat seeds");
        helper.assertValueEqual(recipe.drops().size(), 2, "wheat drop entries");
        ItemStack infused = ateItem("infused_farmland");
        helper.assertTrue(recipe.soilMatches().test(infused), "Infused farmland should be a wheat soil");
        helper.assertFalse(recipe.soilMatches().test(new ItemStack(Items.DIRT)), "Dirt should not be a wheat soil");
        helper.assertTrue(AteBridge.isSoilForAnyRecipe(helper.getLevel(), infused), "Infused farmland should be a soil");
        helper.assertFalse(AteBridge.isSoilForAnyRecipe(helper.getLevel(), new ItemStack(Items.DIAMOND)),
                "Diamond should not be a soil");
        helper.assertValueEqual(AteBridge.soilModifier(infused), 1.5f, "infused farmland modifier");
        helper.assertValueEqual(AteBridge.soilModifier(new ItemStack(Items.DIAMOND)), 1.0f, "default soil modifier");
        var bonemeal = AteBridge.fertilizer(ItemResource.of(Items.BONE_MEAL));
        helper.assertTrue(bonemeal != null, "No bone meal fertilizer data");
        helper.assertValueEqual(bonemeal.speedMultiplier(), 1.2f, "bone meal speed");
        helper.assertValueEqual(bonemeal.yieldMultiplier(), 1.2f, "bone meal yield");
        helper.assertValueEqual(AteBridge.moduleKind(ateItem("sm_mk3")), ModuleKind.SPEED_MK3, "sm_mk3 kind");
        helper.assertTrue(AteBridge.isModule(ateItem("ym_mk1")), "ym_mk1 is a module");
        helper.assertFalse(AteBridge.isModule(new ItemStack(Items.DIRT)), "dirt is not a module");
        helper.succeed();
    }

    private static void recipeMemoRefreshesOnRevisionChange(GameTestHelper helper) {
        AteBridge.RecipeMemo memo = new AteBridge.RecipeMemo();
        AteBridge.PlantRecipe first = memo.get(helper.getLevel(), new ItemStack(Items.WHEAT_SEEDS)).orElse(null);
        helper.assertTrue(first != null, "No recipe for wheat seeds");
        AteBridge.PlantRecipe again = memo.get(helper.getLevel(), new ItemStack(Items.WHEAT_SEEDS, 5)).orElse(null);
        helper.assertTrue(again == first, "The same seed item and revision should reuse the recipe");
        AgritechEvolved.RECIPE_REVISION++;
        AteBridge.PlantRecipe after = memo.get(helper.getLevel(), new ItemStack(Items.WHEAT_SEEDS)).orElse(null);
        helper.assertTrue(after != null && after != first, "A new revision should look the recipe up again");
        helper.assertValueEqual(after.drops(), first.drops(), "wheat drops after the new revision");
        helper.succeed();
    }

    private static void numbersMatchAteConfig(GameTestHelper helper) {
        AteNumbers numbers = AteBridge.numbers();
        helper.assertValueEqual(numbers.advancedBaseProcessingTime(), Config.getAdvancedPlanterBaseProcessingTime(),
                "advanced base processing time");
        helper.assertValueEqual(numbers.basePowerConsumption(), Config.getPlanterBasePowerConsumption(),
                "base power consumption");
        helper.assertValueEqual(numbers.speedMk1(), Config.getSpeedModuleMk1Multiplier(), "speed mk1");
        helper.assertValueEqual(numbers.speedMk2(), Config.getSpeedModuleMk2Multiplier(), "speed mk2");
        helper.assertValueEqual(numbers.speedMk3(), Config.getSpeedModuleMk3Multiplier(), "speed mk3");
        helper.assertValueEqual(numbers.powerMk1(), Config.getSpeedModuleMk1PowerMultiplier(), "power mk1");
        helper.assertValueEqual(numbers.powerMk2(), Config.getSpeedModuleMk2PowerMultiplier(), "power mk2");
        helper.assertValueEqual(numbers.powerMk3(), Config.getSpeedModuleMk3PowerMultiplier(), "power mk3");
        helper.assertValueEqual(numbers.yieldMk1(), Config.getYieldModuleMk1Multiplier(), "yield mk1");
        helper.assertValueEqual(numbers.yieldMk2(), Config.getYieldModuleMk2Multiplier(), "yield mk2");
        helper.assertValueEqual(numbers.yieldMk3(), Config.getYieldModuleMk3Multiplier(), "yield mk3");
        helper.assertValueEqual(numbers.yieldPenaltyMk1(), Config.getYieldModuleMk1SpeedPenalty(), "yield penalty mk1");
        helper.assertValueEqual(numbers.yieldPenaltyMk2(), Config.getYieldModuleMk2SpeedPenalty(), "yield penalty mk2");
        helper.assertValueEqual(numbers.yieldPenaltyMk3(), Config.getYieldModuleMk3SpeedPenalty(), "yield penalty mk3");
        helper.assertValueEqual(numbers.clocheSpeed(), Config.getClocheSpeedMultiplier(), "cloche speed");
        helper.assertValueEqual(numbers.clocheYield(), Config.getClocheYieldMultiplier(), "cloche yield");
        helper.succeed();
    }

    // No seed in ATE's recipes has both a crop and a tree recipe, so the crop-first order can't be seen here, and this
    // checks instead that a crop seed gets its crop recipe's drops and a sapling gets its tree recipe's drops.
    private static void rollDropsIsSeeded(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AteBridge.PlantRecipe wheat = AteBridge.recipe(level, new ItemStack(Items.WHEAT_SEEDS)).orElse(null);
        helper.assertTrue(wheat != null, "No recipe for wheat seeds");
        List<ItemStack> first = AteBridge.rollDrops(wheat.drops(), RandomSource.create(42));
        List<ItemStack> second = AteBridge.rollDrops(wheat.drops(), RandomSource.create(42));
        helper.assertValueEqual(second.size(), first.size(), "rolled stacks");
        for (int i = 0; i < first.size(); i++) {
            helper.assertTrue(ItemStack.matches(first.get(i), second.get(i)), "Rolled stack " + i + " should match");
        }
        var crop = level.recipeAccess().getRecipeFor(ATERecipeTypes.CROP_TYPE.get(),
                new SingleRecipeInput(new ItemStack(Items.WHEAT_SEEDS)), level).orElseThrow().value();
        helper.assertValueEqual(wheat.drops(), crop.getDrops(), "wheat drops come from the crop recipe");
        AteBridge.PlantRecipe oak = AteBridge.recipe(level, new ItemStack(Items.OAK_SAPLING)).orElse(null);
        helper.assertTrue(oak != null, "No recipe for oak saplings");
        var tree = level.recipeAccess().getRecipeFor(ATERecipeTypes.TREE_TYPE.get(),
                new SingleRecipeInput(new ItemStack(Items.OAK_SAPLING)), level).orElseThrow().value();
        helper.assertValueEqual(oak.drops(), tree.getDrops(), "oak drops come from the tree recipe");
        helper.succeed();
    }
}
