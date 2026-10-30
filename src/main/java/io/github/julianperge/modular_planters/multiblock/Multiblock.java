package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.logic.PartType;
import io.github.julianperge.modular_planters.logic.ScanResult;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record Multiblock(
        List<BlockPos> power,
        List<BlockPos> fertilizer,
        List<BlockPos> export,
        List<BlockPos> planters,
        List<BlockPos> frames,
        @Nullable BlockPos me,
        List<BlockPos> all
) {
    // The core checks every part is loaded each tick, so the list of all parts is built once here instead of on each check.
    public Multiblock(List<BlockPos> power, List<BlockPos> fertilizer, List<BlockPos> export, List<BlockPos> planters,
                      List<BlockPos> frames, @Nullable BlockPos me) {
        this(power, fertilizer, export, planters, frames, me, allOf(power, fertilizer, export, planters, frames, me));
    }

    public static Multiblock from(ScanResult<BlockPos> scan) {
        List<BlockPos> meParts = sorted(scan, PartType.ME);
        return new Multiblock(
                sorted(scan, PartType.POWER),
                sorted(scan, PartType.FERTILIZER),
                sorted(scan, PartType.EXPORT),
                sorted(scan, PartType.PLANTER),
                sorted(scan, PartType.FRAME),
                meParts.isEmpty() ? null : meParts.getFirst());
    }

    private static List<BlockPos> allOf(List<BlockPos> power, List<BlockPos> fertilizer, List<BlockPos> export,
                                        List<BlockPos> planters, List<BlockPos> frames, @Nullable BlockPos me) {
        List<BlockPos> all = new ArrayList<>(power);
        all.addAll(fertilizer);
        all.addAll(export);
        all.addAll(planters);
        all.addAll(frames);
        if (me != null) {
            all.add(me);
        }
        return List.copyOf(all);
    }

    private static List<BlockPos> sorted(ScanResult<BlockPos> scan, PartType type) {
        return scan.parts().getOrDefault(type, List.of()).stream().sorted().toList();
    }
}
