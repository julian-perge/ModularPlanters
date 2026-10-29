package io.github.julianperge.modular_planters.multiblock;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

final class LiveBlocks {
    private LiveBlocks() {
    }

    // A pool is only rebuilt when the core's scan changes, so a block broken or unloaded since then is still listed.
    // This returns only the blocks that are not removed and asks the core to rescan when it finds a removed one.
    static <T extends BlockEntity> List<T> of(List<T> blocks, Runnable onStale) {
        List<T> live = new ArrayList<>(blocks.size());
        boolean stale = false;
        for (T block : blocks) {
            if (block.isRemoved()) {
                stale = true;
            } else {
                live.add(block);
            }
        }
        if (stale) {
            onStale.run();
        }
        return live;
    }
}
