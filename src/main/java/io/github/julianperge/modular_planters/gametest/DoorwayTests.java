package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.multiblock.FertilizerPool;
import io.github.julianperge.modular_planters.multiblock.OutputSink;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class DoorwayTests {
  private static final BlockPos CORE = new BlockPos(1, 1, 1);

  private DoorwayTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put(
        "core_exposes_nothing_unformed",
        new TestCase(20, DoorwayTests::coreExposesNothingUnformed));
    into.put(
        "core_energy_goes_to_power_blocks_in_order",
        new TestCase(20, DoorwayTests::coreEnergyGoesToPowerBlocksInOrder));
    into.put(
        "core_accepts_only_fertilizer", new TestCase(20, DoorwayTests::coreAcceptsOnlyFertilizer));
    into.put("core_rejects_wheat", new TestCase(20, DoorwayTests::coreRejectsWheat));
    into.put("doorway_handler_is_cached", new TestCase(20, DoorwayTests::doorwayHandlerIsCached));
    into.put(
        "doorway_skips_a_removed_block", new TestCase(20, DoorwayTests::doorwaySkipsARemovedBlock));
    into.put(
        "core_extract_comes_from_export_blocks",
        new TestCase(20, DoorwayTests::coreExtractComesFromExportBlocks));
    into.put(
        "core_capabilities_follow_the_structure",
        new TestCase(20, DoorwayTests::coreCapabilitiesFollowTheStructure));
    into.put(
        "output_sink_returns_the_remainder",
        new TestCase(20, DoorwayTests::outputSinkReturnsTheRemainder));
    into.put(
        "output_sink_room_counts_stacks_together",
        new TestCase(20, DoorwayTests::outputSinkRoomCountsStacksTogether));
    into.put(
        "fertilizer_consume_takes_nothing_when_short",
        new TestCase(20, DoorwayTests::fertilizerConsumeTakesNothingWhenShort));
    into.put("pools_skip_a_removed_block", new TestCase(20, DoorwayTests::poolsSkipARemovedBlock));
    into.put(
        "pools_take_a_replaced_block", new TestCase(20, DoorwayTests::poolsTakeAReplacedBlock));
  }

  private static void placeFormedLine(GameTestHelper helper, int powerBlocks) {
    helper.setBlock(CORE, MPBlocks.CORE.get());
    int x = 1;
    for (int i = 0; i < powerBlocks; i++) {
      helper.setBlock(CORE.east(x++), MPBlocks.POWER.get());
    }
    helper.setBlock(CORE.east(x++), MPBlocks.FERTILIZER.get());
    helper.setBlock(CORE.east(x++), MPBlocks.EXPORT.get());
    helper.setBlock(CORE.east(x), MPBlocks.PLANTER.get());
  }

  private static BlockPos fertilizerPos(int powerBlocks) {
    return CORE.east(powerBlocks + 1);
  }

  private static BlockPos exportPos(int powerBlocks) {
    return CORE.east(powerBlocks + 2);
  }

  private static void coreExposesNothingUnformed(GameTestHelper helper) {
    helper.setBlock(CORE, MPBlocks.CORE.get());
    helper.runAfterDelay(
        1,
        () -> {
          helper.assertTrue(
              helper.getCapability(Capabilities.Energy.BLOCK, CORE, null) == null,
              "energy capability");
          helper.assertTrue(
              helper.getCapability(Capabilities.Item.BLOCK, CORE, null) == null, "item capability");
          helper.succeed();
        });
  }

  private static void coreEnergyGoesToPowerBlocksInOrder(GameTestHelper helper) {
    placeFormedLine(helper, 2);
    helper.runAfterDelay(
        1,
        () -> {
          EnergyHandler energy = helper.requireCapability(Capabilities.Energy.BLOCK, CORE, null);
          try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(energy.insert(5_000_000, tx), 5_000_000, "inserted energy");
            tx.commit();
          }
          helper.assertValueEqual(
              helper.getBlockEntity(CORE.east(1), PowerBlockEntity.class).stored(),
              4_000_000,
              "first power block");
          helper.assertValueEqual(
              helper.getBlockEntity(CORE.east(2), PowerBlockEntity.class).stored(),
              1_000_000,
              "second power block");
          helper.succeed();
        });
  }

  private static void coreAcceptsOnlyFertilizer(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          ResourceHandler<ItemResource> items =
              helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
          try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(
                items.insert(ItemResource.of(Items.DIRT), 64, tx), 0, "inserted dirt");
            helper.assertValueEqual(
                items.insert(ItemResource.of(Items.BONE_MEAL), 64, tx), 64, "inserted bone meal");
            tx.commit();
          }
          FertilizerBlockEntity fertilizer =
              helper.getBlockEntity(fertilizerPos(1), FertilizerBlockEntity.class);
          try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(
                fertilizer.storage().extract(ItemResource.of(Items.BONE_MEAL), 64, tx),
                64,
                "bone meal in the fertilizer block");
          }
          helper.succeed();
        });
  }

  private static void coreRejectsWheat(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          ResourceHandler<ItemResource> items =
              helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
          try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(
                items.insert(ItemResource.of(Items.WHEAT), 64, tx), 0, "inserted wheat");
          }
          helper.succeed();
        });
  }

  private static void doorwayHandlerIsCached(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          ResourceHandler<ItemResource> first =
              helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
          helper.assertTrue(
              helper.requireCapability(Capabilities.Item.BLOCK, CORE, null) == first,
              "A second query should return the same handler");
          helper.setBlock(CORE.west(), MPBlocks.FRAME.get());
          helper.runAfterDelay(
              1,
              () -> {
                helper.assertTrue(
                    helper.requireCapability(Capabilities.Item.BLOCK, CORE, null) != first,
                    "A part change should replace the handler");
                helper.succeed();
              });
        });
  }

  private static void doorwaySkipsARemovedBlock(GameTestHelper helper) {
    BlockPos removed = CORE.west();
    placeFormedLine(helper, 1);
    helper.setBlock(removed, MPBlocks.FERTILIZER.get());
    helper.runAfterDelay(
        1,
        () -> {
          ResourceHandler<ItemResource> items =
              helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
          int before = items.size();
          helper.getLevel().destroyBlock(helper.absolutePos(removed), true);
          helper.assertValueEqual(
              items.size(), before - FertilizerBlockEntity.SLOTS, "slots after the break");
          for (int slot = 0; slot < items.size(); slot++) {
            helper.assertTrue(
                items.getResource(slot).isEmpty(), "Slot " + slot + " should be empty");
          }
          helper.succeed();
        });
  }

  private static void coreExtractComesFromExportBlocks(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          try (Transaction tx = Transaction.openRoot()) {
            helper
                .getBlockEntity(exportPos(1), ExportBlockEntity.class)
                .storage()
                .insert(ItemResource.of(Items.WHEAT), 10, tx);
            tx.commit();
          }
          ResourceHandler<ItemResource> items =
              helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
          try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(
                items.extract(ItemResource.of(Items.WHEAT), 64, tx), 10, "extracted wheat");
          }
          helper.succeed();
        });
  }

  private static void coreCapabilitiesFollowTheStructure(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          helper.assertTrue(
              helper.getCapability(Capabilities.Energy.BLOCK, CORE, null) != null,
              "energy while formed");
          helper.getLevel().destroyBlock(helper.absolutePos(CORE.east()), true);
          helper.runAfterDelay(
              1,
              () -> {
                helper.assertTrue(
                    helper.getCapability(Capabilities.Energy.BLOCK, CORE, null) == null,
                    "energy after the break");
                helper.assertTrue(
                    helper.getCapability(Capabilities.Item.BLOCK, CORE, null) == null,
                    "items after the break");
                helper.succeed();
              });
        });
  }

  private static void fillExportExcept(GameTestHelper helper, int free) {
    ExportBlockEntity export = helper.getBlockEntity(exportPos(1), ExportBlockEntity.class);
    int fill = ExportBlockEntity.SLOTS * 64 - free;
    helper.assertValueEqual(
        ResourceHandlerUtil.insertStacking(
            export.storage(), ItemResource.of(Items.WHEAT), fill, null),
        fill,
        "pre-filled wheat");
  }

  private static void outputSinkReturnsTheRemainder(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          fillExportExcept(helper, 5);
          OutputSink outputs = helper.getBlockEntity(CORE, CoreBlockEntity.class).live().outputs();
          ItemStack left = outputs.insert(new ItemStack(Items.WHEAT, 10));
          helper.assertValueEqual(left.getCount(), 5, "remainder count");
          helper.assertTrue(left.is(Items.WHEAT), "remainder item");
          helper.succeed();
        });
  }

  private static void outputSinkRoomCountsStacksTogether(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          fillExportExcept(helper, 64);
          OutputSink outputs = helper.getBlockEntity(CORE, CoreBlockEntity.class).live().outputs();
          helper.assertTrue(
              outputs.hasRoomFor(List.of(new ItemStack(Items.WHEAT, 40))), "room for one stack");
          helper.assertTrue(
              !outputs.hasRoomFor(
                  List.of(new ItemStack(Items.WHEAT, 40), new ItemStack(Items.WHEAT, 40))),
              "no room for both stacks");
          helper.assertTrue(
              outputs.hasRoomFor(List.of(new ItemStack(Items.WHEAT, 40))),
              "nothing was kept by the simulation");
          helper.succeed();
        });
  }

  private static void fertilizerConsumeTakesNothingWhenShort(GameTestHelper helper) {
    placeFormedLine(helper, 1);
    helper.runAfterDelay(
        1,
        () -> {
          FertilizerBlockEntity block =
              helper.getBlockEntity(fertilizerPos(1), FertilizerBlockEntity.class);
          try (Transaction tx = Transaction.openRoot()) {
            block.storage().insert(ItemResource.of(Items.BONE_MEAL), 10, tx);
            tx.commit();
          }
          FertilizerPool fertilizer =
              helper.getBlockEntity(CORE, CoreBlockEntity.class).live().fertilizer();
          helper.assertTrue(!fertilizer.consume(11), "consume 11 of 10");
          helper.assertValueEqual(fertilizer.available(), 10, "bone meal left");
          helper.assertTrue(fertilizer.consume(10), "consume 10 of 10");
          helper.assertValueEqual(fertilizer.available(), 0, "bone meal after consuming");
          helper.succeed();
        });
  }

  private static void poolsSkipARemovedBlock(GameTestHelper helper) {
    BlockPos removed = CORE.west();
    placeFormedLine(helper, 1);
    helper.setBlock(removed, MPBlocks.POWER.get());
    helper.runAfterDelay(
        1,
        () -> {
          EnergyHandler energy = helper.requireCapability(Capabilities.Energy.BLOCK, CORE, null);
          PowerBlockEntity removedBlock = helper.getBlockEntity(removed, PowerBlockEntity.class);
          PowerBlockEntity kept = helper.getBlockEntity(CORE.east(), PowerBlockEntity.class);
          helper.getLevel().destroyBlock(helper.absolutePos(removed), true);
          try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(energy.insert(5_000_000, tx), 4_000_000, "inserted energy");
            tx.commit();
          }
          helper.assertValueEqual(removedBlock.stored(), 0, "removed block");
          helper.assertValueEqual(kept.stored(), 4_000_000, "remaining block");
          helper.succeed();
        });
  }

  private static void poolsTakeAReplacedBlock(GameTestHelper helper) {
    BlockPos replaced = CORE.west();
    placeFormedLine(helper, 1);
    helper.setBlock(replaced, MPBlocks.POWER.get());
    helper.runAfterDelay(
        1,
        () -> {
          helper.getLevel().destroyBlock(helper.absolutePos(replaced), true);
          helper.setBlock(replaced, MPBlocks.POWER.get());
          EnergyHandler stale = helper.requireCapability(Capabilities.Energy.BLOCK, CORE, null);
          try (Transaction tx = Transaction.openRoot()) {
            stale.insert(1000, tx);
          }
          helper.runAfterDelay(
              2,
              () -> {
                EnergyHandler energy =
                    helper.requireCapability(Capabilities.Energy.BLOCK, CORE, null);
                try (Transaction tx = Transaction.openRoot()) {
                  helper.assertValueEqual(
                      energy.insert(5_000_000, tx), 5_000_000, "inserted energy");
                  tx.commit();
                }
                helper.assertValueEqual(
                    helper.getBlockEntity(replaced, PowerBlockEntity.class).stored(),
                    4_000_000,
                    "replaced block");
                helper.assertValueEqual(
                    helper.getBlockEntity(CORE.east(), PowerBlockEntity.class).stored(),
                    1_000_000,
                    "other block");
                helper.succeed();
              });
        });
  }
}
