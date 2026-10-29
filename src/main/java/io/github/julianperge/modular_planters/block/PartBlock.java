package io.github.julianperge.modular_planters.block;

import io.github.julianperge.modular_planters.logic.PartType;
import net.minecraft.world.level.block.Block;

public class PartBlock extends Block {
    private final PartType partType;

    public PartBlock(Properties properties, PartType partType) {
        super(properties);
        this.partType = partType;
    }

    public PartType partType() {
        return partType;
    }
}
