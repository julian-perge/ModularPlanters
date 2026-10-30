package io.github.julianperge.modular_planters.ae2;

import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.logic.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MeBlock extends PartBlock implements EntityBlock {
    public MeBlock(Properties properties) {
        super(properties, PartType.ME);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MeBlockEntity(pos, state);
    }
}
