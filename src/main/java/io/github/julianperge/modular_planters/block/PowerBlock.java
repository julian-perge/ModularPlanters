package io.github.julianperge.modular_planters.block;

import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import io.github.julianperge.modular_planters.logic.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class PowerBlock extends PartBlock implements EntityBlock {
    public PowerBlock(Properties properties) {
        super(properties, PartType.POWER);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerBlockEntity(pos, state);
    }
}
