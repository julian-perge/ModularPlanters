package io.github.julianperge.modular_planters.registry;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.block.CoreBlock;
import io.github.julianperge.modular_planters.block.ExportBlock;
import io.github.julianperge.modular_planters.block.FertilizerBlock;
import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.block.PlanterBlock;
import io.github.julianperge.modular_planters.block.PowerBlock;
import io.github.julianperge.modular_planters.logic.PartType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModularPlanters.MOD_ID);

    public static final DeferredBlock<CoreBlock> CORE =
            BLOCKS.registerBlock("core", CoreBlock::new, MPBlocks::metal);
    public static final DeferredBlock<PartBlock> FRAME =
            BLOCKS.registerBlock("frame", properties -> new PartBlock(properties, PartType.FRAME), MPBlocks::metal);
    public static final DeferredBlock<PowerBlock> POWER =
            BLOCKS.registerBlock("power", PowerBlock::new, MPBlocks::metal);
    public static final DeferredBlock<FertilizerBlock> FERTILIZER =
            BLOCKS.registerBlock("fertilizer", FertilizerBlock::new, MPBlocks::metal);
    public static final DeferredBlock<ExportBlock> EXPORT =
            BLOCKS.registerBlock("export", ExportBlock::new, MPBlocks::metal);
    public static final DeferredBlock<PlanterBlock> PLANTER =
            BLOCKS.registerBlock("planter", PlanterBlock::new, MPBlocks::metal);

    private MPBlocks() {
    }

    private static BlockBehaviour.Properties metal(BlockBehaviour.Properties properties) {
        return properties.strength(3.0f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.METAL);
    }
}
