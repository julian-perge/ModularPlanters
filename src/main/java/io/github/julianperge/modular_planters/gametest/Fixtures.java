package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.ae2.Ae2Integration;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class Fixtures {
    public static final BlockPos ME = new BlockPos(1, 2, 1);
    public static final BlockPos ENERGY_CELL = new BlockPos(1, 3, 1);
    private static final BlockPos DRIVE = new BlockPos(2, 2, 1);

    public record Rig(BlockPos core, BlockPos power, BlockPos export, List<BlockPos> planters) {
    }

    private Fixtures() {
    }

    public static Rig rig(GameTestHelper h, int planters) {
        BlockPos core = new BlockPos(1, 1, 1);
        BlockPos power = new BlockPos(2, 1, 1);
        BlockPos export = new BlockPos(3, 1, 1);
        h.setBlock(core, MPBlocks.CORE.get());
        h.setBlock(power, MPBlocks.POWER.get());
        h.getBlockEntity(power, PowerBlockEntity.class).setStored(4_000_000);
        h.setBlock(export, MPBlocks.EXPORT.get());
        List<BlockPos> planterPositions = new ArrayList<>();
        for (int i = 0; i < planters; i++) {
            BlockPos planter = new BlockPos(1, 1, 2 + i);
            h.setBlock(planter, MPBlocks.PLANTER.get());
            planterPositions.add(planter);
        }
        return new Rig(core, power, export, List.copyOf(planterPositions));
    }

    public static Rig meRig(GameTestHelper h, int planters) {
        Rig rig = rig(h, planters);
        h.setBlock(ME, Ae2Integration.ME_BLOCK.get());
        h.setBlock(ENERGY_CELL, ae2Block("creative_energy_cell"));
        h.setBlock(DRIVE, ae2Block("drive"));
        addCell(h, "ae2", "item_storage_cell_64k");
        return rig;
    }

    public static void addFluxCell(GameTestHelper h) {
        addCell(h, "appflux", "fe_4m_cell");
    }

    private static void addCell(GameTestHelper h, String namespace, String path) {
        ResourceHandler<ItemResource> drive =
                h.getLevel().getCapability(Capabilities.Item.BLOCK, h.absolutePos(DRIVE), null);
        Item cell = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(namespace, path));
        h.assertValueEqual(ResourceHandlerUtil.insertStacking(drive, ItemResource.of(cell), 1, null), 1,
                "cells in the drive");
    }

    public static void plant(PlanterBlockEntity p, int seeds, int soil, int mk3PerSlot) {
        p.seeds().add(new ItemStack(Items.WHEAT_SEEDS), seeds);
        if (soil > 0) {
            p.soilAndModules().set(PlanterBlockEntity.SOIL, ItemResource.of(ateItem("infused_farmland")), soil);
        }
        if (mk3PerSlot > 0) {
            p.soilAndModules().set(PlanterBlockEntity.MODULE_1, ItemResource.of(ateItem("sm_mk3")), mk3PerSlot);
            p.soilAndModules().set(PlanterBlockEntity.MODULE_2, ItemResource.of(ateItem("sm_mk3")), mk3PerSlot);
        }
    }

    private static Item ateItem(String path) {
        return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("agritechevolved", path));
    }

    private static Block ae2Block(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("ae2", path));
    }
}
