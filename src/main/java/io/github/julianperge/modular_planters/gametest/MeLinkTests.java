package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.multiblock.FertilizerPool;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import io.github.julianperge.modular_planters.multiblock.OutputSink;
import io.github.julianperge.modular_planters.multiblock.PowerPool;
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

  private MeLinkTests() {}

  public static void register(Map<String, TestCase> into) {
    into.put(
        "online_link_takes_outputs_first",
        new TestCase(20, MeLinkTests::onlineLinkTakesOutputsFirst));
    into.put(
        "offline_link_sends_outputs_to_export",
        new TestCase(20, MeLinkTests::offlineLinkSendsOutputsToExport));
    into.put(
        "removed_link_is_skipped_and_marks_stale",
        new TestCase(20, MeLinkTests::removedLinkIsSkippedAndMarksStale));
    into.put("room_counts_me_room", new TestCase(20, MeLinkTests::roomCountsMeRoom));
    into.put(
        "me_partial_room_sends_the_rest_to_export",
        new TestCase(20, MeLinkTests::mePartialRoomSendsTheRestToExport));
    into.put(
        "export_move_puts_contents_into_me",
        new TestCase(20, MeLinkTests::exportMovePutsContentsIntoMe));
    into.put(
        "export_move_conserves_items_when_me_refuses_part",
        new TestCase(20, MeLinkTests::exportMoveConservesItemsWhenMeRefusesPart));
    into.put(
        "export_move_skips_offline_link",
        new TestCase(20, MeLinkTests::exportMoveSkipsOfflineLink));
    into.put(
        "top_up_fills_fertilizer_from_me",
        new TestCase(20, MeLinkTests::topUpFillsFertilizerFromMe));
    into.put(
        "top_up_ignores_non_fertilizer_filter",
        new TestCase(20, MeLinkTests::topUpIgnoresNonFertilizerFilter));
    into.put("refill_fills_the_free_room", new TestCase(20, MeLinkTests::refillFillsTheFreeRoom));
    into.put("refill_is_capped_by_room", new TestCase(20, MeLinkTests::refillIsCappedByRoom));
  }

  private static ExportBlockEntity placeExport(GameTestHelper helper) {
    helper.setBlock(EXPORT, MPBlocks.EXPORT.get());
    return helper.getBlockEntity(EXPORT, ExportBlockEntity.class);
  }

  private static FertilizerBlockEntity placeFertilizer(GameTestHelper helper) {
    helper.setBlock(EXPORT, MPBlocks.FERTILIZER.get());
    return helper.getBlockEntity(EXPORT, FertilizerBlockEntity.class);
  }

  private static PowerBlockEntity placePower(GameTestHelper helper) {
    helper.setBlock(EXPORT, MPBlocks.POWER.get());
    return helper.getBlockEntity(EXPORT, PowerBlockEntity.class);
  }

  static void fill(ExportBlockEntity export, Item item, int amount) {
    ResourceHandlerUtil.insertStacking(export.storage(), ItemResource.of(item), amount, null);
  }

  static int count(ExportBlockEntity export, Item item) {
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
        ResourceHandlerUtil.insertStacking(
            export.storage(), ItemResource.of(Items.COBBLESTONE), full, null),
        full,
        "pre-filled cobblestone");
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 64);
    OutputSink sink = new OutputSink(List.of(export), me, () -> {});
    helper.assertTrue(sink.hasRoomFor(List.of(new ItemStack(Items.WHEAT, 64))), "ME room counts");
    helper.assertTrue(!sink.hasRoomFor(List.of(new ItemStack(Items.WHEAT, 65))), "65 do not fit");
    helper.assertValueEqual(
        me.stored(ItemResource.of(Items.WHEAT)), 0L, "the simulation stored nothing");
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

  private static void exportMovePutsContentsIntoMe(GameTestHelper helper) {
    ExportBlockEntity export = placeExport(helper);
    fill(export, Items.WHEAT, 64);
    fill(export, Items.COBBLESTONE, 64);
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 1000);
    OutputSink sink = new OutputSink(List.of(export), me, () -> {});
    sink.moveExportsInto(me);
    helper.assertValueEqual(
        count(export, Items.WHEAT) + count(export, Items.COBBLESTONE), 0, "export left");
    helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 64L, "wheat in ME");
    helper.assertValueEqual(
        me.stored(ItemResource.of(Items.COBBLESTONE)), 64L, "cobblestone in ME");
    helper.succeed();
  }

  private static void exportMoveConservesItemsWhenMeRefusesPart(GameTestHelper helper) {
    ExportBlockEntity export = placeExport(helper);
    fill(export, Items.WHEAT, 64);
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 1000);
    me.modulateShortfall = 5;
    OutputSink sink = new OutputSink(List.of(export), me, () -> {});
    sink.moveExportsInto(me);
    helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 59L, "wheat in ME");
    helper.assertValueEqual(count(export, Items.WHEAT), 5, "wheat kept in export");
    helper.succeed();
  }

  private static void exportMoveSkipsOfflineLink(GameTestHelper helper) {
    ExportBlockEntity export = placeExport(helper);
    fill(export, Items.WHEAT, 64);
    FakeMeLink me = new FakeMeLink(MeStatus.OFFLINE, 1000);
    OutputSink sink = new OutputSink(List.of(export), me, () -> {});
    sink.moveExportsInto(me);
    helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 0L, "wheat in ME");
    helper.assertValueEqual(count(export, Items.WHEAT), 64, "wheat kept in export");
    helper.succeed();
  }

  private static void topUpFillsFertilizerFromMe(GameTestHelper helper) {
    FertilizerBlockEntity block = placeFertilizer(helper);
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 5000);
    me.filter = ItemResource.of(Items.BONE_MEAL);
    me.put(ItemResource.of(Items.BONE_MEAL), 2000);
    FertilizerPool pool = new FertilizerPool(List.of(block), () -> {});
    pool.topUpFrom(me);
    helper.assertValueEqual(pool.available(), 27 * 64, "fertilizer stored");
    helper.assertValueEqual(
        me.stored(ItemResource.of(Items.BONE_MEAL)), 2000L - 27 * 64, "bone meal left in ME");
    helper.succeed();
  }

  private static void topUpIgnoresNonFertilizerFilter(GameTestHelper helper) {
    FertilizerBlockEntity block = placeFertilizer(helper);
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 5000);
    me.filter = ItemResource.of(Items.WHEAT);
    me.put(ItemResource.of(Items.WHEAT), 64);
    FertilizerPool pool = new FertilizerPool(List.of(block), () -> {});
    pool.topUpFrom(me);
    helper.assertValueEqual(pool.available(), 0, "fertilizer stored");
    helper.assertValueEqual(me.stored(ItemResource.of(Items.WHEAT)), 64L, "wheat left in ME");
    helper.succeed();
  }

  private static void refillFillsTheFreeRoom(GameTestHelper helper) {
    PowerBlockEntity block = placePower(helper);
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 0);
    me.energy = 5_000_000;
    PowerPool pool = new PowerPool(List.of(block), () -> {});
    helper.assertValueEqual(pool.refillFrom(me), 4_000_000L, "moved");
    helper.assertValueEqual(pool.stored(), 4_000_000L, "stored");
    helper.assertValueEqual(me.energy, 1_000_000L, "left in ME");
    helper.succeed();
  }

  private static void refillIsCappedByRoom(GameTestHelper helper) {
    PowerBlockEntity block = placePower(helper);
    block.setStored(3_999_900);
    FakeMeLink me = new FakeMeLink(MeStatus.ONLINE, 0);
    me.energy = 1_000_000;
    PowerPool pool = new PowerPool(List.of(block), () -> {});
    helper.assertValueEqual(pool.refillFrom(me), 100L, "moved");
    helper.assertValueEqual(pool.stored(), 4_000_000L, "stored");
    helper.assertValueEqual(me.energy, 999_900L, "left in ME");
    helper.succeed();
  }
}
