package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import io.github.julianperge.modular_planters.multiblock.OutputSink;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public final class MeLinkTests {
    private static final BlockPos EXPORT = new BlockPos(1, 1, 1);

    private MeLinkTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("online_link_takes_outputs_first", new TestCase(20, MeLinkTests::onlineLinkTakesOutputsFirst));
        into.put("offline_link_sends_outputs_to_export", new TestCase(20, MeLinkTests::offlineLinkSendsOutputsToExport));
        into.put("removed_link_is_skipped_and_marks_stale",
                new TestCase(20, MeLinkTests::removedLinkIsSkippedAndMarksStale));
        into.put("room_counts_me_room", new TestCase(20, MeLinkTests::roomCountsMeRoom));
        into.put("me_partial_room_sends_the_rest_to_export",
                new TestCase(20, MeLinkTests::mePartialRoomSendsTheRestToExport));
    }

    private static ExportBlockEntity placeExport(GameTestHelper helper) {
        helper.setBlock(EXPORT, MPBlocks.EXPORT.get());
        return helper.getBlockEntity(EXPORT, ExportBlockEntity.class);
    }

    private static int count(ExportBlockEntity export, Item item) {
        ItemStacksResourceHandler storage = export.storage();
        int total = 0;
        for (int slot = 0; slot < storage.size(); slot++) {
            total += storage.getResource(slot).getItem() == item ? storage.getAmountAsInt(slot) : 0;
        }
        return total;
    }

    private static void onlineLinkTakesOutputsFirst(GameTestHelper helper) {
        ExportBlockEntity export = placeExport(helper);
        FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 1000);
        OutputSink sink = new OutputSink(List.of(export), me, () -> {});
        helper.assertTrue(sink.insert(new ItemStack(Items.WHEAT, 64)).isEmpty(), "nothing left over");
        helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 64L, "wheat in ME");
        helper.assertValueEqual(count(export, Items.WHEAT), 0, "wheat in export");
        helper.succeed();
    }

    private static void offlineLinkSendsOutputsToExport(GameTestHelper helper) {
        ExportBlockEntity export = placeExport(helper);
        FakeMeLink me = new FakeMeLink(MeStatus.OFFLINE, 1000);
        OutputSink sink = new OutputSink(List.of(export), me, () -> {});
        helper.assertTrue(sink.insert(new ItemStack(Items.WHEAT, 64)).isEmpty(), "nothing left over");
        helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 0L, "wheat in ME");
        helper.assertValueEqual(count(export, Items.WHEAT), 64, "wheat in export");
        helper.succeed();
    }

    private static void removedLinkIsSkippedAndMarksStale(GameTestHelper helper) {
        ExportBlockEntity export = placeExport(helper);
        FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 1000);
        me.removed = true;
        AtomicBoolean stale = new AtomicBoolean();
        OutputSink sink = new OutputSink(List.of(export), me, () -> stale.set(true));
        helper.assertTrue(sink.insert(new ItemStack(Items.WHEAT, 64)).isEmpty(), "nothing left over");
        helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 0L, "wheat in ME");
        helper.assertValueEqual(count(export, Items.WHEAT), 64, "wheat in export");
        helper.assertTrue(stale.get(), "onStale ran");
        helper.succeed();
    }

    private static void roomCountsMeRoom(GameTestHelper helper) {
        ExportBlockEntity export = placeExport(helper);
        int full = ExportBlockEntity.SLOTS * 64;
        helper.assertValueEqual(
                ResourceHandlerUtil.insertStacking(export.storage(), ItemResource.of(Items.COBBLESTONE), full, null), full,
                "pre-filled cobblestone");
        FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 64);
        OutputSink sink = new OutputSink(List.of(export), me, () -> {});
        helper.assertTrue(sink.hasRoomFor(List.of(new ItemStack(Items.WHEAT, 64))), "ME room counts");
        helper.assertTrue(!sink.hasRoomFor(List.of(new ItemStack(Items.WHEAT, 65))), "65 do not fit");
        helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 0L, "the simulation stored nothing");
        helper.succeed();
    }

    private static void mePartialRoomSendsTheRestToExport(GameTestHelper helper) {
        ExportBlockEntity export = placeExport(helper);
        FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 10);
        OutputSink sink = new OutputSink(List.of(export), me, () -> {});
        List<ItemStack> drops = List.of(new ItemStack(Items.WHEAT, 64), new ItemStack(Items.WHEAT, 64));
        helper.assertTrue(sink.hasRoomFor(drops), "room");
        for (ItemStack drop : drops) {
            helper.assertTrue(sink.insert(drop.copy()).isEmpty(), "nothing left over");
        }
        helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 10L, "wheat in ME");
        helper.assertValueEqual(count(export, Items.WHEAT), 118, "wheat in export");
        helper.succeed();
    }
}
