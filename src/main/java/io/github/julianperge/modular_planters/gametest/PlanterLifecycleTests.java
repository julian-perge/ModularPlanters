package io.github.julianperge.modular_planters.gametest;

import com.misterd.agritechevolved.integration.PlanterPostHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterPreHarvestEvent;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.logic.SeedBin;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class PlanterLifecycleTests {
  private PlanterLifecycleTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put(
        "planter_state_survives_save_and_load",
        new TestCase(250, PlanterLifecycleTests::planterStateSurvivesSaveAndLoad));
    into.put(
        "breaking_planter_drops_everything",
        new TestCase(250, PlanterLifecycleTests::breakingPlanterDropsEverything));
    into.put(
        "contents_kept_when_shape_breaks",
        new TestCase(250, PlanterLifecycleTests::contentsKeptWhenShapeBreaks));
    into.put(
        "breaking_core_stops_planters_and_keeps_contents",
        new TestCase(250, PlanterLifecycleTests::breakingCoreStopsPlantersAndKeepsContents));
    into.put(
        "changing_soil_type_resets_progress",
        new TestCase(150, PlanterLifecycleTests::changingSoilTypeResetsProgress));
    into.put(
        "changing_soil_count_keeps_progress",
        new TestCase(150, PlanterLifecycleTests::changingSoilCountKeepsProgress));
    into.put(
        "soil_swap_through_empty_resets_progress",
        new TestCase(20, PlanterLifecycleTests::soilSwapThroughEmptyResetsProgress));
    into.put(
        "planter_load_clamps_bad_values",
        new TestCase(20, PlanterLifecycleTests::planterLoadClampsBadValues));
    into.put(
        "unloaded_part_pauses_the_multiblock_until_it_loads",
        new TestCase(400, PlanterLifecycleTests::unloadedPartPausesTheMultiblockUntilItLoads));
  }

  // GameTest chunks are force-loaded, and a force-loaded chunk keeps the chunks within two of it
  // loaded as well, so this test builds its multiblock far from the test grid and loads those
  // chunks with its own tickets. The Planter Core's ticket keeps only the chunks next to its own
  // loaded, so removing the far chunk's ticket leaves the far Planter Block unloaded, the same as
  // when a player walks away from a build that crosses a chunk border.
  private static void unloadedPartPausesTheMultiblockUntilItLoads(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    ChunkPos origin = ChunkPos.containing(helper.absolutePos(BlockPos.ZERO));
    ChunkPos coreChunk = new ChunkPos(origin.x(), origin.z() - 200);
    ChunkPos farChunk = new ChunkPos(coreChunk.x() + 2, coreChunk.z());
    level.getChunkSource().addTicketWithRadius(TicketType.FORCED, coreChunk, 1);
    level.getChunkSource().addTicketWithRadius(TicketType.FORCED, farChunk, 1);
    BlockPos core =
        new BlockPos(
            coreChunk.x() * 16 + 15,
            helper.absolutePos(BlockPos.ZERO).getY() + 1,
            coreChunk.z() * 16 + 8);
    BlockPos near = core.south();
    BlockPos far = core.east(17);
    level.setBlockAndUpdate(core, MPBlocks.CORE.get().defaultBlockState());
    level.setBlockAndUpdate(core.east(), MPBlocks.POWER.get().defaultBlockState());
    ((PowerBlockEntity) level.getBlockEntity(core.east())).setStored(4_000_000);
    level.setBlockAndUpdate(core.east(2), MPBlocks.EXPORT.get().defaultBlockState());
    for (int i = 3; i < 17; i++) {
      level.setBlockAndUpdate(core.east(i), MPBlocks.FRAME.get().defaultBlockState());
    }
    level.setBlockAndUpdate(far, MPBlocks.PLANTER.get().defaultBlockState());
    level.setBlockAndUpdate(near, MPBlocks.PLANTER.get().defaultBlockState());
    CoreBlockEntity coreEntity = (CoreBlockEntity) level.getBlockEntity(core);
    PlanterBlockEntity farPlanter = (PlanterBlockEntity) level.getBlockEntity(far);
    PlanterBlockEntity nearPlanter = (PlanterBlockEntity) level.getBlockEntity(near);
    Fixtures.plant(farPlanter, 4, 4, 4);
    Fixtures.plant(nearPlanter, 4, 4, 4);
    int[] progressWhenUnloaded = {0, 0};
    helper
        .startSequence()
        .thenWaitUntil(
            () -> helper.assertTrue(farPlanter.seeds().progress(0) > 0, "far planter grows"))
        .thenExecute(
            () -> level.getChunkSource().removeTicketWithRadius(TicketType.FORCED, farChunk, 1))
        .thenWaitUntil(() -> helper.assertTrue(!level.isLoaded(far), "far chunk unloaded"))
        .thenWaitUntil(
            () ->
                helper.assertValueEqual(
                    coreEntity.lastScan().problem(), Problem.WAITING_FOR_CHUNKS, "problem"))
        .thenExecute(
            () -> {
              progressWhenUnloaded[0] = farPlanter.seeds().progress(0);
              progressWhenUnloaded[1] = nearPlanter.seeds().progress(0);
            })
        .thenIdle(40)
        .thenExecute(
            () -> {
              helper.assertTrue(coreEntity.multiblock() != null, "multiblock kept while waiting");
              helper.assertTrue(coreEntity.planters().contains(far), "far planter still listed");
              helper.assertValueEqual(
                  farPlanter.seeds().progress(0), progressWhenUnloaded[0], "far progress paused");
              helper.assertValueEqual(
                  nearPlanter.seeds().progress(0), progressWhenUnloaded[1], "near progress paused");
              level.getChunkSource().addTicketWithRadius(TicketType.FORCED, farChunk, 1);
            })
        .thenWaitUntil(
            () -> helper.assertTrue(coreEntity.lastScan().formed(), "formed after the reload"))
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    farPlanter.seeds().progress(0) > progressWhenUnloaded[0]
                        && nearPlanter.seeds().progress(0) > progressWhenUnloaded[1],
                    "both planters grow again"))
        .thenExecute(
            () -> {
              helper.assertTrue(coreEntity.ownsPlanter(far), "far planter owned again");
              level.getChunkSource().removeTicketWithRadius(TicketType.FORCED, coreChunk, 1);
              level.getChunkSource().removeTicketWithRadius(TicketType.FORCED, farChunk, 1);
            })
        .thenSucceed();
  }

  private static void holdTwoStacksOfCobblestone(GameTestHelper helper, Fixtures.Rig rig) {
    BlockPos at = helper.absolutePos(rig.planters().getFirst());
    PlanterTests.fillExport(helper, rig, PlanterTests.FULL_EXPORT - 2 * 64);
    PlanterTests.listen(
        helper,
        PlanterPostHarvestEvent.class,
        event -> {
          if (event.getPlanter().getBlockPos().equals(at)) {
            event.getDrops().add(new ItemStack(Items.COBBLESTONE, 64));
            event.getDrops().add(new ItemStack(Items.COBBLESTONE, 64));
          }
        });
  }

  private static ItemStack named(String name) {
    ItemStack seed = new ItemStack(Items.WHEAT_SEEDS);
    seed.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    return seed;
  }

  private static void planterStateSurvivesSaveAndLoad(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
    BlockPos at = helper.absolutePos(rig.planters().getFirst());
    holdTwoStacksOfCobblestone(helper, rig);
    PlanterTests.listen(
        helper,
        PlanterPreHarvestEvent.class,
        event -> {
          if (event.getPlanter().getBlockPos().equals(at)) {
            event.setSeed(named("levelled"));
          }
        });
    Fixtures.plant(planter, 4, 4, 4);
    helper
        .startSequence()
        .thenWaitUntil(
            () -> helper.assertTrue(!planter.heldOutputs().isEmpty(), "outputs are held"))
        .thenExecuteFor(40, () -> {})
        .thenExecute(
            () -> {
              SeedBin<ItemStack> bin = planter.seeds();
              bin.advance(1);
              bin.advance(2);
              bin.advance(2);
              bin.markReady(2);
              helper.assertTrue(!planter.heldOutputs().isEmpty(), "held outputs before the save");
              ServerLevel level = helper.getLevel();
              CompoundTag tag = planter.saveWithFullMetadata(level.registryAccess());
              BlockEntity loaded =
                  BlockEntity.loadStatic(
                      planter.getBlockPos(), planter.getBlockState(), tag, level.registryAccess());
              if (!(loaded instanceof PlanterBlockEntity copy)) {
                helper.fail("the saved tag loads a planter");
                return;
              }

              helper.assertValueEqual(copy.seeds().size(), bin.size(), "bin size");
              for (int i = 0; i < bin.size(); i++) {
                helper.assertTrue(
                    ItemStack.matches(copy.seeds().seed(i), bin.seed(i)), "seed " + i);
                helper.assertValueEqual(
                    copy.seeds().progress(i), bin.progress(i), "progress of seed " + i);
                helper.assertValueEqual(
                    copy.seeds().ready(i), bin.ready(i), "ready flag of seed " + i);
              }
              for (int slot = 0; slot < planter.soilAndUpgrades().size(); slot++) {
                helper.assertTrue(
                    ItemStack.matches(
                        copy.soilAndUpgrades()
                            .getResource(slot)
                            .toStack(copy.soilAndUpgrades().getAmountAsInt(slot)),
                        planter
                            .soilAndUpgrades()
                            .getResource(slot)
                            .toStack(planter.soilAndUpgrades().getAmountAsInt(slot))),
                    "soil and upgrade slot " + slot);
              }
              helper.assertValueEqual(
                  copy.clocheRemainders().snapshot(),
                  planter.clocheRemainders().snapshot(),
                  "cloche remainders");
              helper.assertValueEqual(
                  copy.heldOutputs().size(), planter.heldOutputs().size(), "held stacks");
              for (int i = 0; i < planter.heldOutputs().size(); i++) {
                helper.assertTrue(
                    ItemStack.matches(copy.heldOutputs().get(i), planter.heldOutputs().get(i)),
                    "held stack " + i);
              }
              helper.assertTrue(
                  planter.status() != PlanterStatus.IDLE, "the saved planter is not idle");
              helper.assertValueEqual(copy.status(), planter.status(), "status");
            })
        .thenSucceed();
  }

  private static List<ItemStack> merged(List<ItemStack> stacks) {
    List<ItemStack> merged = new ArrayList<>();
    for (ItemStack stack : stacks) {
      boolean found = false;
      for (ItemStack into : merged) {
        if (ItemStack.isSameItemSameComponents(into, stack)) {
          into.grow(stack.getCount());
          found = true;
          break;
        }
      }
      if (!found) {
        merged.add(stack.copy());
      }
    }
    return merged;
  }

  private static void breakingPlanterDropsEverything(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
    holdTwoStacksOfCobblestone(helper, rig);
    planter.seeds().add(new ItemStack(Items.WHEAT_SEEDS), 3);
    planter.seeds().add(named("custom"), 2);
    Fixtures.plant(planter, 0, 5, 4);
    helper
        .startSequence()
        .thenWaitUntil(
            () -> helper.assertTrue(!planter.heldOutputs().isEmpty(), "outputs are held"))
        .thenExecute(
            () -> {
              List<ItemStack> contents = new ArrayList<>();
              for (int i = 0; i < planter.seeds().size(); i++) {
                contents.add(planter.seeds().seed(i));
              }
              for (int slot = 0; slot < planter.soilAndUpgrades().size(); slot++) {
                contents.add(
                    planter
                        .soilAndUpgrades()
                        .getResource(slot)
                        .toStack(planter.soilAndUpgrades().getAmountAsInt(slot)));
              }
              contents.addAll(planter.heldOutputs());
              contents.add(new ItemStack(MPBlocks.PLANTER.asItem()));
              List<ItemStack> expected = merged(contents);
              helper.assertValueEqual(
                  expected.stream()
                      .filter(s -> s.has(DataComponents.CUSTOM_NAME))
                      .mapToInt(ItemStack::getCount)
                      .sum(),
                  2,
                  "named seeds expected");
              helper.assertTrue(
                  PlanterTests.count(expected, Items.COBBLESTONE) > 0, "held cobblestone expected");

              BlockPos at = helper.absolutePos(rig.planters().getFirst());
              helper.getLevel().destroyBlock(at, true);
              List<ItemStack> dropped = new ArrayList<>();
              for (ItemEntity entity :
                  helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(3))) {
                dropped.add(entity.getItem());
              }
              List<ItemStack> actual = merged(dropped);
              helper.assertValueEqual(actual.size(), expected.size(), "kinds of dropped stacks");
              for (ItemStack want : expected) {
                int got =
                    actual.stream()
                        .filter(s -> ItemStack.isSameItemSameComponents(s, want))
                        .mapToInt(ItemStack::getCount)
                        .sum();
                helper.assertValueEqual(got, want.getCount(), "dropped " + want);
              }
            })
        .thenSucceed();
  }

  private static void contentsKeptWhenShapeBreaks(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
    Fixtures.plant(planter, 4, 4, 4);
    AtomicLong placedAt = new AtomicLong();
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertValueEqual(planter.seeds().progress(0), 40, "progress"))
        .thenExecute(() -> helper.destroyBlock(rig.power()))
        .thenWaitUntil(
            () -> helper.assertValueEqual(planter.status(), PlanterStatus.IDLE, "status"))
        .thenWaitUntil(
            () -> helper.assertTrue(!PlanterTests.active(planter), "planter is inactive"))
        .thenExecute(
            () -> {
              helper.assertValueEqual(planter.seeds().size(), 4, "seeds kept");
              for (int i = 0; i < 4; i++) {
                helper.assertValueEqual(planter.seeds().progress(i), 40, "progress of seed " + i);
              }
              helper.assertValueEqual(
                  planter.soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.SOIL), 4, "soil");
              helper.assertValueEqual(
                  planter.soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_1),
                  4,
                  "upgrade 1");
              helper.assertValueEqual(
                  planter.soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_2),
                  4,
                  "upgrade 2");
              helper.setBlock(rig.power(), MPBlocks.POWER.get());
              PlanterTests.power(helper, rig).setStored(4_000_000);
              placedAt.set(helper.getTick());
            })
        .thenWaitUntil(
            () ->
                helper.assertTrue(
                    PlanterTests.count(PlanterTests.export(helper, rig), Items.WHEAT) > 0,
                    "wheat in the export"))
        .thenExecute(
            () ->
                helper.assertValueEqual(
                    helper.getTick() - placedAt.get(), 47L, "ticks from power back to harvest"))
        .thenSucceed();
  }

  private record Snapshot(
      List<ItemStack> seeds,
      List<Integer> progress,
      List<Boolean> ready,
      List<ItemStack> soilAndUpgrades,
      List<ItemStack> held) {
    static Snapshot of(PlanterBlockEntity planter) {
      List<ItemStack> seeds = new ArrayList<>();
      List<Integer> progress = new ArrayList<>();
      List<Boolean> ready = new ArrayList<>();
      for (int i = 0; i < planter.seeds().size(); i++) {
        seeds.add(planter.seeds().seed(i).copy());
        progress.add(planter.seeds().progress(i));
        ready.add(planter.seeds().ready(i));
      }
      List<ItemStack> soilAndUpgrades = new ArrayList<>();
      for (int slot = 0; slot < planter.soilAndUpgrades().size(); slot++) {
        soilAndUpgrades.add(
            planter
                .soilAndUpgrades()
                .getResource(slot)
                .toStack(planter.soilAndUpgrades().getAmountAsInt(slot)));
      }
      return new Snapshot(
          seeds,
          progress,
          ready,
          soilAndUpgrades,
          planter.heldOutputs().stream().map(ItemStack::copy).toList());
    }

    void assertSame(GameTestHelper helper, Snapshot other, String name) {
      helper.assertTrue(stacksMatch(seeds, other.seeds), name + " seeds");
      helper.assertValueEqual(other.progress, progress, name + " progress");
      helper.assertValueEqual(other.ready, ready, name + " ready flags");
      helper.assertTrue(
          stacksMatch(soilAndUpgrades, other.soilAndUpgrades), name + " soil and upgrades");
      helper.assertTrue(stacksMatch(held, other.held), name + " held outputs");
    }

    private static boolean stacksMatch(List<ItemStack> left, List<ItemStack> right) {
      if (left.size() != right.size()) {
        return false;
      }
      for (int i = 0; i < left.size(); i++) {
        if (!ItemStack.matches(left.get(i), right.get(i))) {
          return false;
        }
      }
      return true;
    }
  }

  private static void breakingCoreStopsPlantersAndKeepsContents(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 2);
    PlanterBlockEntity holding = PlanterTests.planter(helper, rig, 0);
    PlanterBlockEntity growing = PlanterTests.planter(helper, rig, 1);
    holdTwoStacksOfCobblestone(helper, rig);
    Fixtures.plant(holding, 4, 4, 4);
    List<Snapshot> before = new ArrayList<>();
    helper
        .startSequence()
        .thenWaitUntil(
            () -> helper.assertTrue(!holding.heldOutputs().isEmpty(), "outputs are held"))
        .thenExecute(() -> Fixtures.plant(growing, 4, 4, 4))
        .thenWaitUntil(() -> helper.assertValueEqual(growing.seeds().progress(0), 40, "progress"))
        .thenExecute(
            () -> {
              helper.assertValueEqual(
                  holding.status(), PlanterStatus.PAUSED_EXPORT_FULL, "holding status");
              helper.assertValueEqual(growing.status(), PlanterStatus.GROWING, "growing status");
              helper.assertTrue(growing.lastDraw() > 0, "growing planter draws power");
              helper.assertTrue(PlanterTests.active(growing), "growing planter is active");
              before.add(Snapshot.of(holding));
              before.add(Snapshot.of(growing));
              helper.destroyBlock(rig.core());
              for (PlanterBlockEntity planter : List.of(holding, growing)) {
                helper.assertValueEqual(
                    planter.status(), PlanterStatus.IDLE, "status after the core is broken");
                helper.assertValueEqual(planter.lastDraw(), 0L, "draw after the core is broken");
              }
            })
        .thenWaitUntil(
            () -> {
              for (PlanterBlockEntity planter : List.of(holding, growing)) {
                helper.assertTrue(
                    !PlanterTests.active(planter), "inactive after the core is broken");
              }
            })
        .thenExecuteAfter(
            20,
            () -> {
              before.get(0).assertSame(helper, Snapshot.of(holding), "holding planter");
              before.get(1).assertSame(helper, Snapshot.of(growing), "growing planter");
              helper.assertTrue(!holding.heldOutputs().isEmpty(), "held outputs are kept");
              helper.assertTrue(growing.seeds().progress(0) > 0, "progress is kept");
            })
        .thenSucceed();
  }

  private static void changingSoilTypeResetsProgress(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
    Fixtures.plant(planter, 4, 4, 4);
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertValueEqual(planter.seeds().progress(0), 40, "progress"))
        .thenExecute(
            () ->
                planter
                    .soilAndUpgrades()
                    .set(PlanterBlockEntity.SOIL, ItemResource.of(Items.FARMLAND), 4))
        .thenExecute(
            () -> {
              for (int i = 0; i < 4; i++) {
                helper.assertValueEqual(
                    planter.seeds().progress(i),
                    0,
                    "progress of seed " + i + " right after the swap");
              }
            })
        .thenSucceed();
  }

  private static void changingSoilCountKeepsProgress(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.rig(helper, 1);
    PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
    Fixtures.plant(planter, 4, 4, 4);
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertValueEqual(planter.seeds().progress(0), 40, "progress"))
        .thenExecute(
            () ->
                planter
                    .soilAndUpgrades()
                    .set(
                        PlanterBlockEntity.SOIL,
                        planter.soilAndUpgrades().getResource(PlanterBlockEntity.SOIL),
                        5))
        .thenExecuteAfter(
            1,
            () -> {
              for (int i = 0; i < 4; i++) {
                helper.assertTrue(
                    planter.seeds().progress(i) >= 40, "progress of seed " + i + " is kept");
              }
            })
        .thenSucceed();
  }

  private static void soilSwapThroughEmptyResetsProgress(GameTestHelper helper) {
    BlockPos pos = new BlockPos(1, 1, 1);
    helper.setBlock(pos, MPBlocks.PLANTER.get());
    PlanterBlockEntity planter = helper.getBlockEntity(pos, PlanterBlockEntity.class);
    Fixtures.plant(planter, 4, 4, 0);
    for (int i = 0; i < 4; i++) {
      planter.seeds().advance(i);
    }
    helper.assertValueEqual(planter.seeds().progress(0), 1, "progress before the swap");
    planter.soilAndUpgrades().set(PlanterBlockEntity.SOIL, ItemResource.EMPTY, 0);
    helper.assertValueEqual(planter.seeds().progress(0), 1, "progress with the soil removed");
    planter.soilAndUpgrades().set(PlanterBlockEntity.SOIL, ItemResource.of(Items.FARMLAND), 4);
    for (int i = 0; i < 4; i++) {
      helper.assertValueEqual(
          planter.seeds().progress(i), 0, "progress of seed " + i + " after the swap");
    }
    helper.succeed();
  }

  private static void planterLoadClampsBadValues(GameTestHelper helper) {
    BlockPos pos = new BlockPos(1, 1, 1);
    helper.setBlock(pos, MPBlocks.PLANTER.get());
    PlanterBlockEntity planter = helper.getBlockEntity(pos, PlanterBlockEntity.class);
    planter.seeds().add(new ItemStack(Items.WHEAT_SEEDS), 1);
    ServerLevel level = helper.getLevel();
    CompoundTag tag = planter.saveWithFullMetadata(level.registryAccess());
    CompoundTag entry = tag.getListOrEmpty("seeds").getCompoundOrEmpty(0).copy();
    entry.putInt("progress", -5);
    ListTag bad = new ListTag();
    for (int i = 0; i < SeedBin.CAPACITY + 5; i++) {
      bad.add(entry.copy());
    }
    tag.put("seeds", bad);
    BlockEntity loaded =
        BlockEntity.loadStatic(
            planter.getBlockPos(), planter.getBlockState(), tag, level.registryAccess());
    if (!(loaded instanceof PlanterBlockEntity copy)) {
      helper.fail("the tag loads a planter");
      return;
    }
    SeedBin<ItemStack> seeds = copy.seeds();
    helper.assertValueEqual(seeds.size(), SeedBin.CAPACITY, "seeds after the cap");
    for (int i = 0; i < seeds.size(); i++) {
      helper.assertValueEqual(seeds.progress(i), 0, "progress of seed " + i);
    }
    helper.succeed();
  }
}
