package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import io.github.julianperge.modular_planters.network.SeedBinCombosPayload;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class MenuTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);
    private static final int FIRST_INVENTORY_SLOT = 4;

    private MenuTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("seed_slot_keeps_mixed_seeds_separate", new TestCase(20, MenuTests::seedSlotKeepsMixedSeedsSeparate));
        into.put("seed_slot_takes_only_what_fits", new TestCase(20, MenuTests::seedSlotTakesOnlyWhatFits));
        into.put("seed_slot_rejects_other_seeds_and_bad_soil_pairs",
                new TestCase(20, MenuTests::seedSlotRejectsOtherSeedsAndBadSoilPairs));
        into.put("taking_returns_the_trailing_run", new TestCase(20, MenuTests::takingReturnsTheTrailingRun));
        into.put("upgrade_slot_takes_upgrades_only", new TestCase(20, MenuTests::upgradeSlotTakesUpgradesOnly));
        into.put("seed_slot_never_swaps_with_the_cursor", new TestCase(20, MenuTests::seedSlotNeverSwapsWithTheCursor));
        into.put("shift_click_moves_seeds_both_ways_without_loss",
                new TestCase(20, MenuTests::shiftClickMovesSeedsBothWaysWithoutLoss));
        into.put("shift_click_keeps_seeds_that_do_not_fit", new TestCase(20, MenuTests::shiftClickKeepsSeedsThatDoNotFit));
        into.put("hotbar_swap_never_swaps_the_seed_slot", new TestCase(20, MenuTests::hotbarSwapNeverSwapsTheSeedSlot));
        into.put("drag_and_double_click_skip_the_seed_slot", new TestCase(20, MenuTests::dragAndDoubleClickSkipTheSeedSlot));
        into.put("shift_click_keeps_progress_with_a_full_inventory",
                new TestCase(20, MenuTests::shiftClickKeepsProgressWithAFullInventory));
        into.put("shift_click_keeps_progress_with_a_partly_full_inventory",
                new TestCase(20, MenuTests::shiftClickKeepsProgressWithAPartlyFullInventory));
        into.put("throw_drops_seeds_without_loss", new TestCase(20, MenuTests::throwDropsSeedsWithoutLoss));
        into.put("shift_click_refuses_items_that_fit_no_slot", new TestCase(20, MenuTests::shiftClickRefusesItemsThatFitNoSlot));
        into.put("shift_click_moves_soil_and_upgrades_to_their_slots",
                new TestCase(20, MenuTests::shiftClickMovesSoilAndUpgradesToTheirSlots));
        into.put("shift_click_puts_upgrades_and_seeds_in_their_slots_when_all_are_empty",
                new TestCase(20, MenuTests::shiftClickPutsUpgradesAndSeedsInTheirSlotsWhenAllAreEmpty));
        into.put("shift_click_merges_into_an_occupied_upgrade_slot",
                new TestCase(20, MenuTests::shiftClickMergesIntoAnOccupiedUpgradeSlot));
        into.put("hotbar_swap_takes_seeds_into_an_empty_slot", new TestCase(20, MenuTests::hotbarSwapTakesSeedsIntoAnEmptySlot));
        into.put("menu_stays_valid_only_in_range_of_a_standing_planter", new TestCase(20, MenuTests::menuStaysValidOnlyInRange));
        into.put("combos_payload_survives_the_codec", new TestCase(20, MenuTests::combosPayloadSurvivesTheCodec));
        into.put("right_click_moves_one_seed_each_way", new TestCase(20, MenuTests::rightClickMovesOneSeedEachWay));
        into.put("single_transfer_set_throws_without_changing_seeds",
                new TestCase(20, MenuTests::singleTransferSetThrowsWithoutChangingSeeds));
    }

    private record Rig(PlanterBlockEntity planter, Player player, PlanterMenu menu) {
        void click(int slot, ItemStack carried) {
            menu.setCarried(carried);
            menu.clicked(slot, 0, ContainerInput.PICKUP, player);
        }
    }

    private static Rig rig(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.PLANTER.get());
        PlanterBlockEntity planter = helper.getBlockEntity(POS, PlanterBlockEntity.class);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        return new Rig(planter, player, new PlanterMenu(1, inventory, planter, helper.absolutePos(POS), false));
    }

    private static ItemStack seeds(int count) {
        return new ItemStack(Items.WHEAT_SEEDS, count);
    }

    private static ItemStack named(int count) {
        ItemStack stack = seeds(count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Special"));
        return stack;
    }

    private static int total(Rig rig, Item item) {
        int total = rig.menu().getCarried().is(item) ? rig.menu().getCarried().getCount() : 0;
        for (ItemStack stack : rig.player().getInventory().getNonEquipmentItems()) {
            total += stack.is(item) ? stack.getCount() : 0;
        }
        for (int i = 0; i < rig.planter().seeds().size(); i++) {
            total += rig.planter().seeds().seed(i).is(item) ? 1 : 0;
        }
        return total;
    }

    private static void seedSlotKeepsMixedSeedsSeparate(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.click(PlanterMenu.SEED, seeds(3));
        rig.click(PlanterMenu.SEED, named(2));
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "bin size");
        helper.assertValueEqual(rig.planter().seeds().combos().size(), 2, "combos");
        helper.assertValueEqual(rig.menu().getCarried().isEmpty(), true, "cursor empty");
        helper.assertValueEqual(rig.menu().slots.get(PlanterMenu.SEED).getItem().getCount(), 5, "shown count");
        helper.succeed();
    }

    private static void seedSlotTakesOnlyWhatFits(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 60);
        rig.click(PlanterMenu.SEED, seeds(10));
        helper.assertValueEqual(rig.planter().seeds().size(), 64, "bin size");
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 6, "cursor count");
        helper.succeed();
    }

    private static void seedSlotRejectsOtherSeedsAndBadSoilPairs(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 2);
        rig.click(PlanterMenu.SEED, new ItemStack(Items.BEETROOT_SEEDS, 3));
        helper.assertValueEqual(rig.planter().seeds().size(), 2, "bin size after beetroot");
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 3, "cursor after beetroot");

        rig.planter().seeds().clear();
        rig.planter().soilAndUpgrades().set(PlanterBlockEntity.SOIL, ItemResource.of(Items.DIRT), 1);
        rig.click(PlanterMenu.SEED, seeds(4));
        helper.assertValueEqual(rig.planter().seeds().size(), 0, "bin size on dirt");
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 4, "cursor on dirt");
        helper.succeed();
    }

    private static void takingReturnsTheTrailingRun(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 3);
        rig.planter().seeds().add(named(1), 2);
        rig.click(PlanterMenu.SEED, ItemStack.EMPTY);
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 2, "cursor count");
        helper.assertValueEqual(rig.menu().getCarried().has(DataComponents.CUSTOM_NAME), true, "cursor is the named run");
        helper.assertValueEqual(rig.planter().seeds().size(), 3, "bin size");
        helper.succeed();
    }

    private static void upgradeSlotTakesUpgradesOnly(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.click(PlanterMenu.UPGRADE_1, new ItemStack(Items.DIRT, 5));
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_1), 0, "dirt in upgrade slot");
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 5, "dirt stays on cursor");
        Item mk3 = ateItem("sm_mk3");
        rig.click(PlanterMenu.UPGRADE_1, new ItemStack(mk3, 64));
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_1), 64, "upgrades in slot");
        helper.succeed();
    }

    private static void seedSlotNeverSwapsWithTheCursor(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 3);
        rig.click(PlanterMenu.SEED, new ItemStack(Items.DIRT, 2));
        helper.assertValueEqual(rig.planter().seeds().size(), 3, "bin size with dirt");
        helper.assertValueEqual(rig.menu().getCarried().is(Items.DIRT), true, "dirt stays on cursor");
        rig.menu().setCarried(named(1));
        rig.menu().clicked(PlanterMenu.SEED, 1, ContainerInput.PICKUP, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 4, "bin size after right click");
        helper.assertValueEqual(rig.menu().getCarried().isEmpty(), true, "cursor after right click");
        helper.succeed();
    }

    private static void shiftClickMovesSeedsBothWaysWithoutLoss(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.player().getInventory().setItem(10, seeds(10));
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 1, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 10, "bin size after shift-click in");
        helper.assertValueEqual(total(rig, Items.WHEAT_SEEDS), 10, "seeds after shift-click in");
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 0, "bin size after shift-click out");
        helper.assertValueEqual(total(rig, Items.WHEAT_SEEDS), 10, "seeds after shift-click out");
        helper.succeed();
    }

    private static void shiftClickKeepsSeedsThatDoNotFit(GameTestHelper helper) {
        Rig rig = rig(helper);
        Inventory inventory = rig.player().getInventory();
        for (int slot = 0; slot < 36; slot++) {
            inventory.setItem(slot, new ItemStack(Items.DIRT, 64));
        }
        inventory.setItem(0, seeds(60));
        rig.planter().seeds().add(seeds(1), 20);
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 16, "seeds left in the bin");
        helper.assertValueEqual(inventory.getItem(0).getCount(), 64, "topped up stack");
        helper.assertValueEqual(total(rig, Items.WHEAT_SEEDS), 80, "seeds in total");
        helper.succeed();
    }

    private static void hotbarSwapNeverSwapsTheSeedSlot(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 3);
        rig.player().getInventory().setItem(0, named(2));
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.SWAP, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "bin size after hotbar swap");
        helper.assertValueEqual(rig.player().getInventory().getItem(0).isEmpty(), true, "hotbar emptied");
        rig.player().getInventory().setItem(0, new ItemStack(Items.BEETROOT_SEEDS, 2));
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.SWAP, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "bin size with beetroot");
        helper.assertValueEqual(rig.player().getInventory().getItem(0).getCount(), 2, "beetroot stays in the hotbar");
        helper.succeed();
    }

    private static void dragAndDoubleClickSkipTheSeedSlot(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 3);
        rig.planter().seeds().add(named(1), 2);
        rig.menu().setCarried(seeds(1));
        rig.menu().clicked(PlanterMenu.UPGRADE_2, 0, ContainerInput.PICKUP_ALL, rig.player());
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 1, "cursor after double click");
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "bin size after double click");

        rig.menu().setCarried(seeds(6));
        rig.menu().clicked(-999, 0b000, ContainerInput.QUICK_CRAFT, rig.player());
        rig.menu().clicked(PlanterMenu.SEED, 0b001, ContainerInput.QUICK_CRAFT, rig.player());
        rig.menu().clicked(FIRST_INVENTORY_SLOT, 0b001, ContainerInput.QUICK_CRAFT, rig.player());
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 1, 0b001, ContainerInput.QUICK_CRAFT, rig.player());
        rig.menu().clicked(-999, 0b010, ContainerInput.QUICK_CRAFT, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "bin size after drag");
        helper.assertValueEqual(total(rig, Items.WHEAT_SEEDS), 11, "seeds after drag");
        helper.succeed();
    }

    private static void rightClickMovesOneSeedEachWay(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.menu().setCarried(seeds(5));
        rig.menu().clicked(PlanterMenu.SEED, 1, ContainerInput.PICKUP, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 1, "bin size after right-click place");
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 4, "cursor after right-click place");
        rig.menu().setCarried(ItemStack.EMPTY);
        rig.planter().seeds().add(seeds(1), 4);
        rig.menu().clicked(PlanterMenu.SEED, 1, ContainerInput.PICKUP, rig.player());
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 3, "half taken");
        helper.assertValueEqual(rig.planter().seeds().size(), 2, "bin size after half take");
        helper.succeed();
    }

    private static void primeBin(PlanterBlockEntity planter, int seedCount) {
        planter.seeds().add(seeds(1), seedCount);
        for (int i = 0; i < seedCount; i++) {
            for (int step = 0; step < 3; step++) {
                planter.seeds().advance(i);
            }
        }
        for (int i = 0; i < seedCount; i++) {
            planter.seeds().markReady(i);
        }
    }

    private static void assertProgressKept(GameTestHelper helper, PlanterBlockEntity planter, int kept) {
        helper.assertValueEqual(planter.seeds().size(), kept, "seeds left in the bin");
        for (int i = 0; i < kept; i++) {
            helper.assertValueEqual(planter.seeds().progress(i), 3, "progress of seed " + i);
            helper.assertValueEqual(planter.seeds().ready(i), true, "ready flag of seed " + i);
        }
    }

    private static void shiftClickKeepsProgressWithAFullInventory(GameTestHelper helper) {
        Rig rig = rig(helper);
        Inventory inventory = rig.player().getInventory();
        for (int slot = 0; slot < 36; slot++) {
            inventory.setItem(slot, new ItemStack(Items.DIRT, 64));
        }
        primeBin(rig.planter(), 20);
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.QUICK_MOVE, rig.player());
        assertProgressKept(helper, rig.planter(), 20);
        helper.succeed();
    }

    private static void shiftClickKeepsProgressWithAPartlyFullInventory(GameTestHelper helper) {
        Rig rig = rig(helper);
        Inventory inventory = rig.player().getInventory();
        for (int slot = 0; slot < 36; slot++) {
            inventory.setItem(slot, new ItemStack(Items.DIRT, 64));
        }
        inventory.setItem(0, seeds(60));
        primeBin(rig.planter(), 20);
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(inventory.getItem(0).getCount(), 64, "topped up stack");
        assertProgressKept(helper, rig.planter(), 16);
        helper.succeed();
    }

    private static Item ateItem(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("agritechevolved", path));
    }

    private static int droppedSeeds(GameTestHelper helper) {
        int total = 0;
        for (ItemEntity entity : helper.getEntities(EntityType.ITEM, POS, 6.0)) {
            total += entity.getItem().is(Items.WHEAT_SEEDS) ? entity.getItem().getCount() : 0;
        }
        return total;
    }

    private static void throwDropsSeedsWithoutLoss(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.player().setPos(helper.absoluteVec(Vec3.atCenterOf(POS)));
        rig.planter().seeds().add(seeds(1), 3);
        rig.planter().seeds().add(named(1), 2);
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.THROW, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 4, "bin size after Q");
        helper.assertValueEqual(droppedSeeds(helper), 1, "dropped after Q");
        rig.menu().clicked(PlanterMenu.SEED, 1, ContainerInput.THROW, rig.player());
        helper.assertValueEqual(rig.planter().seeds().size(), 0, "bin size after Ctrl+Q");
        helper.assertValueEqual(droppedSeeds(helper), 5, "dropped after Ctrl+Q");
        helper.succeed();
    }

    // This replays ServerInventoryTransfers.singleTransfer from Inventory Essentials 26.1.2.7 for a slot that shows more than one item.
    private static void singleTransferAsInventoryEssentials(Rig rig, Slot slot) {
        ItemStack clicked = slot.getItem();
        ItemStack rest = clicked.copy();
        clicked.setCount(1);
        slot.set(clicked);
        rest.shrink(1);
        rig.menu().clicked(slot.index, 0, ContainerInput.QUICK_MOVE, rig.player());
        if (!slot.hasItem()) {
            slot.set(rest);
        } else if (!rig.player().addItem(rest)) {
            rig.player().drop(rest, false);
        }
    }

    private static void singleTransferSetThrowsWithoutChangingSeeds(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 3);
        rig.planter().seeds().add(named(1), 2);
        boolean threw = false;
        try {
            singleTransferAsInventoryEssentials(rig, rig.menu().getSlot(PlanterMenu.SEED));
        } catch (IllegalStateException expected) {
            threw = true;
        }
        helper.assertTrue(threw, "the seed slot's set throws on the server");
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "bin size");
        helper.assertValueEqual(total(rig, Items.WHEAT_SEEDS), 5, "seeds in total");
        helper.succeed();
    }

    private static void shiftClickRefusesItemsThatFitNoSlot(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.player().getInventory().setItem(10, new ItemStack(Items.STICK, 7));
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 1, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.player().getInventory().getItem(10).getCount(), 7, "sticks stay in the inventory");
        helper.assertValueEqual(rig.planter().seeds().size(), 0, "bin stays empty");
        for (int slot = PlanterMenu.SOIL; slot <= PlanterMenu.UPGRADE_2; slot++) {
            helper.assertValueEqual(rig.menu().slots.get(slot).hasItem(), false, "slot " + slot + " stays empty");
        }
        helper.succeed();
    }

    private static void shiftClickMovesSoilAndUpgradesToTheirSlots(GameTestHelper helper) {
        Rig rig = rig(helper);
        Inventory inventory = rig.player().getInventory();
        inventory.setItem(10, new ItemStack(ateItem("infused_farmland"), 10));
        inventory.setItem(11, new ItemStack(ateItem("sm_mk3"), 10));
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 1, 0, ContainerInput.QUICK_MOVE, rig.player());
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 2, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.SOIL), 10, "soil");
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_1), 10, "first upgrade slot");
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_2), 0, "second upgrade slot");
        helper.assertValueEqual(inventory.getItem(10).isEmpty(), true, "soil left the inventory");
        helper.assertValueEqual(inventory.getItem(11).isEmpty(), true, "upgrades left the inventory");
        helper.succeed();
    }

    private static void shiftClickPutsUpgradesAndSeedsInTheirSlotsWhenAllAreEmpty(GameTestHelper helper) {
        Rig rig = rig(helper);
        Inventory inventory = rig.player().getInventory();
        inventory.setItem(10, new ItemStack(ateItem("sm_mk3"), 10));
        inventory.setItem(11, seeds(5));
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 1, 0, ContainerInput.QUICK_MOVE, rig.player());
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 2, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.SOIL), 0, "soil slot");
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_1), 10, "first upgrade slot");
        helper.assertValueEqual(rig.planter().seeds().size(), 5, "seeds in the bin");
        helper.succeed();
    }

    private static void shiftClickMergesIntoAnOccupiedUpgradeSlot(GameTestHelper helper) {
        Rig rig = rig(helper);
        Item mk3 = ateItem("sm_mk3");
        rig.planter().soilAndUpgrades().set(PlanterBlockEntity.UPGRADE_1, ItemResource.of(ateItem("sm_mk2")), 4);
        rig.planter().soilAndUpgrades().set(PlanterBlockEntity.UPGRADE_2, ItemResource.of(mk3), 10);
        rig.player().getInventory().setItem(10, new ItemStack(mk3, 5));
        rig.menu().clicked(FIRST_INVENTORY_SLOT + 1, 0, ContainerInput.QUICK_MOVE, rig.player());
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_2), 15, "merged upgrades");
        helper.assertValueEqual(rig.planter().soilAndUpgrades().getAmountAsInt(PlanterBlockEntity.UPGRADE_1), 4, "other upgrade slot");
        helper.assertValueEqual(rig.player().getInventory().getItem(10).isEmpty(), true, "inventory emptied");
        helper.succeed();
    }

    private static void hotbarSwapTakesSeedsIntoAnEmptySlot(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.planter().seeds().add(seeds(1), 3);
        rig.planter().seeds().add(named(1), 2);
        rig.menu().clicked(PlanterMenu.SEED, 0, ContainerInput.SWAP, rig.player());
        ItemStack hotbar = rig.player().getInventory().getItem(0);
        helper.assertValueEqual(hotbar.getCount(), 2, "hotbar count");
        helper.assertValueEqual(hotbar.has(DataComponents.CUSTOM_NAME), true, "hotbar holds the named run");
        helper.assertValueEqual(rig.planter().seeds().size(), 3, "bin size");
        helper.succeed();
    }

    private static void menuStaysValidOnlyInRange(GameTestHelper helper) {
        Rig rig = rig(helper);
        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(POS));
        rig.player().setPos(center);
        helper.assertValueEqual(rig.menu().stillValid(rig.player()), true, "in range");
        rig.player().setPos(center.add(100, 0, 0));
        helper.assertValueEqual(rig.menu().stillValid(rig.player()), false, "out of range");
        rig.player().setPos(center);
        helper.getLevel().destroyBlock(helper.absolutePos(POS), false);
        helper.assertValueEqual(rig.menu().stillValid(rig.player()), false, "after the planter is removed");
        helper.succeed();
    }

    private static void combosPayloadSurvivesTheCodec(GameTestHelper helper) {
        SeedBinCombosPayload sent = new SeedBinCombosPayload(7, List.of(seeds(3), named(2)));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess(), ConnectionType.NEOFORGE);
        SeedBinCombosPayload.STREAM_CODEC.encode(buffer, sent);
        SeedBinCombosPayload received = SeedBinCombosPayload.STREAM_CODEC.decode(buffer);
        helper.assertValueEqual(received.containerId(), 7, "container id");
        helper.assertValueEqual(received.combos().size(), 2, "combo count");
        for (int i = 0; i < 2; i++) {
            helper.assertValueEqual(ItemStack.matches(received.combos().get(i), sent.combos().get(i)), true, "combo " + i);
        }
        helper.succeed();
    }
}
