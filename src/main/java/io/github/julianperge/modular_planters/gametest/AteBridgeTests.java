package io.github.julianperge.modular_planters.gametest;

import com.misterd.agritechevolved.Config;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.logic.ModuleKind;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class AteBridgeTests {
    private AteBridgeTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("reads_live_ate_config", new TestCase(20, AteBridgeTests::readsLiveAteConfig));
        into.put("finds_wheat_recipe_and_soils", new TestCase(20, AteBridgeTests::findsWheatRecipeAndSoils));
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
}
