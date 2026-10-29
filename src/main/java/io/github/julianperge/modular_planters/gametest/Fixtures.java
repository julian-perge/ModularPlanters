package io.github.julianperge.modular_planters.gametest;

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
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class Fixtures {
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
}
