package io.github.julianperge.modular_planters.gametest;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import io.github.julianperge.modular_planters.ae2.MeBlockEntity;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class Ae2Tests {
    private Ae2Tests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("me_block_joins_the_grid", new TestCase(100, Ae2Tests::meBlockJoinsTheGrid));
        into.put("export_contents_move_into_me", new TestCase(100, Ae2Tests::exportContentsMoveIntoMe));
        into.put("me_offline_falls_back_to_export", new TestCase(200, Ae2Tests::meOfflineFallsBackToExport));
        into.put("breaking_me_block_falls_back_to_export",
                new TestCase(200, Ae2Tests::breakingMeBlockFallsBackToExport));
        into.put("second_me_block_stops_forming", new TestCase(40, Ae2Tests::secondMeBlockStopsForming));
    }

    private static long meCount(GameTestHelper helper, MeBlockEntity me, Item item) {
        IGrid grid = me.mainNode().getGrid();
        helper.assertTrue(grid != null, "the ME block has a grid");
        return grid.getStorageService().getInventory().getAvailableStacks().get(AEItemKey.of(item));
    }

    private static void meBlockJoinsTheGrid(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        helper.succeedWhen(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"));
    }

    private static void exportContentsMoveIntoMe(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
        MeBlockEntity me = helper.getBlockEntity(Fixtures.ME, MeBlockEntity.class);
        MeLinkTests.fill(export, Items.WHEAT, 64);
        helper.succeedWhen(() -> {
            helper.assertValueEqual(MeLinkTests.count(export, Items.WHEAT), 0, "wheat in export");
            helper.assertValueEqual(meCount(helper, me, Items.WHEAT), 64L, "wheat in ME");
        });
    }

    private static void meOfflineFallsBackToExport(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
                .thenExecute(() -> helper.setBlock(Fixtures.ENERGY_CELL, Blocks.AIR))
                .thenWaitUntil(() -> helper.assertTrue(core.meStatus() != MeStatus.ONLINE, "ME went offline"))
                .thenExecute(() -> {
                    helper.assertTrue(core.outputs().insert(new ItemStack(Items.WHEAT, 64)).isEmpty(),
                            "nothing left over");
                    helper.assertValueEqual(MeLinkTests.count(export, Items.WHEAT), 64, "wheat in export");
                })
                .thenSucceed();
    }

    private static void breakingMeBlockFallsBackToExport(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        ExportBlockEntity export = helper.getBlockEntity(rig.export(), ExportBlockEntity.class);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
                .thenExecute(() -> helper.getLevel().destroyBlock(helper.absolutePos(Fixtures.ME), true))
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(core.lastScan().formed(), "still formed");
                    helper.assertValueEqual(core.meStatus(), MeStatus.NONE, "ME status");
                    helper.assertTrue(core.outputs().insert(new ItemStack(Items.WHEAT, 64)).isEmpty(),
                            "nothing left over");
                    helper.assertValueEqual(MeLinkTests.count(export, Items.WHEAT), 64, "wheat in export");
                })
                .thenSucceed();
    }

    private static void secondMeBlockStopsForming(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        helper.setBlock(new BlockPos(3, 2, 1), Ae2Integration.ME_BLOCK.get());
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        helper.succeedWhen(() -> helper.assertValueEqual(core.lastScan().problem(), Problem.SECOND_ME, "problem"));
    }
}
