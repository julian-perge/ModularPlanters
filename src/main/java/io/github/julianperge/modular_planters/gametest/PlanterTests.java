package io.github.julianperge.modular_planters.gametest;

import com.misterd.agritechevolved.integration.PlanterPostHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterPreHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterProcessingTimeEvent;
import io.github.julianperge.modular_planters.block.PlanterBlock;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.logic.SeedBin;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class PlanterTests {
    private static final int ROUND_ENERGY = 4 * 512 * 87;
    static final int FULL_EXPORT = ExportBlockEntity.SLOTS * 64;

    private PlanterTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("harvests_on_ate_schedule", new TestCase(150, PlanterTests::harvestsOnAteSchedule));
        into.put("events_fire_per_seed_with_own_drops", new TestCase(400, PlanterTests::eventsFirePerSeedWithOwnDrops));
        into.put("pauses_when_power_short", new TestCase(40, PlanterTests::pausesWhenPowerShort));
        into.put("power_goes_to_planters_in_position_order",
                new TestCase(120, PlanterTests::powerGoesToPlantersInPositionOrder));
        into.put("soil_limits_growing_seeds", new TestCase(150, PlanterTests::soilLimitsGrowingSeeds));
        into.put("fertilizer_is_used_per_seed", new TestCase(120, PlanterTests::fertilizerIsUsedPerSeed));
        into.put("waits_for_export_room", new TestCase(160, PlanterTests::waitsForExportRoom));
        into.put("extra_drops_are_held_not_voided", new TestCase(180, PlanterTests::extraDropsAreHeldNotVoided));
    }

    static PlanterBlockEntity planter(GameTestHelper helper, Fixtures.Rig rig, int index) {
        return helper.getBlockEntity(rig.planters().get(index), PlanterBlockEntity.class);
    }

    static PowerBlockEntity power(GameTestHelper helper, Fixtures.Rig rig) {
        return helper.getBlockEntity(rig.power(), PowerBlockEntity.class);
    }

    static ItemStacksResourceHandler export(GameTestHelper helper, Fixtures.Rig rig) {
        return helper.getBlockEntity(rig.export(), ExportBlockEntity.class).storage();
    }

    static int count(ItemStacksResourceHandler storage, Item item) {
        int total = 0;
        for (int slot = 0; slot < storage.size(); slot++) {
            if (storage.getResource(slot).is(item)) {
                total += storage.getAmountAsInt(slot);
            }
        }
        return total;
    }

    static int count(List<ItemStack> stacks, Item item) {
        return stacks.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }

    static void fillExport(GameTestHelper helper, Fixtures.Rig rig, int amount) {
        helper.assertValueEqual(ResourceHandlerUtil.insertStacking(export(helper, rig), ItemResource.of(Items.COBBLESTONE),
                amount, null), amount, "pre-filled cobblestone");
    }

    private static void assertProgress(GameTestHelper helper, PlanterBlockEntity planter, int from, int expected) {
        SeedBin<ItemStack> seeds = planter.seeds();
        for (int i = from; i < seeds.size(); i++) {
            helper.assertValueEqual(seeds.progress(i), expected, "progress of seed " + i);
        }
    }

    static boolean active(PlanterBlockEntity planter) {
        return planter.getBlockState().getValue(PlanterBlock.ACTIVE);
    }

    // Every GameTest running side by side shares the event bus, so each caller's listener must check its own test's planter position, and this removes the listener once its test passes or fails.
    static <T extends Event> void listen(GameTestHelper helper, Class<T> type, Consumer<T> listener) {
        NeoForge.EVENT_BUS.addListener(type, listener);
        helper.testInfo.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo testInfo) {
            }

            @Override
            public void testPassed(GameTestInfo testInfo, GameTestRunner runner) {
                NeoForge.EVENT_BUS.unregister(listener);
            }

            @Override
            public void testFailed(GameTestInfo testInfo, GameTestRunner runner) {
                NeoForge.EVENT_BUS.unregister(listener);
            }

            @Override
            public void testAddedForRerun(GameTestInfo original, GameTestInfo copy, GameTestRunner runner) {
            }
        });
    }

    private static void harvestsOnAteSchedule(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        Fixtures.plant(planter(helper, rig, 0), 4, 4, 4);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(count(export(helper, rig), Items.WHEAT) > 0, "wheat in the export"))
                .thenExecute(() -> {
                    helper.assertValueEqual(count(export(helper, rig), Items.WHEAT), 4, "exported wheat");
                    helper.assertValueEqual(4_000_000 - power(helper, rig).stored(), ROUND_ENERGY, "energy used");
                })
                .thenSucceed();
    }

    private static void eventsFirePerSeedWithOwnDrops(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        PlanterBlockEntity planter = planter(helper, rig, 0);
        BlockPos at = helper.absolutePos(rig.planters().getFirst());
        AtomicInteger pre = new AtomicInteger();
        AtomicInteger post = new AtomicInteger();
        AtomicInteger times = new AtomicInteger();
        listen(helper, PlanterPreHarvestEvent.class, event -> {
            if (event.getPlanter().getBlockPos().equals(at) && pre.incrementAndGet() % 2 == 1) {
                ItemStack levelled = event.getSeed().copy();
                levelled.set(DataComponents.CUSTOM_NAME, Component.literal("levelled"));
                event.setSeed(levelled);
            }
        });
        listen(helper, PlanterPostHarvestEvent.class, event -> {
            if (event.getPlanter().getBlockPos().equals(at)) {
                for (ItemStack drop : event.getDrops()) {
                    if (!drop.is(Items.WHEAT_SEEDS)) {
                        drop.grow(3);
                    }
                }
                post.incrementAndGet();
            }
        });
        listen(helper, PlanterProcessingTimeEvent.class, event -> {
            if (event.getPlanter().getBlockPos().equals(at)) {
                times.incrementAndGet();
            }
        });
        Fixtures.plant(planter, 4, 4, 4);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(post.get() >= 4, "first round harvested"))
                .thenExecute(() -> {
                    helper.assertValueEqual(post.get(), 4, "post-harvest events after the first round");
                    helper.assertValueEqual(times.get(), 87, "processing time events in the first round");
                    helper.assertValueEqual(count(export(helper, rig), Items.WHEAT), 16, "wheat after the first round");
                    List<SeedBin.Combo<ItemStack>> combos = planter.seeds().combos();
                    helper.assertValueEqual(combos.size(), 2, "seed combinations");
                    for (SeedBin.Combo<ItemStack> combo : combos) {
                        helper.assertValueEqual(combo.count(), 2, "seeds in a combination");
                    }
                    for (int i = 0; i < 4; i++) {
                        helper.assertValueEqual(planter.seeds().seed(i).has(DataComponents.CUSTOM_NAME), i % 2 == 0,
                                "custom name on seed " + i);
                    }
                })
                .thenWaitUntil(() -> helper.assertTrue(post.get() >= 12, "third round harvested"))
                .thenExecute(() -> {
                    helper.assertValueEqual(post.get(), 12, "post-harvest events after the third round");
                    helper.assertValueEqual(count(export(helper, rig), Items.WHEAT), 49, "wheat after the third round");
                })
                .thenSucceed();
    }

    private static void pausesWhenPowerShort(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        PlanterBlockEntity planter = planter(helper, rig, 0);
        Fixtures.plant(planter, 4, 4, 4);
        power(helper, rig).setStored(2047);
        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(planter.status(), PlanterStatus.PAUSED_FOR_POWER, "status");
            helper.assertFalse(active(planter), "active while paused for power");
            helper.assertValueEqual(power(helper, rig).stored(), 2047, "stored energy");
            assertProgress(helper, planter, 0, 0);
            helper.succeed();
        });
    }

    private static void powerGoesToPlantersInPositionOrder(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 2);
        PlanterBlockEntity first = planter(helper, rig, 0);
        PlanterBlockEntity second = planter(helper, rig, 1);
        Fixtures.plant(first, 4, 4, 4);
        Fixtures.plant(second, 4, 4, 4);
        PowerBlockEntity power = power(helper, rig);
        power.setStored(0);
        helper.onEachTick(() -> power.setStored(power.stored() + 2048));
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(count(export(helper, rig), Items.WHEAT) > 0, "wheat in the export"))
                .thenExecute(() -> {
                    helper.assertTrue(helper.getTick() <= 100, "first planter harvested by tick 100");
                    helper.assertValueEqual(count(export(helper, rig), Items.WHEAT), 4, "exported wheat");
                    helper.assertValueEqual(second.status(), PlanterStatus.PAUSED_FOR_POWER, "second planter status");
                    assertProgress(helper, second, 0, 0);
                })
                .thenSucceed();
    }

    private static void soilLimitsGrowingSeeds(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        PlanterBlockEntity planter = planter(helper, rig, 0);
        Fixtures.plant(planter, 4, 2, 4);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(count(export(helper, rig), Items.WHEAT) > 0, "wheat in the export"))
                .thenExecute(() -> {
                    helper.assertValueEqual(planter.status(), PlanterStatus.SHORT_ON_SOIL, "status");
                    helper.assertTrue(active(planter), "active while short on soil");
                    helper.assertValueEqual(count(export(helper, rig), Items.WHEAT), 2, "exported wheat");
                    assertProgress(helper, planter, 2, 0);
                })
                .thenSucceed();
    }

    private static void fertilizerIsUsedPerSeed(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        BlockPos fertilizerPos = new BlockPos(1, 1, 0);
        helper.setBlock(fertilizerPos, MPBlocks.FERTILIZER.get());
        ItemStacksResourceHandler fertilizer = helper.getBlockEntity(fertilizerPos, FertilizerBlockEntity.class).storage();
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(fertilizer.insert(ItemResource.of(Items.BONE_MEAL), 4, tx), 4, "inserted bone meal");
            tx.commit();
        }
        Fixtures.plant(planter(helper, rig, 0), 4, 4, 4);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(count(export(helper, rig), Items.WHEAT) > 0, "wheat in the export"))
                .thenExecute(() -> {
                    helper.assertTrue(helper.getTick() <= 75, "harvested by tick 75, at tick " + helper.getTick());
                    helper.assertValueEqual(count(fertilizer, Items.BONE_MEAL), 0, "bone meal left");
                })
                .thenSucceed();
    }

    private static void waitsForExportRoom(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        PlanterBlockEntity planter = planter(helper, rig, 0);
        fillExport(helper, rig, FULL_EXPORT);
        Fixtures.plant(planter, 4, 4, 4);
        AtomicLong extractedAt = new AtomicLong();
        helper.startSequence()
                .thenExecuteAfter(100, () -> {
                    helper.assertValueEqual(planter.status(), PlanterStatus.PAUSED_EXPORT_FULL, "status");
                    for (int i = 0; i < 4; i++) {
                        helper.assertTrue(planter.seeds().ready(i), "seed " + i + " ready");
                    }
                    helper.assertValueEqual(4_000_000 - power(helper, rig).stored(), ROUND_ENERGY, "energy used");
                    try (Transaction tx = Transaction.openRoot()) {
                        helper.assertValueEqual(export(helper, rig).extract(ItemResource.of(Items.COBBLESTONE), 128, tx), 128,
                                "extracted cobblestone");
                        tx.commit();
                    }
                    extractedAt.set(helper.getTick());
                })
                .thenWaitUntil(() -> helper.assertTrue(count(export(helper, rig), Items.WHEAT) > 0, "wheat in the export"))
                .thenExecute(() -> {
                    helper.assertTrue(helper.getTick() - extractedAt.get() <= 2, "harvest within 2 ticks of the room");
                    helper.assertValueEqual(count(export(helper, rig), Items.COBBLESTONE), FULL_EXPORT - 128,
                            "cobblestone left");
                    helper.assertValueEqual(count(export(helper, rig), Items.WHEAT), 4, "exported wheat");
                })
                .thenSucceed();
    }

    private static void extraDropsAreHeldNotVoided(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.rig(helper, 1);
        PlanterBlockEntity planter = planter(helper, rig, 0);
        BlockPos at = helper.absolutePos(rig.planters().getFirst());
        int before = FULL_EXPORT - 2 * 64;
        fillExport(helper, rig, before);
        AtomicInteger post = new AtomicInteger();
        listen(helper, PlanterPostHarvestEvent.class, event -> {
            if (event.getPlanter().getBlockPos().equals(at)) {
                event.getDrops().add(new ItemStack(Items.COBBLESTONE, 64));
                event.getDrops().add(new ItemStack(Items.COBBLESTONE, 64));
                post.incrementAndGet();
            }
        });
        Fixtures.plant(planter, 4, 4, 4);
        AtomicLong extractedAt = new AtomicLong();
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(post.get() >= 4, "harvested"))
                .thenExecute(() -> {
                    int held = count(planter.heldOutputs(), Items.COBBLESTONE);
                    int exported = count(export(helper, rig), Items.COBBLESTONE);
                    helper.assertTrue(held > 0, "some cobblestone is held");
                    helper.assertValueEqual(held + exported, before + 512, "held plus exported cobblestone");
                    helper.assertValueEqual(planter.status(), PlanterStatus.PAUSED_EXPORT_FULL, "status");
                })
                .thenExecuteFor(20, () -> {
                    assertProgress(helper, planter, 0, 0);
                    helper.assertValueEqual(planter.status(), PlanterStatus.PAUSED_EXPORT_FULL, "status while held");
                })
                .thenExecute(() -> {
                    try (Transaction tx = Transaction.openRoot()) {
                        export(helper, rig).extract(ItemResource.of(Items.COBBLESTONE), FULL_EXPORT, tx);
                        tx.commit();
                    }
                    extractedAt.set(helper.getTick());
                })
                .thenWaitUntil(() -> {
                    helper.assertTrue(planter.heldOutputs().isEmpty(), "held outputs moved out");
                    for (int i = 0; i < 4; i++) {
                        helper.assertTrue(planter.seeds().progress(i) > 0, "seed " + i + " growing again");
                    }
                })
                .thenExecute(() -> helper.assertTrue(helper.getTick() - extractedAt.get() <= 2,
                        "held outputs moved within 2 ticks"))
                .thenSucceed();
    }
}
