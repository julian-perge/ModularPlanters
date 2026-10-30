package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPDataComponents;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class StorageTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private StorageTests() {
    }

    public static void register(Map<String, TestCase> into) {
        into.put("power_accepts_any_rate_and_refuses_extract",
                new TestCase(20, StorageTests::powerAcceptsAnyRateAndRefusesExtract));
        into.put("power_rolls_back_aborted_insert", new TestCase(20, StorageTests::powerRollsBackAbortedInsert));
        into.put("power_keeps_energy_when_broken", new TestCase(20, StorageTests::powerKeepsEnergyWhenBroken));
        into.put("power_keeps_energy_above_a_lowered_cap",
                new TestCase(20, StorageTests::powerKeepsEnergyAboveALoweredCap));
        into.put("power_survives_save_and_load", new TestCase(20, StorageTests::powerSurvivesSaveAndLoad));
        into.put("fertilizer_block_accepts_only_fertilizer",
                new TestCase(20, StorageTests::fertilizerBlockAcceptsOnlyFertilizer));
        into.put("export_block_refuses_outside_insert", new TestCase(20, StorageTests::exportBlockRefusesOutsideInsert));
        into.put("storage_blocks_drop_contents_when_broken",
                new TestCase(20, StorageTests::storageBlocksDropContentsWhenBroken));
    }

    private static void powerAcceptsAnyRateAndRefusesExtract(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.POWER.get());
        EnergyHandler energy = helper.requireCapability(Capabilities.Energy.BLOCK, POS, null);
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(energy.insert(1_000_000, tx), 1_000_000, "inserted energy");
            tx.commit();
        }
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(energy.extract(1, tx), 0, "extracted energy");
        }
        helper.succeed();
    }

    private static void powerRollsBackAbortedInsert(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.POWER.get());
        PowerBlockEntity power = helper.getBlockEntity(POS, PowerBlockEntity.class);
        EnergyHandler energy = helper.requireCapability(Capabilities.Energy.BLOCK, POS, null);
        int before = power.stored();
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(energy.insert(1000, tx), 1000, "inserted energy");
        }
        helper.assertValueEqual(power.stored(), before, "stored energy after abort");
        helper.succeed();
    }

    private static void powerKeepsEnergyWhenBroken(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.POWER.get());
        helper.getBlockEntity(POS, PowerBlockEntity.class).setStored(123_456);
        helper.getLevel().destroyBlock(helper.absolutePos(POS), true);

        List<ItemEntity> drops = helper.getEntities(EntityType.ITEM, POS, 2.0);
        helper.assertValueEqual(drops.size(), 1, "dropped item entities");
        ItemStack dropped = drops.getFirst().getItem();
        helper.assertValueEqual(dropped.get(MPDataComponents.STORED_ENERGY.get()), 123_456, "stored energy on the item");

        BlockPos placedAt = new BlockPos(3, 1, 1);
        helper.setBlock(placedAt, MPBlocks.POWER.get());
        PowerBlockEntity placed = helper.getBlockEntity(placedAt, PowerBlockEntity.class);
        placed.applyComponentsFromItemStack(dropped);
        helper.assertValueEqual(placed.stored(), 123_456, "stored energy after placing");
        helper.succeed();
    }

    private static PowerBlockEntity saveAndLoad(GameTestHelper helper, PowerBlockEntity power) {
        ServerLevel level = helper.getLevel();
        CompoundTag tag = power.saveWithFullMetadata(level.registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(power.getBlockPos(), power.getBlockState(), tag, level.registryAccess());
        helper.assertTrue(loaded instanceof PowerBlockEntity, "the saved tag loads a power block");
        return (PowerBlockEntity) loaded;
    }

    private static void powerKeepsEnergyAboveALoweredCap(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.POWER.get());
        PowerBlockEntity power = helper.getBlockEntity(POS, PowerBlockEntity.class);
        power.setStored(4_000_000);
        int original = MPConfig.POWER_BLOCK_BUFFER.get();
        try {
            MPConfig.POWER_BLOCK_BUFFER.set(1_000_000);
            PowerBlockEntity loaded = saveAndLoad(helper, power);
            helper.assertValueEqual(loaded.stored(), 4_000_000, "stored energy above the lowered cap");
            try (Transaction tx = Transaction.openRoot()) {
                helper.assertValueEqual(loaded.externalHandler().insert(1, tx), 0, "inserted energy above the cap");
            }
        } finally {
            MPConfig.POWER_BLOCK_BUFFER.set(original);
        }
        helper.succeed();
    }

    private static void powerSurvivesSaveAndLoad(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.POWER.get());
        PowerBlockEntity power = helper.getBlockEntity(POS, PowerBlockEntity.class);
        power.setStored(123_456);
        helper.assertValueEqual(saveAndLoad(helper, power).stored(), 123_456, "stored energy after a save and load");
        helper.succeed();
    }

    private static void fertilizerBlockAcceptsOnlyFertilizer(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.FERTILIZER.get());
        ResourceHandler<ItemResource> items = helper.requireCapability(Capabilities.Item.BLOCK, POS, null);
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(items.insert(ItemResource.of(Items.DIRT), 64, tx), 0, "inserted dirt");
            helper.assertValueEqual(items.insert(ItemResource.of(Items.BONE_MEAL), 64, tx), 64, "inserted bone meal");
            tx.commit();
        }
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(items.extract(ItemResource.of(Items.BONE_MEAL), 64, tx), 0, "extracted bone meal");
        }
        helper.succeed();
    }

    private static void exportBlockRefusesOutsideInsert(GameTestHelper helper) {
        helper.setBlock(POS, MPBlocks.EXPORT.get());
        ResourceHandler<ItemResource> items = helper.requireCapability(Capabilities.Item.BLOCK, POS, null);
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(items.insert(ItemResource.of(Items.WHEAT), 10, tx), 0, "inserted wheat");
            tx.commit();
        }
        try (Transaction tx = Transaction.openRoot()) {
            helper.getBlockEntity(POS, ExportBlockEntity.class).storage().insert(ItemResource.of(Items.WHEAT), 10, tx);
            tx.commit();
        }
        try (Transaction tx = Transaction.openRoot()) {
            helper.assertValueEqual(items.extract(ItemResource.of(Items.WHEAT), 64, tx), 10, "extracted wheat");
        }
        helper.succeed();
    }

    private static void storageBlocksDropContentsWhenBroken(GameTestHelper helper) {
        BlockPos exportPos = new BlockPos(5, 1, 1);
        helper.setBlock(POS, MPBlocks.FERTILIZER.get());
        helper.setBlock(exportPos, MPBlocks.EXPORT.get());
        try (Transaction tx = Transaction.openRoot()) {
            helper.getBlockEntity(POS, FertilizerBlockEntity.class).storage()
                    .insert(ItemResource.of(Items.BONE_MEAL), 32, tx);
            helper.getBlockEntity(exportPos, ExportBlockEntity.class).storage()
                    .insert(ItemResource.of(Items.WHEAT), 10, tx);
            tx.commit();
        }
        FertilizerBlockEntity fertilizer = helper.getBlockEntity(POS, FertilizerBlockEntity.class);
        ExportBlockEntity export = helper.getBlockEntity(exportPos, ExportBlockEntity.class);
        helper.getLevel().destroyBlock(helper.absolutePos(POS), true);
        helper.getLevel().destroyBlock(helper.absolutePos(exportPos), true);
        helper.assertValueEqual(dropped(helper, POS, Items.BONE_MEAL), 32, "dropped bone meal");
        helper.assertValueEqual(dropped(helper, exportPos, Items.WHEAT), 10, "dropped wheat");
        fertilizer.preRemoveSideEffects(fertilizer.getBlockPos(), fertilizer.getBlockState());
        export.preRemoveSideEffects(export.getBlockPos(), export.getBlockState());
        helper.assertValueEqual(dropped(helper, POS, Items.BONE_MEAL), 32, "bone meal after a second removal call");
        helper.assertValueEqual(dropped(helper, exportPos, Items.WHEAT), 10, "wheat after a second removal call");
        helper.succeed();
    }

    private static int dropped(GameTestHelper helper, BlockPos pos, Item item) {
        int total = 0;
        for (ItemEntity entity : helper.getEntities(EntityType.ITEM, pos, 2.0)) {
            if (entity.getItem().is(item)) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }
}
