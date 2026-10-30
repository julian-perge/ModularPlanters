package io.github.julianperge.modular_planters.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import com.glodblock.github.appflux.common.me.key.FluxKey;
import com.glodblock.github.appflux.common.me.key.type.EnergyType;
import io.github.julianperge.modular_planters.ae2.MeBlockEntity;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import java.util.Map;
import net.minecraft.gametest.framework.GameTestHelper;

public final class AppliedFluxTests {
    private static final long FE_IN_ME = 1_000_000L;

    private AppliedFluxTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("flux_refills_last_tick_demand", new TestCase(200, AppliedFluxTests::fluxRefillsLastTickDemand));
        into.put("no_flux_without_fe_in_me", new TestCase(100, AppliedFluxTests::noFluxWithoutFeInMe));
    }

    private static IGrid grid(GameTestHelper helper, MeBlockEntity me) {
        IGrid grid = me.mainNode().getGrid();
        helper.assertTrue(grid != null, "the ME block has a grid");
        return grid;
    }

    private static AEKey fe() {
        return FluxKey.of(EnergyType.FE);
    }

    private static long meFe(GameTestHelper helper, MeBlockEntity me) {
        return grid(helper, me).getStorageService().getInventory().getAvailableStacks().get(fe());
    }

    private static long insertFe(GameTestHelper helper, MeBlockEntity me, long amount) {
        return grid(helper, me).getStorageService().getInventory()
                .insert(fe(), amount, Actionable.MODULATE, IActionSource.ofMachine(me));
    }

    private static void fluxRefillsLastTickDemand(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        Fixtures.addFluxCell(helper);
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        MeBlockEntity me = helper.getBlockEntity(Fixtures.ME, MeBlockEntity.class);
        PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
        PowerBlockEntity power = PlanterTests.power(helper, rig);
        Fixtures.plant(planter, 64, 64, 0);
        power.setStored(0);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
                .thenWaitUntil(() -> helper.assertValueEqual(insertFe(helper, me, FE_IN_ME), FE_IN_ME,
                        "FE inserted into ME"))
                .thenWaitUntil(() -> {
                    helper.assertValueEqual(planter.status(), PlanterStatus.GROWING, "status");
                    helper.assertValueEqual(power.stored(), 0, "nothing stored ahead");
                    helper.assertTrue(meFe(helper, me) < FE_IN_ME, "FE came from ME");
                })
                .thenSucceed();
    }

    private static void noFluxWithoutFeInMe(GameTestHelper helper) {
        Fixtures.Rig rig = Fixtures.meRig(helper, 1);
        Fixtures.addFluxCell(helper);
        CoreBlockEntity core = helper.getBlockEntity(rig.core(), CoreBlockEntity.class);
        PlanterBlockEntity planter = PlanterTests.planter(helper, rig, 0);
        PowerBlockEntity power = PlanterTests.power(helper, rig);
        Fixtures.plant(planter, 64, 64, 0);
        power.setStored(0);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertValueEqual(core.meStatus(), MeStatus.ONLINE, "ME status"))
                .thenExecuteAfter(60, () -> {
                    helper.assertValueEqual(planter.status(), PlanterStatus.PAUSED_FOR_POWER, "status");
                    helper.assertValueEqual(power.stored(), 0, "stored");
                })
                .thenSucceed();
    }
}
