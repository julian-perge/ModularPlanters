package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.multiblock.CoreFinder;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class StructureTests {
  private static final BlockPos CORE = new BlockPos(1, 1, 1);

  private StructureTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put("forms_with_required_parts", new TestCase(20, StructureTests::formsWithRequiredParts));
    into.put("breaking_a_part_unforms", new TestCase(20, StructureTests::breakingAPartUnforms));
    into.put(
        "part_cap_comes_from_config", new TestCase(20, StructureTests::partCapComesFromConfig));
    into.put("two_cores_neither_forms", new TestCase(20, StructureTests::twoCoresNeitherForms));
    into.put(
        "placing_the_missing_part_forms",
        new TestCase(20, StructureTests::placingTheMissingPartForms));
    into.put(
        "core_finder_counts_only_parts",
        new TestCase(20, StructureTests::coreFinderCountsOnlyParts));
  }

  private static void placeFormedLine(GameTestHelper helper) {
    helper.setBlock(CORE, MPBlocks.CORE.get());
    helper.setBlock(CORE.east(), MPBlocks.POWER.get());
    helper.setBlock(CORE.east(2), MPBlocks.EXPORT.get());
    helper.setBlock(CORE.east(3), MPBlocks.PLANTER.get());
  }

  private static void formsWithRequiredParts(GameTestHelper helper) {
    placeFormedLine(helper);
    helper.runAfterDelay(
        1,
        () -> {
          CoreBlockEntity core = helper.getBlockEntity(CORE, CoreBlockEntity.class);
          helper.assertTrue(core.multiblock() != null, "multiblock should be formed");
          helper.assertValueEqual(core.multiblock().power().size(), 1, "power blocks");
          helper.assertValueEqual(
              core.multiblock().power().getFirst(),
              helper.absolutePos(CORE.east()),
              "power position");
          helper.succeed();
        });
  }

  private static void breakingAPartUnforms(GameTestHelper helper) {
    placeFormedLine(helper);
    helper.runAfterDelay(
        1,
        () -> {
          helper.assertTrue(
              helper.getBlockEntity(CORE, CoreBlockEntity.class).multiblock() != null,
              "formed before the break");
          helper.getLevel().destroyBlock(helper.absolutePos(CORE.east()), true);
          helper.runAfterDelay(
              1,
              () -> {
                CoreBlockEntity core = helper.getBlockEntity(CORE, CoreBlockEntity.class);
                helper.assertTrue(core.multiblock() == null, "multiblock should be gone");
                helper.assertValueEqual(
                    core.lastScan().problem(), Problem.MISSING_POWER, "problem");
                helper.succeed();
              });
        });
  }

  private static void partCapComesFromConfig(GameTestHelper helper) {
    BlockPos corePos = new BlockPos(0, 1, 0);
    helper.setBlock(corePos, MPBlocks.CORE.get());
    for (int x = 1; x <= 5; x++) {
      helper.setBlock(new BlockPos(x, 1, 0), MPBlocks.FRAME.get());
    }
    CoreBlockEntity core = helper.getBlockEntity(corePos, CoreBlockEntity.class);
    int saved = MPConfig.PART_CAP.get();
    try {
      MPConfig.PART_CAP.set(4);
      core.markDirty();
      CoreBlockEntity.serverTick(
          helper.getLevel(), helper.absolutePos(corePos), helper.getBlockState(corePos), core);
      helper.assertValueEqual(core.lastScan().problem(), Problem.TOO_MANY_PARTS, "problem");
    } finally {
      MPConfig.PART_CAP.set(saved);
    }
    helper.succeed();
  }

  private static void coreFinderCountsOnlyParts(GameTestHelper helper) {
    BlockPos corePos = new BlockPos(0, 1, 0);
    helper.setBlock(corePos, MPBlocks.CORE.get());
    for (int y = 1; y <= 2; y++) {
      for (int x = 0; x <= 8; x++) {
        for (int z = 0; z <= 8; z++) {
          BlockPos pos = new BlockPos(x, y, z);
          if (!pos.equals(corePos)) {
            helper.setBlock(pos, MPBlocks.FRAME.get());
          }
        }
      }
    }
    int marked =
        CoreFinder.notifyCores(helper.getLevel(), helper.absolutePos(new BlockPos(8, 2, 8)));
    helper.assertValueEqual(marked, 1, "cores marked dirty");
    helper.succeed();
  }

  private static void twoCoresNeitherForms(GameTestHelper helper) {
    BlockPos other = CORE.east(2);
    helper.setBlock(CORE, MPBlocks.CORE.get());
    helper.setBlock(CORE.east(), MPBlocks.FRAME.get());
    helper.setBlock(other, MPBlocks.CORE.get());
    helper.runAfterDelay(
        1,
        () -> {
          CoreBlockEntity first = helper.getBlockEntity(CORE, CoreBlockEntity.class);
          CoreBlockEntity second = helper.getBlockEntity(other, CoreBlockEntity.class);
          helper.assertValueEqual(
              first.lastScan().problem(), Problem.SECOND_CORE, "first core problem");
          helper.assertValueEqual(
              first.lastScan().problemPos(),
              helper.absolutePos(other),
              "first core problem position");
          helper.assertValueEqual(
              second.lastScan().problem(), Problem.SECOND_CORE, "second core problem");
          helper.assertValueEqual(
              second.lastScan().problemPos(),
              helper.absolutePos(CORE),
              "second core problem position");
          helper.succeed();
        });
  }

  private static void placingTheMissingPartForms(GameTestHelper helper) {
    helper.setBlock(CORE, MPBlocks.CORE.get());
    helper.setBlock(CORE.east(), MPBlocks.POWER.get());
    helper.setBlock(CORE.east(2), MPBlocks.EXPORT.get());
    helper.runAfterDelay(
        1,
        () -> {
          helper.assertValueEqual(
              helper.getBlockEntity(CORE, CoreBlockEntity.class).lastScan().problem(),
              Problem.MISSING_PLANTER,
              "problem before the planter");
          helper.setBlock(CORE.east(3), MPBlocks.PLANTER.get());
          helper.runAfterDelay(
              1,
              () -> {
                helper.assertTrue(
                    helper.getBlockEntity(CORE, CoreBlockEntity.class).multiblock() != null,
                    "multiblock should be formed");
                helper.succeed();
              });
        });
  }
}
