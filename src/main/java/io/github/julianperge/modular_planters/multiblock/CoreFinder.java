package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class CoreFinder {
  private static final int SEARCH_LIMIT = 256;

  private CoreFinder() {}

  // Only part blocks count against the search limit, so the air around a large multiblock can't use
  // it up before the core is reached.
  public static int notifyCores(Level level, BlockPos changed) {
    if (level.isClientSide()) {
      return 0;
    }
    int marked = 0;
    int queued = 0;
    Set<BlockPos> seen = new HashSet<>();
    seen.add(changed);
    Deque<BlockPos> queue = new ArrayDeque<>();
    queue.add(changed);
    while (!queue.isEmpty()) {
      BlockPos current = queue.remove();
      for (Direction direction : Direction.values()) {
        BlockPos next = current.relative(direction);
        if (!seen.add(next)
            || !level.isLoaded(next)
            || !(level.getBlockState(next).getBlock() instanceof PartBlock)) {
          continue;
        }
        if (level.getBlockEntity(next) instanceof CoreBlockEntity core) {
          core.markDirty();
          marked++;
        }
        if (queued < SEARCH_LIMIT) {
          queue.add(next);
          queued++;
        }
      }
    }
    return marked;
  }
}
