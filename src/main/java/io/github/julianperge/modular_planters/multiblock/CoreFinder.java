package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public final class CoreFinder {
    private static final int SEARCH_LIMIT = 256;

    private CoreFinder() {
    }

    public static void notifyCores(Level level, BlockPos changed) {
        if (level.isClientSide()) {
            return;
        }
        Set<BlockPos> seen = new HashSet<>();
        seen.add(changed);
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(changed);
        while (!queue.isEmpty() && seen.size() < SEARCH_LIMIT) {
            BlockPos current = queue.remove();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!seen.add(next) || !level.isLoaded(next) || !(level.getBlockState(next).getBlock() instanceof PartBlock)) {
                    continue;
                }
                if (level.getBlockEntity(next) instanceof CoreBlockEntity core) {
                    core.markDirty();
                }
                queue.add(next);
            }
        }
    }
}
