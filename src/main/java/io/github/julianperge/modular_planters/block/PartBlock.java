package io.github.julianperge.modular_planters.block;

import io.github.julianperge.modular_planters.logic.PartType;
import io.github.julianperge.modular_planters.multiblock.CoreFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PartBlock extends Block {
    private final PartType partType;

    public PartBlock(Properties properties, PartType partType) {
        super(properties);
        this.partType = partType;
    }

    public PartType partType() {
        return partType;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        CoreFinder.notifyCores(level, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        CoreFinder.notifyCores(level, pos);
    }
}
