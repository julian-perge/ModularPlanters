package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
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
        into.put("module_slot_takes_modules_only", new TestCase(20, MenuTests::moduleSlotTakesModulesOnly));
        into.put("seed_slot_never_swaps_with_the_cursor", new TestCase(20, MenuTests::seedSlotNeverSwapsWithTheCursor));
        into.put("shift_click_moves_seeds_both_ways_without_loss",
                new TestCase(20, MenuTests::shiftClickMovesSeedsBothWaysWithoutLoss));
        into.put("shift_click_keeps_seeds_that_do_not_fit", new TestCase(20, MenuTests::shiftClickKeepsSeedsThatDoNotFit));
        into.put("hotbar_swap_never_swaps_the_seed_slot", new TestCase(20, MenuTests::hotbarSwapNeverSwapsTheSeedSlot));
        into.put("drag_and_double_click_skip_the_seed_slot", new TestCase(20, MenuTests::dragAndDoubleClickSkipTheSeedSlot));
        into.put("right_click_moves_one_seed_each_way", new TestCase(20, MenuTests::rightClickMovesOneSeedEachWay));
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
        rig.planter().soilAndModules().set(PlanterBlockEntity.SOIL, ItemResource.of(Items.DIRT), 1);
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

    private static void moduleSlotTakesModulesOnly(GameTestHelper helper) {
        Rig rig = rig(helper);
        rig.click(PlanterMenu.MODULE_1, new ItemStack(Items.DIRT, 5));
        helper.assertValueEqual(rig.planter().soilAndModules().getAmountAsInt(PlanterBlockEntity.MODULE_1), 0, "dirt in module slot");
        helper.assertValueEqual(rig.menu().getCarried().getCount(), 5, "dirt stays on cursor");
        Item mk3 = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("agritechevolved", "sm_mk3"));
        rig.click(PlanterMenu.MODULE_1, new ItemStack(mk3, 64));
        helper.assertValueEqual(rig.planter().soilAndModules().getAmountAsInt(PlanterBlockEntity.MODULE_1), 64, "modules in slot");
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
        rig.menu().clicked(PlanterMenu.MODULE_2, 0, ContainerInput.PICKUP_ALL, rig.player());
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
}
