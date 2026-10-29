package io.github.julianperge.modular_planters.registry;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.logic.PartType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModularPlanters.MOD_ID);

    public static final DeferredBlock<PartBlock> CORE =
            BLOCKS.registerBlock("core", properties -> new PartBlock(properties, PartType.CORE), MPBlocks::metal);
    public static final DeferredBlock<PartBlock> FRAME =
            BLOCKS.registerBlock("frame", properties -> new PartBlock(properties, PartType.FRAME), MPBlocks::metal);
    public static final DeferredBlock<PartBlock> POWER =
            BLOCKS.registerBlock("power", properties -> new PartBlock(properties, PartType.POWER), MPBlocks::metal);
    public static final DeferredBlock<PartBlock> FERTILIZER =
            BLOCKS.registerBlock("fertilizer", properties -> new PartBlock(properties, PartType.FERTILIZER), MPBlocks::metal);
    public static final DeferredBlock<PartBlock> EXPORT =
            BLOCKS.registerBlock("export", properties -> new PartBlock(properties, PartType.EXPORT), MPBlocks::metal);
    public static final DeferredBlock<PartBlock> PLANTER =
            BLOCKS.registerBlock("planter", properties -> new PartBlock(properties, PartType.PLANTER), MPBlocks::metal);

    private MPBlocks() {
    }

    private static BlockBehaviour.Properties metal(BlockBehaviour.Properties properties) {
        return properties.strength(3.0f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.METAL);
    }
}
