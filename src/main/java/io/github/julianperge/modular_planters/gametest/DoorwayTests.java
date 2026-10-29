package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class DoorwayTests {
    private static final BlockPos CORE = new BlockPos(1, 1, 1);

    private DoorwayTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("core_exposes_nothing_unformed", new TestCase(20, DoorwayTests::coreExposesNothingUnformed));
        into.put("core_energy_goes_to_power_blocks_in_order",
                new TestCase(20, DoorwayTests::coreEnergyGoesToPowerBlocksInOrder));
        into.put("core_accepts_only_fertilizer", new TestCase(20, DoorwayTests::coreAcceptsOnlyFertilizer));
        into.put("core_extract_comes_from_export_blocks",
                new TestCase(20, DoorwayTests::coreExtractComesFromExportBlocks));
        into.put("core_capabilities_follow_the_structure",
                new TestCase(20, DoorwayTests::coreCapabilitiesFollowTheStructure));
    }

    private static void placeFormedLine(GameTestHelper helper, int powerBlocks) {
        helper.setBlock(CORE, MPBlocks.CORE.get());
        int x = 1;
        for (int i = 0; i < powerBlocks; i++) {
            helper.setBlock(CORE.east(x++), MPBlocks.POWER.get());
        }
        helper.setBlock(CORE.east(x++), MPBlocks.FERTILIZER.get());
        helper.setBlock(CORE.east(x++), MPBlocks.EXPORT.get());
        helper.setBlock(CORE.east(x), MPBlocks.PLANTER.get());
    }

    private static BlockPos fertilizerPos(int powerBlocks) {
        return CORE.east(powerBlocks + 1);
    }

    private static BlockPos exportPos(int powerBlocks) {
        return CORE.east(powerBlocks + 2);
    }

    private static void coreExposesNothingUnformed(GameTestHelper helper) {
        helper.setBlock(CORE, MPBlocks.CORE.get());
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(helper.getCapability(Capabilities.Energy.BLOCK, CORE, null) == null, "energy capability");
            helper.assertTrue(helper.getCapability(Capabilities.Item.BLOCK, CORE, null) == null, "item capability");
            helper.succeed();
        });
    }

    private static void coreEnergyGoesToPowerBlocksInOrder(GameTestHelper helper) {
        placeFormedLine(helper, 2);
        helper.runAfterDelay(1, () -> {
            EnergyHandler energy = helper.requireCapability(Capabilities.Energy.BLOCK, CORE, null);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(energy.insert(5_000_000, tx), 5_000_000, "inserted energy");
                tx.commit();
            }
            helper.assertValueEqual(helper.getBlockEntity(CORE.east(1), PowerBlockEntity.class).stored(), 4_000_000,
                    "first power block");
            helper.assertValueEqual(helper.getBlockEntity(CORE.east(2), PowerBlockEntity.class).stored(), 1_000_000,
                    "second power block");
            helper.succeed();
        });
    }

    private static void coreAcceptsOnlyFertilizer(GameTestHelper helper) {
        placeFormedLine(helper, 1);
        helper.runAfterDelay(1, () -> {
            ResourceHandler<ItemResource> items = helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(items.insert(ItemResource.of(Items.DIRT), 64, tx), 0, "inserted dirt");
                helper.assertValueEqual(items.insert(ItemResource.of(Items.BONE_MEAL), 64, tx), 64, "inserted bone meal");
                tx.commit();
            }
            FertilizerBlockEntity fertilizer = helper.getBlockEntity(fertilizerPos(1), FertilizerBlockEntity.class);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(fertilizer.storage().extract(ItemResource.of(Items.BONE_MEAL), 64, tx), 64,
                        "bone meal in the fertilizer block");
            }
            helper.succeed();
        });
    }

    private static void coreExtractComesFromExportBlocks(GameTestHelper helper) {
        placeFormedLine(helper, 1);
        helper.runAfterDelay(1, () -> {
            try (Transaction tx = Transaction.openRoot()) {
                helper.getBlockEntity(exportPos(1), ExportBlockEntity.class).storage()
                        .insert(ItemResource.of(Items.WHEAT), 10, tx);
                tx.commit();
            }
            ResourceHandler<ItemResource> items = helper.requireCapability(Capabilities.Item.BLOCK, CORE, null);
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(items.extract(ItemResource.of(Items.WHEAT), 64, tx), 10, "extracted wheat");
            }
            helper.succeed();
        });
    }

    private static void coreCapabilitiesFollowTheStructure(GameTestHelper helper) {
        placeFormedLine(helper, 1);
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(helper.getCapability(Capabilities.Energy.BLOCK, CORE, null) != null, "energy while formed");
            helper.getLevel().destroyBlock(helper.absolutePos(CORE.east()), true);
            helper.runAfterDelay(1, () -> {
                helper.assertTrue(helper.getCapability(Capabilities.Energy.BLOCK, CORE, null) == null,
                        "energy after the break");
                helper.assertTrue(helper.getCapability(Capabilities.Item.BLOCK, CORE, null) == null,
                        "items after the break");
                helper.succeed();
            });
        });
    }
}
