package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.menu.CoreMenu;
import io.github.julianperge.modular_planters.menu.PlanterMenu;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import io.github.julianperge.modular_planters.network.CoreRequestPayload;
import io.github.julianperge.modular_planters.network.CoreTablePayload;
import io.github.julianperge.modular_planters.network.SeedBinCombosPayload;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.registration.ChannelAttributes;

public final class CoreScreenTests {
    private static final BlockPos OUTSIDE = new BlockPos(6, 1, 6);

    private CoreScreenTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("table_rows_follow_position_order_and_status",
                new TestCase(250, CoreScreenTests::tableRowsFollowPositionOrderAndStatus));
        into.put("open_request_opens_planter_menu_anchored_to_core",
                new TestCase(100, CoreScreenTests::openRequestOpensPlanterMenuAnchoredToCore));
        into.put("open_request_refused_for_planter_outside_structure",
                new TestCase(100, CoreScreenTests::openRequestRefusedForPlanterOutsideStructure));
        into.put("open_request_refused_when_player_far_from_core",
                new TestCase(100, CoreScreenTests::openRequestRefusedWhenPlayerFarFromCore));
        into.put("open_request_refused_without_a_core_menu",
                new TestCase(100, CoreScreenTests::openRequestRefusedWithoutACoreMenu));
        into.put("back_to_core_reopens_core_menu", new TestCase(100, CoreScreenTests::backToCoreReopensCoreMenu));
        into.put("back_to_core_refused_for_a_directly_opened_planter",
                new TestCase(100, CoreScreenTests::backToCoreRefusedForADirectlyOpenedPlanter));
        into.put("remote_planter_menu_closes_when_planter_leaves_structure",
                new TestCase(100, CoreScreenTests::remotePlanterMenuClosesWhenPlanterLeavesStructure));
        into.put("remote_planter_menu_closes_when_core_is_broken",
                new TestCase(100, CoreScreenTests::remotePlanterMenuClosesWhenCoreIsBroken));
        into.put("core_menu_closes_when_core_is_broken", new TestCase(100, CoreScreenTests::coreMenuClosesWhenCoreIsBroken));
        into.put("core_totals_report_no_me", new TestCase(100, CoreScreenTests::coreTotalsReportNoMe));
        into.put("table_payload_survives_the_codec", new TestCase(100, CoreScreenTests::tablePayloadSurvivesTheCodec));
    }

    private record Setup(Fixtures.Rig rig, CoreBlockEntity core, ServerPlayer player) {
        BlockPos corePos(GameTestHelper helper) {
            return helper.absolutePos(rig.core());
        }

        void open() {
            CoreMenu.open(player, core);
        }

        void request(GameTestHelper helper, CoreRequestPayload.Action action, BlockPos relativePlanter) {
            CoreMenu.handle(player, new CoreRequestPayload(action,
                    Optional.ofNullable(relativePlanter).map(helper::absolutePos)));
        }
    }

    // This is the only mock ServerPlayer a 26.1 GameTest can open menus on, because the FakePlayer from NeoForge ignores openMenu.
    // Its connection never negotiated any channels, so the payloads that opening a menu sends are declared as ad hoc channels here.
    @SuppressWarnings("removal")
    private static Setup setup(GameTestHelper helper, int planters) {
        Fixtures.Rig rig = Fixtures.rig(helper, planters);
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Set<Identifier> channels = ChannelAttributes.getOrCreateAdHocChannels(player.connection.getConnection());
        channels.add(AdvancedOpenScreenPayload.TYPE.id());
        channels.add(CoreTablePayload.TYPE.id());
        channels.add(SeedBinCombosPayload.TYPE.id());
        player.setPos(helper.absoluteVec(Vec3.atCenterOf(rig.core())));
        return new Setup(rig, core, player);
    }

    private static void waitUntilFormed(GameTestHelper helper, Setup setup, Runnable then) {
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(setup.core().lastScan().formed() && setup.core().outputs() != null, "formed"))
                .thenExecute(then)
                .thenSucceed();
    }

    private static void tableRowsFollowPositionOrderAndStatus(GameTestHelper helper) {
        Setup setup = setup(helper, 2);
        PlanterBlockEntity first = PlanterTests.planter(helper, setup.rig(), 0);
        Fixtures.plant(first, 4, 4, 4);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(first.status(), PlanterStatus.GROWING, "first status"))
                .thenExecute(() -> {
                    CoreTablePayload table = CoreMenu.buildTable(setup.core(), 7);
                    helper.assertValueEqual(table.containerId(), 7, "container id");
                    helper.assertValueEqual(table.rows().size(), 2, "row count");
                    helper.assertValueEqual(table.rows().get(0).pos(), helper.absolutePos(setup.rig().planters().get(0)), "first row");
                    helper.assertValueEqual(table.rows().get(1).pos(), helper.absolutePos(setup.rig().planters().get(1)), "second row");
                    helper.assertValueEqual(table.rows().get(0).status(), PlanterStatus.GROWING, "first row status");
                    helper.assertValueEqual(table.rows().get(1).status(), PlanterStatus.IDLE, "second row status");
                    helper.assertValueEqual(table.rows().get(0).seeds(), 4, "first row seeds");
                    helper.assertTrue(table.rows().get(0).seedIcon().is(Items.WHEAT_SEEDS), "first row seed icon");
                    helper.assertTrue(table.rows().get(1).seedIcon().isEmpty(), "second row has no seed icon");
                    helper.assertTrue(table.totals().formed(), "formed");
                    helper.assertValueEqual(table.totals().partCount(), 4, "part count");
                    helper.assertValueEqual(table.totals().energyCapacity(), 4_000_000L, "energy capacity");
                    helper.assertTrue(table.totals().problemPos().isEmpty(), "no problem position");
                })
                .thenSucceed();
    }

    private static void openRequestOpensPlanterMenuAnchoredToCore(GameTestHelper helper) {
        Setup setup = setup(helper, 2);
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            setup.request(helper, CoreRequestPayload.Action.OPEN, setup.rig().planters().get(1));
            if (!(setup.player().containerMenu instanceof PlanterMenu menu)) {
                helper.fail("the planter menu is open");
                return;
            }
            helper.assertTrue(menu.fromCore(), "opened from the core");
            helper.assertValueEqual(menu.anchor(), setup.corePos(helper), "anchor");
            helper.assertTrue(menu.stillValid(setup.player()), "valid next to the core");
        });
    }

    private static void openRequestRefusedForPlanterOutsideStructure(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        helper.setBlock(OUTSIDE, MPBlocks.PLANTER.get());
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            CoreMenu open = (CoreMenu) setup.player().containerMenu;
            setup.request(helper, CoreRequestPayload.Action.OPEN, OUTSIDE);
            helper.assertTrue(setup.player().containerMenu == open, "the core menu stays open");
        });
    }

    private static void openRequestRefusedWhenPlayerFarFromCore(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            CoreMenu open = (CoreMenu) setup.player().containerMenu;
            setup.player().setPos(setup.player().position().add(20, 0, 0));
            setup.request(helper, CoreRequestPayload.Action.OPEN, setup.rig().planters().getFirst());
            helper.assertTrue(setup.player().containerMenu == open, "the core menu stays open");
        });
    }

    private static void openRequestRefusedWithoutACoreMenu(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        waitUntilFormed(helper, setup, () -> {
            AbstractContainerMenu before = setup.player().containerMenu;
            setup.request(helper, CoreRequestPayload.Action.OPEN, setup.rig().planters().getFirst());
            helper.assertTrue(setup.player().containerMenu == before, "no menu opens");
        });
    }

    private static void backToCoreReopensCoreMenu(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            setup.request(helper, CoreRequestPayload.Action.OPEN, setup.rig().planters().getFirst());
            helper.assertTrue(setup.player().containerMenu instanceof PlanterMenu, "the planter menu is open");
            setup.request(helper, CoreRequestPayload.Action.BACK_TO_CORE, null);
            if (!(setup.player().containerMenu instanceof CoreMenu menu)) {
                helper.fail("the core menu is open again");
                return;
            }
            helper.assertTrue(menu.core() == setup.core(), "same core");
        });
    }

    private static void backToCoreRefusedForADirectlyOpenedPlanter(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        waitUntilFormed(helper, setup, () -> {
            PlanterBlockEntity planter = PlanterTests.planter(helper, setup.rig(), 0);
            BlockPos planterPos = helper.absolutePos(setup.rig().planters().getFirst());
            PlanterMenu.open(setup.player(), planter, planterPos, false);
            PlanterMenu open = (PlanterMenu) setup.player().containerMenu;
            setup.request(helper, CoreRequestPayload.Action.BACK_TO_CORE, null);
            helper.assertTrue(setup.player().containerMenu == open, "the planter menu stays open");
        });
    }

    private static void remotePlanterMenuClosesWhenPlanterLeavesStructure(GameTestHelper helper) {
        Setup setup = setup(helper, 2);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(setup.core().lastScan().formed() && setup.core().outputs() != null, "formed"))
                .thenExecute(() -> {
                    setup.open();
                    setup.request(helper, CoreRequestPayload.Action.OPEN, setup.rig().planters().get(1));
                    helper.assertTrue(setup.player().containerMenu.stillValid(setup.player()), "valid while formed");
                    setup.player().setPos(setup.player().position().add(20, 0, 0));
                    helper.assertTrue(!setup.player().containerMenu.stillValid(setup.player()), "invalid out of range of the core");
                    setup.player().setPos(helper.absoluteVec(Vec3.atCenterOf(setup.rig().core())));
                    helper.destroyBlock(setup.rig().planters().get(0));
                })
                .thenWaitUntil(() -> helper.assertTrue(!setup.player().containerMenu.stillValid(setup.player()),
                        "invalid once the second planter is cut off from the core"))
                .thenSucceed();
    }

    private static void remotePlanterMenuClosesWhenCoreIsBroken(GameTestHelper helper) {
        Setup setup = setup(helper, 2);
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            setup.request(helper, CoreRequestPayload.Action.OPEN, setup.rig().planters().get(1));
            if (!(setup.player().containerMenu instanceof PlanterMenu menu)) {
                helper.fail("the planter menu is open");
                return;
            }
            helper.assertTrue(menu.stillValid(setup.player()), "valid while the core stands");
            helper.destroyBlock(setup.rig().core());
            helper.assertTrue(!menu.stillValid(setup.player()), "invalid once the core is broken");
        });
    }

    private static void coreMenuClosesWhenCoreIsBroken(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            if (!(setup.player().containerMenu instanceof CoreMenu menu)) {
                helper.fail("the core menu is open");
                return;
            }
            helper.assertTrue(menu.stillValid(setup.player()), "valid while the core stands");
            helper.destroyBlock(setup.rig().core());
            helper.assertTrue(!menu.stillValid(setup.player()), "invalid once the core is broken");
        });
    }

    private static void tablePayloadSurvivesTheCodec(GameTestHelper helper) {
        Setup setup = setup(helper, 2);
        Fixtures.plant(PlanterTests.planter(helper, setup.rig(), 0), 4, 4, 4);
        waitUntilFormed(helper, setup, () -> {
            CoreTablePayload built = CoreMenu.buildTable(setup.core(), 3);
            CoreTablePayload.Totals t = built.totals();
            CoreTablePayload table = new CoreTablePayload(built.containerId(), new CoreTablePayload.Totals(t.formed(), t.partCount(),
                    t.partCap(), t.problem(), t.problemPos(), t.energyStored(), t.energyCapacity(), t.drawPerTick(),
                    t.fertilizerCount(), t.exportFillPercent(), MeStatus.NO_CHANNEL), built.rows());
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                    Unpooled.buffer(), helper.getLevel().registryAccess(), ConnectionType.NEOFORGE);
            CoreTablePayload.STREAM_CODEC.encode(buffer, table);
            CoreTablePayload decoded = CoreTablePayload.STREAM_CODEC.decode(buffer);
            helper.assertValueEqual(decoded.totals(), table.totals(), "totals");
            helper.assertValueEqual(decoded.totals().meStatus(), MeStatus.NO_CHANNEL, "me status");
            helper.assertValueEqual(decoded.rows().size(), table.rows().size(), "rows");
            helper.assertValueEqual(decoded.rows().getFirst().pos(), table.rows().getFirst().pos(), "first row position");
            helper.assertValueEqual(decoded.rows().getFirst().status(), table.rows().getFirst().status(), "first row status");
            helper.assertTrue(decoded.rows().getFirst().seedIcon().is(Items.WHEAT_SEEDS), "seed icon");
            helper.assertTrue(decoded.rows().get(1).seedIcon().isEmpty(), "empty seed icon");
        });
    }

    private static void coreTotalsReportNoMe(GameTestHelper helper) {
        Setup setup = setup(helper, 1);
        waitUntilFormed(helper, setup, () -> {
            setup.open();
            if (!(setup.player().containerMenu instanceof CoreMenu menu)) {
                helper.fail("the core menu is open");
                return;
            }
            helper.assertValueEqual(CoreMenu.buildTable(setup.core(), menu.containerId).totals().meStatus(), MeStatus.NONE, "me status");
        });
    }
}
