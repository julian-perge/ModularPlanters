package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class Ae2AbsentTests {
  private static final Identifier ME =
      Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "me");

  private Ae2AbsentTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put(
        "me_block_is_not_registered", new TestCase(20, Ae2AbsentTests::meBlockIsNotRegistered));
    into.put("me_recipe_is_skipped", new TestCase(20, Ae2AbsentTests::meRecipeIsSkipped));
  }

  private static void meBlockIsNotRegistered(GameTestHelper helper) {
    helper.assertTrue(!BuiltInRegistries.BLOCK.containsKey(ME), "the ME block is registered");
    helper.assertTrue(!BuiltInRegistries.ITEM.containsKey(ME), "the ME block item is registered");
    helper.succeed();
  }

  private static void meRecipeIsSkipped(GameTestHelper helper) {
    var recipes = helper.getLevel().recipeAccess().recipeMap();
    helper.assertTrue(
        recipes.byKey(ResourceKey.create(Registries.RECIPE, MPBlocks.CORE.getId())) != null,
        "the core recipe loaded");
    helper.assertTrue(
        recipes.byKey(ResourceKey.create(Registries.RECIPE, ME)) == null,
        "the ME recipe loaded without AE2");
    helper.succeed();
  }
}
