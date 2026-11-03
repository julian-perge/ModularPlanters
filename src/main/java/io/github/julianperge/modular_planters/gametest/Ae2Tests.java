package io.github.julianperge.modular_planters.gametest;

import appeng.api.networking.IGrid;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.IPriorityHost;
import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import io.github.julianperge.modular_planters.ae2.MeBlockEntity;
import io.github.julianperge.modular_planters.ae2.MeMenu;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class Ae2Tests {
  private static final BlockPos LONE_ME = new BlockPos(1, 1, 1);

  private Ae2Tests() {}

  public static void register(Map<String, TestCase> into) {
    into.put("me_block_joins_the_grid", new TestCase(100, Ae2Tests::meBlockJoinsTheGrid));
    into.put("export_contents_move_into_me", new TestCase(100, Ae2Tests::exportContentsMoveIntoMe));
    into.put(
        "me_offline_falls_back_to_export", new TestCase(200, Ae2Tests::meOfflineFallsBackToExport));
    into.put(
        "breaking_me_block_falls_back_to_export",
        new TestCase(200, Ae2Tests::breakingMeBlockFallsBackToExport));
    into.put(
        "second_me_block_stops_forming", new TestCase(40, Ae2Tests::secondMeBlockStopsForming));
    into.put(
        "filter_clicks_never_move_items", new TestCase(20, Ae2Tests::filterClicksNeverMoveItems));
    into.put(
        "filter_survives_save_and_drops_no_bone_meal",
        new TestCase(20, Ae2Tests::filterSurvivesSaveAndDropsNoBoneMeal));
    into.put("me_block_drops_itself", new TestCase(20, Ae2Tests::meBlockDropsItself));
    into.put("fertilizer_tops_up_from_me", new TestCase(100, Ae2Tests::fertilizerTopsUpFromMe));
    into.put(
        "storage_bus_takes_outputs_and_fertilizer",
        new TestCase(200, Ae2Tests::storageBusTakesOutputsAndFertilizer));
  }

  private static long meCount(GameTestHelper helper, MeBlockEntity me, Item item) {
    IGrid grid = me.mainNode().getGrid();
    helper.assertTrue(grid != null, "the ME block has a grid");
    return grid.getStorageService().getInventory().getAvailableStacks().get(AEItemKey.of(item));
  }

  private static void meBlockJoinsTheGrid(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    helper.succeedWhen(
        () -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"));
  }

  private static void exportContentsMoveIntoMe(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
    MeBlockEntity me = helper.getBlockEntity(Fixtures.ME, MeBlockEntity.class);
    MeLinkTests.fill(export, Items.WHEAT, 64);
    helper.succeedWhen(
        () -> {
          helper.assertValueEqual(MeLinkTests.count(export, Items.WHEAT), 0, "wheat in export");
          helper.assertValueEqual(meCount(helper, me, Items.WHEAT), 64L, "wheat in ME");
        });
  }

  private static void meOfflineFallsBackToExport(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
        .thenExecute(() -> helper.setBlock(Fixtures.ENERGY_CELL, Blocks.AIR))
        .thenWaitUntil(
            () -> helper.assertTrue(core.meStatus() != MeStatus.ONLINE, "ME went offline"))
        .thenExecute(
            () -> {
              helper.assertTrue(
                  core.outputs().insert(new ItemStack(Items.WHEAT, 64)).isEmpty(),
                  "nothing left over");
              helper.assertValueEqual(
                  MeLinkTests.count(export, Items.WHEAT), 64, "wheat in export");
            })
        .thenSucceed();
  }

  private static void breakingMeBlockFallsBackToExport(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
        .thenExecute(() -> helper.getLevel().destroyBlock(helper.absolutePos(Fixtures.ME), true))
        .thenExecuteAfter(
            1,
            () -> {
              helper.assertTrue(core.lastScan().formed(), "still formed");
              helper.assertValueEqual(core.meStatus(), MeStatus.NONE, "ME status");
              helper.assertTrue(
                  core.outputs().insert(new ItemStack(Items.WHEAT, 64)).isEmpty(),
                  "nothing left over");
              helper.assertValueEqual(
                  MeLinkTests.count(export, Items.WHEAT), 64, "wheat in export");
            })
        .thenSucceed();
  }

  private static void secondMeBlockStopsForming(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    helper.setBlock(new BlockPos(3, 2, 1), Ae2Integration.ME_BLOCK.get());
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    helper.succeedWhen(
        () -> helper.assertValueEqual(core.lastScan().problem(), Problem.SECOND_ME, "problem"));
  }

  private static int slotFor(MeMenu menu, Inventory inventory, int inventorySlot) {
    for (Slot slot : menu.slots) {
      if (slot.container == inventory && slot.getContainerSlot() == inventorySlot) {
        return slot.index;
      }
    }
    throw new IllegalStateException("no menu slot shows inventory slot " + inventorySlot);
  }

  private static void filterClicksNeverMoveItems(GameTestHelper helper) {
    helper.setBlock(LONE_ME, Ae2Integration.ME_BLOCK.get());
    MeBlockEntity me = helper.getBlockEntity(LONE_ME, MeBlockEntity.class);
    Player player = helper.makeMockPlayer(GameType.SURVIVAL);
    player.getInventory().setItem(0, new ItemStack(Items.BONE_MEAL, 16));
    MeMenu menu = new MeMenu(1, player.getInventory(), me);
    menu.setCarried(new ItemStack(Items.BONE_MEAL, 16));

    menu.clicked(0, 0, ContainerInput.PICKUP, player);
    helper.assertValueEqual(menu.getCarried().getCount(), 16, "cursor kept");
    helper.assertValueEqual(me.fertilizerFilter(), ItemResource.of(Items.BONE_MEAL), "filter set");
    helper.assertTrue(menu.getSlot(0).getItem().is(Items.BONE_MEAL), "filter slot shows bone meal");
    helper.assertValueEqual(menu.getSlot(0).getItem().getCount(), 1, "filter slot shows one item");

    helper.assertTrue(
        menu.quickMoveStack(player, slotFor(menu, player.getInventory(), 0)).isEmpty(),
        "shift click moves nothing");
    helper.assertValueEqual(player.getInventory().getItem(0).getCount(), 16, "inventory kept");
    menu.clicked(0, 0, ContainerInput.SWAP, player);
    menu.clicked(0, 0, ContainerInput.PICKUP_ALL, player);
    helper.assertValueEqual(
        player.getInventory().getItem(0).getCount(), 16, "inventory kept after swap");
    helper.assertValueEqual(menu.getCarried().getCount(), 16, "cursor kept after swap");
    helper.assertTrue(!menu.canDragTo(menu.getSlot(0)), "no drag");

    menu.setCarried(new ItemStack(Items.WHEAT));
    menu.clicked(0, 0, ContainerInput.PICKUP, player);
    helper.assertValueEqual(
        me.fertilizerFilter(), ItemResource.of(Items.BONE_MEAL), "wheat refused");
    menu.setCarried(ItemStack.EMPTY);
    menu.clicked(0, 0, ContainerInput.PICKUP, player);
    helper.assertValueEqual(me.fertilizerFilter(), ItemResource.EMPTY, "filter cleared");
    helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "filter slot cleared");
    helper.assertValueEqual(menu.getCarried().getCount(), 0, "cursor still empty");

    Player creative = helper.makeMockPlayer(GameType.CREATIVE);
    creative.getAbilities().instabuild = true;
    helper.assertTrue(
        creative.hasInfiniteMaterials(), "the creative player has infinite materials");
    creative.getInventory().setItem(0, new ItemStack(Items.BONE_MEAL, 16));
    MeMenu creativeMenu = new MeMenu(3, creative.getInventory(), me);
    for (ContainerInput input : new ContainerInput[] {ContainerInput.CLONE, ContainerInput.THROW}) {
      for (int button = 0; button < 2; button++) {
        String click = input + " with button " + button;
        helper.assertTrue(
            me.setFertilizerFilter(ItemResource.of(Items.BONE_MEAL)), "filter set before " + click);
        creativeMenu.clicked(0, button, input, creative);
        helper.assertTrue(creativeMenu.getCarried().isEmpty(), "cursor empty after " + click);
        helper.assertValueEqual(
            me.fertilizerFilter(), ItemResource.EMPTY, "filter cleared by " + click);
        helper.assertValueEqual(
            creative.getInventory().getItem(0).getCount(), 16, "inventory kept after " + click);
        helper.assertTrue(
            helper
                .getLevel()
                .getEntitiesOfClass(ItemEntity.class, creative.getBoundingBox().inflate(8.0))
                .isEmpty(),
            "nothing dropped by " + click);
      }
    }

    RegistryFriendlyByteBuf buffer =
        new RegistryFriendlyByteBuf(
            Unpooled.buffer(), helper.getLevel().registryAccess(), ConnectionType.NEOFORGE);
    buffer.writeBlockPos(helper.absolutePos(LONE_ME));
    MeMenu clientMenu = new MeMenu(2, player.getInventory(), buffer);
    helper.assertValueEqual(clientMenu.slots.size(), 37, "client menu slots");
    clientMenu.setCarried(new ItemStack(Items.BONE_MEAL, 16));
    clientMenu.clicked(0, 0, ContainerInput.PICKUP, player);
    helper.assertValueEqual(clientMenu.getCarried().getCount(), 16, "client cursor kept");
    helper.assertTrue(clientMenu.getSlot(0).getItem().isEmpty(), "client filter slot untouched");
    helper.succeed();
  }

  private static void filterSurvivesSaveAndDropsNoBoneMeal(GameTestHelper helper) {
    helper.setBlock(LONE_ME, Ae2Integration.ME_BLOCK.get());
    MeBlockEntity me = helper.getBlockEntity(LONE_ME, MeBlockEntity.class);
    helper.assertTrue(
        me.setFertilizerFilter(ItemResource.of(Items.BONE_MEAL)), "bone meal accepted");
    helper.assertTrue(!me.setFertilizerFilter(ItemResource.of(Items.WHEAT)), "wheat refused");
    ServerLevel level = helper.getLevel();
    CompoundTag tag = me.saveWithFullMetadata(level.registryAccess());
    BlockEntity loaded =
        BlockEntity.loadStatic(me.getBlockPos(), me.getBlockState(), tag, level.registryAccess());
    helper.assertTrue(loaded instanceof MeBlockEntity, "the saved tag loads an ME block");
    helper.assertValueEqual(
        ((MeBlockEntity) loaded).fertilizerFilter(),
        ItemResource.of(Items.BONE_MEAL),
        "filter loaded");

    level.destroyBlock(helper.absolutePos(LONE_ME), true);
    int boneMeal = 0;
    for (ItemEntity entity : helper.getEntities(EntityType.ITEM, LONE_ME, 2.0)) {
      boneMeal += entity.getItem().is(Items.BONE_MEAL) ? entity.getItem().getCount() : 0;
    }
    helper.assertValueEqual(boneMeal, 0, "bone meal dropped");
    helper.succeed();
  }

  private static void meBlockDropsItself(GameTestHelper helper) {
    helper.setBlock(LONE_ME, Ae2Integration.ME_BLOCK.get());
    MeBlockEntity me = helper.getBlockEntity(LONE_ME, MeBlockEntity.class);
    helper.assertTrue(
        me.setFertilizerFilter(ItemResource.of(Items.BONE_MEAL)), "bone meal accepted");
    helper.getLevel().destroyBlock(helper.absolutePos(LONE_ME), true);
    int meBlocks = 0;
    int boneMeal = 0;
    for (ItemEntity entity : helper.getEntities(EntityType.ITEM, LONE_ME, 2.0)) {
      ItemStack stack = entity.getItem();
      meBlocks += stack.is(Ae2Integration.ME_ITEM.get()) ? stack.getCount() : 0;
      boneMeal += stack.is(Items.BONE_MEAL) ? stack.getCount() : 0;
    }
    helper.assertValueEqual(meBlocks, 1, "ME blocks dropped");
    helper.assertValueEqual(boneMeal, 0, "bone meal dropped");
    helper.succeed();
  }

  private static void fertilizerTopsUpFromMe(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    helper.setBlock(new BlockPos(2, 1, 2), MPBlocks.FERTILIZER.get());
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    MeBlockEntity me = helper.getBlockEntity(Fixtures.ME, MeBlockEntity.class);
    helper.assertTrue(me.setFertilizerFilter(ItemResource.of(Items.BONE_MEAL)), "filter set");
    helper
        .startSequence()
        .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
        .thenExecute(
            () ->
                helper.assertValueEqual(
                    me.insertItem(ItemResource.of(Items.BONE_MEAL), 100, false),
                    100L,
                    "bone meal inserted"))
        .thenWaitUntil(
            () -> {
              helper.assertValueEqual(core.fertilizer().available(), 100, "fertilizer stored");
              helper.assertValueEqual(
                  meCount(helper, me, Items.BONE_MEAL), 0L, "bone meal left in ME");
            })
        .thenSucceed();
  }

  private static int chestCount(ChestBlockEntity chest, Item item) {
    int total = 0;
    for (int slot = 0; slot < chest.getContainerSize(); slot++) {
      total += chest.getItem(slot).is(item) ? chest.getItem(slot).getCount() : 0;
    }
    return total;
  }

  // AE2's storage bus opens its own root transaction for every insert and extract, so this crashes
  // the server if the multiblock calls ME while it holds a transaction open.
  private static void storageBusTakesOutputsAndFertilizer(GameTestHelper helper) {
    Fixtures.Rig rig = Fixtures.meRig(helper, 1);
    helper.setBlock(new BlockPos(2, 1, 2), MPBlocks.FERTILIZER.get());
    BlockPos busPos = new BlockPos(2, 3, 1);
    BlockPos chestPos = new BlockPos(3, 3, 1);
    helper.setBlock(chestPos, Blocks.CHEST);
    ChestBlockEntity chest = helper.getBlockEntity(chestPos, ChestBlockEntity.class);
    chest.setItem(0, new ItemStack(Items.BONE_MEAL, 100 - 64));
    chest.setItem(1, new ItemStack(Items.BONE_MEAL, 64));
    ServerLevel level = helper.getLevel();
    helper.assertTrue(
        PartHelper.setPart(
                level, helper.absolutePos(busPos), null, null, partItem("fluix_glass_cable"))
            != null,
        "cable placed");
    IPriorityHost bus =
        (IPriorityHost)
            PartHelper.setPart(
                level, helper.absolutePos(busPos), Direction.EAST, null, partItem("storage_bus"));
    helper.assertTrue(bus != null, "storage bus placed");
    bus.setPriority(10);
    CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
    ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
    MeBlockEntity me = helper.getBlockEntity(Fixtures.ME, MeBlockEntity.class);
    helper.assertTrue(me.setFertilizerFilter(ItemResource.of(Items.BONE_MEAL)), "filter set");
    helper
        .startSequence()
        .thenWaitUntil(
            () -> {
              helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status");
              helper.assertValueEqual(
                  core.fertilizer().available(), 100, "fertilizer taken through the bus");
            })
        .thenExecute(
            () -> {
              helper.assertTrue(
                  core.outputs().hasRoomFor(List.of(new ItemStack(Items.WHEAT, 64))), "room");
              helper.assertTrue(
                  core.outputs().insert(new ItemStack(Items.WHEAT, 64)).isEmpty(),
                  "nothing left over");
              helper.assertValueEqual(chestCount(chest, Items.WHEAT), 64, "wheat in the chest");
              MeLinkTests.fill(export, Items.CARROT, 64);
            })
        .thenWaitUntil(
            () -> {
              helper.assertValueEqual(
                  MeLinkTests.count(export, Items.CARROT), 0, "carrots in export");
              helper.assertValueEqual(chestCount(chest, Items.CARROT), 64, "carrots in the chest");
            })
        .thenSucceed();
  }

  private static IPartItem<?> partItem(String path) {
    return (IPartItem<?>)
        BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("ae2", path));
  }
}
