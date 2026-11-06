package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.multiblock.LiveMultiblock;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import io.github.julianperge.modular_planters.multiblock.Multiblock;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class LiveMultiblockTests {
  private static final BlockPos FERTILIZER = new BlockPos(1, 2, 1);

  private LiveMultiblockTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put(
        "paused_live_multiblock_exposes_nothing_but_its_planters",
        new TestCase(20, LiveMultiblockTests::pausedExposesNothingButItsPlanters));
    into.put(
        "running_live_multiblock_reports_the_stored_pools",
        new TestCase(20, LiveMultiblockTests::runningReportsTheStoredPools));
    into.put(
        "running_live_multiblock_owns_only_its_planters",
        new TestCase(20, LiveMultiblockTests::runningOwnsOnlyItsPlanters));
    into.put(
        "running_live_multiblock_asks_for_a_rebuild_when_a_power_block_is_removed",
        new TestCase(20, LiveMultiblockTests::removedPowerBlockAsksForARebuild));
    into.put(
        "running_live_multiblock_item_doorway_takes_fertilizer_and_gives_outputs",
        new TestCase(20, LiveMultiblockTests::itemDoorwayTakesFertilizerAndGivesOutputs));
    into.put(
        "core_rebuilds_its_live_multiblock_when_a_fertilizer_block_is_removed",
        new TestCase(100, LiveMultiblockTests::coreRebuildsWhenAFertilizerBlockIsRemoved));
  }

  private static void pausedExposesNothingButItsPlanters(GameTestHelper helper) {
    BlockPos firstPlanter = new BlockPos(1, 1, 4);
    BlockPos secondPlanter = new BlockPos(1, 1, 5);
    Multiblock multiblock =
        new Multiblock(
            List.of(new BlockPos(1, 1, 2)),
            List.of(),
            List.of(new BlockPos(1, 1, 3)),
            List.of(firstPlanter, secondPlanter),
            List.of(),
            null);

    LiveMultiblock paused = LiveMultiblock.paused(multiblock);

    helper.assertValueEqual(
        paused.totals(),
        new LiveMultiblock.PoolTotals(0, 0, 0, 0, MeStatus.NONE),
        "totals while paused");
    helper.assertTrue(paused.energy() == null, "no energy doorway while paused");
    helper.assertTrue(paused.items() == null, "no item doorway while paused");
    helper.assertTrue(
        !paused.ownsPlanter(firstPlanter), "a listed planter is still not owned while paused");
    helper.assertValueEqual(
        paused.planters(), List.of(firstPlanter, secondPlanter), "planters kept while paused");
    helper.succeed();
  }

  private static void runningReportsTheStoredPools(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    placeFertilizer(helper);
    helper.getBlockEntity(rig.power(), PowerBlockEntity.class).setStored(1_234_567);
    helper
        .getBlockEntity(FERTILIZER, FertilizerBlockEntity.class)
        .storage()
        .set(0, ItemResource.of(Items.BONE_MEAL), 10);
    ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
    for (int slot = 0; slot < ExportBlockEntity.SLOTS; slot++) {
      export.storage().set(slot, ItemResource.of(Items.WHEAT), 64);
    }

    LiveMultiblock running =
        LiveMultiblock.running(helper.getLevel(), multiblockOf(helper, rig), () -> {});

    helper.assertValueEqual(
        running.totals(),
        new LiveMultiblock.PoolTotals(1_234_567, 4_000_000, 10, 100, MeStatus.NONE),
        "totals while running");
    helper.succeed();
  }

  private static void runningOwnsOnlyItsPlanters(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 2);
    placeFertilizer(helper);

    LiveMultiblock running =
        LiveMultiblock.running(helper.getLevel(), multiblockOf(helper, rig), () -> {});

    helper.assertTrue(
        running.ownsPlanter(helper.absolutePos(rig.planters().get(0))), "first planter");
    helper.assertTrue(
        running.ownsPlanter(helper.absolutePos(rig.planters().get(1))), "second planter");
    helper.assertTrue(
        !running.ownsPlanter(helper.absolutePos(rig.power())), "a power block is not a planter");
    helper.assertTrue(
        !running.ownsPlanter(helper.absolutePos(new BlockPos(5, 1, 5))),
        "a position outside the multiblock");
    helper.succeed();
  }

  // Pipes keep the energy handler they were given, so it is fetched once, before the block goes.
  private static void removedPowerBlockAsksForARebuild(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    placeFertilizer(helper);
    helper.getBlockEntity(rig.power(), PowerBlockEntity.class).setStored(0);
    int[] rebuildRequests = {0};
    LiveMultiblock running =
        LiveMultiblock.running(
            helper.getLevel(), multiblockOf(helper, rig), () -> rebuildRequests[0]++);
    EnergyHandler energy = running.energy();
    helper.assertTrue(energy != null, "energy doorway while running");
    helper.assertValueEqual(insert(energy, 100), 100, "energy taken while the block is there");

    helper.getLevel().destroyBlock(helper.absolutePos(rig.power()), false);

    helper.assertValueEqual(insert(energy, 100), 0, "energy taken after the block is gone");
    helper.assertValueEqual(running.totals().capacity(), 0L, "capacity after the block is gone");
    helper.assertTrue(rebuildRequests[0] > 0, "the Core was asked to rebuild");
    helper.succeed();
  }

  private static void itemDoorwayTakesFertilizerAndGivesOutputs(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    placeFertilizer(helper);
    helper
        .getBlockEntity(rig.export(), ExportBlockEntity.class)
        .storage()
        .set(0, ItemResource.of(Items.WHEAT), 20);
    LiveMultiblock running =
        LiveMultiblock.running(helper.getLevel(), multiblockOf(helper, rig), () -> {});
    ResourceHandler<ItemResource> items = running.items();
    helper.assertTrue(items != null, "item doorway while running");

    helper.assertValueEqual(insert(items, Items.BONE_MEAL, 5), 5, "bone meal taken");
    helper.assertValueEqual(insert(items, Items.WHEAT, 5), 0, "wheat refused");
    helper.assertValueEqual(extract(items, Items.WHEAT, 20), 20, "wheat given");
    helper.assertValueEqual(running.totals().fertilizer(), 5, "bone meal stored");
    helper.succeed();
  }

  private static void coreRebuildsWhenAFertilizerBlockIsRemoved(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    BlockPos secondFertilizer = new BlockPos(2, 2, 1);
    placeFertilizer(helper);
    helper.setBlock(secondFertilizer, MPBlocks.FERTILIZER.get());
    for (BlockPos pos : List.of(FERTILIZER, secondFertilizer)) {
      helper
          .getBlockEntity(pos, FertilizerBlockEntity.class)
          .storage()
          .set(0, ItemResource.of(Items.BONE_MEAL), 10);
    }
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    LiveMultiblock[] before = {null};

    helper
        .startSequence()
        .thenWaitUntil(
            () ->
                helper.assertValueEqual(
                    core.live() != null ? core.totals().fertilizer() : -1,
                    20,
                    "both blocks pooled"))
        .thenExecute(
            () -> {
              before[0] = core.live();
              helper.getLevel().destroyBlock(helper.absolutePos(secondFertilizer), false);
            })
        .thenWaitUntil(
            () ->
                helper.assertTrue(core.live() != before[0], "the Core built a new live multiblock"))
        .thenExecute(
            () -> {
              helper.assertValueEqual(core.totals().fertilizer(), 10, "bone meal left");
              ResourceHandler<ItemResource> items = core.items();
              helper.assertTrue(items != null, "item doorway after the rebuild");
              helper.assertValueEqual(insert(items, Items.BONE_MEAL, 5), 5, "bone meal taken");
              helper.assertValueEqual(core.totals().fertilizer(), 15, "bone meal stored");
            })
        .thenSucceed();
  }

  private static int insert(ResourceHandler<ItemResource> items, Item item, int amount) {
    try (Transaction tx = Transaction.openRoot()) {
      int inserted = items.insert(ItemResource.of(item), amount, tx);
      tx.commit();
      return inserted;
    }
  }

  private static int extract(ResourceHandler<ItemResource> items, Item item, int amount) {
    try (Transaction tx = Transaction.openRoot()) {
      int extracted = items.extract(ItemResource.of(item), amount, tx);
      tx.commit();
      return extracted;
    }
  }

  private static int insert(EnergyHandler energy, int amount) {
    try (Transaction tx = Transaction.openRoot()) {
      int inserted = energy.insert(amount, tx);
      tx.commit();
      return inserted;
    }
  }

  private static void placeFertilizer(GameTestHelper helper) {
    helper.setBlock(FERTILIZER, MPBlocks.FERTILIZER.get());
  }

  // The positions are absolute, like the ones a scan gives the Core, and the Core of the rig scans
  // the same blocks on its own, so the live multiblock under test is never the Core's own.
  private static Multiblock multiblockOf(GameTestHelper helper, Fixtures.Rig rig) {
    return new Multiblock(
        List.of(helper.absolutePos(rig.power())),
        List.of(helper.absolutePos(FERTILIZER)),
        List.of(helper.absolutePos(rig.export())),
        rig.planters().stream().map(helper::absolutePos).toList(),
        List.of(),
        null);
  }
}
