package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.jade.JadeInfo;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

public final class JadeTests {
  private JadeTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put(
        "formed_core_shows_its_part_count",
        new TestCase(20, JadeTests::formedCoreShowsItsPartCount));
    into.put(
        "core_shows_its_problem_until_formed",
        new TestCase(20, JadeTests::coreShowsItsProblemUntilFormed));
    into.put(
        "planter_shows_status_and_growing_seeds",
        new TestCase(20, JadeTests::planterShowsStatusAndGrowingSeeds));
    into.put("unformed_planter_shows_idle", new TestCase(20, JadeTests::unformedPlanterShowsIdle));
    into.put(
        "empty_server_data_shows_no_lines",
        new TestCase(20, JadeTests::emptyServerDataShowsNoLines));
    into.put(
        "jade_data_providers_are_server_only",
        new TestCase(20, JadeTests::jadeDataProvidersAreServerOnly));
  }

  // Jade logs a plugin that throws while registering and keeps running, so without this check a
  // broken plugin only shows up as empty tooltips in game.
  private static void jadeDataProvidersAreServerOnly(GameTestHelper helper) {
    if (ModList.get().isLoaded("jade")) {
      helper.assertTrue(
          JadeRegistration.dataProvidersAreServerOnly(),
          "no data provider is also a tooltip provider");
    }
    helper.succeed();
  }

  private static void formedCoreShowsItsPartCount(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    helper
        .startSequence()
        .thenWaitUntil(
            () -> helper.assertTrue(core.lastScan().formed(), "formed after the first scan"))
        .thenExecute(
            () ->
                helper.assertValueEqual(
                    JadeInfo.coreLines(JadeInfo.core(core)),
                    List.of(
                        Component.translatable(
                            "modular_planters.jade.formed",
                            core.lastScan().partCount(),
                            MPConfig.PART_CAP.get())),
                    "lines"))
        .thenSucceed();
  }

  private static void coreShowsItsProblemUntilFormed(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertTrue(core.lastScan().formed(), "formed before the break"))
        .thenExecute(() -> helper.getLevel().destroyBlock(helper.absolutePos(rig.power()), false))
        .thenWaitUntil(
            () ->
                helper.assertValueEqual(
                    core.lastScan().problem(), Problem.MISSING_POWER, "problem"))
        .thenExecute(
            () ->
                helper.assertValueEqual(
                    JadeInfo.coreLines(JadeInfo.core(core)),
                    List.of(
                        Component.translatable(
                            "modular_planters.jade.not_formed",
                            Component.translatable("modular_planters.problem.missing_power"))),
                    "lines"))
        .thenSucceed();
  }

  private static void planterShowsStatusAndGrowingSeeds(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
    Fixtures.plant(planter, 64, 32, 0);
    helper
        .startSequence()
        .thenWaitUntil(
            () -> helper.assertValueEqual(planter.status(), PlanterStatus.SHORT_ON_SOIL, "status"))
        .thenExecute(
            () ->
                helper.assertValueEqual(
                    JadeInfo.planterLines(JadeInfo.planter(planter)),
                    List.of(
                        Component.translatable("modular_planters.status.short_on_soil"),
                        Component.translatable("modular_planters.jade.growing", 32, 64)),
                    "lines"))
        .thenSucceed();
  }

  private static void unformedPlanterShowsIdle(GameTestHelper helper) {
    BlockPos pos = new BlockPos(1, 1, 1);
    helper.setBlock(pos, MPBlocks.PLANTER.get());
    PlanterBlockEntity planter = helper.getBlockEntity(pos, PlanterBlockEntity.class);
    planter.seeds().add(new ItemStack(Items.WHEAT_SEEDS), 10);
    helper.assertValueEqual(
        JadeInfo.planterLines(JadeInfo.planter(planter)),
        List.of(
            Component.translatable("modular_planters.status.idle"),
            Component.translatable("modular_planters.jade.growing", 0, 10)),
        "lines");
    helper.succeed();
  }

  private static void emptyServerDataShowsNoLines(GameTestHelper helper) {
    helper.assertValueEqual(JadeInfo.coreLines(new CompoundTag()), List.of(), "core");
    helper.assertValueEqual(JadeInfo.planterLines(new CompoundTag()), List.of(), "planter");
    helper.succeed();
  }
}
