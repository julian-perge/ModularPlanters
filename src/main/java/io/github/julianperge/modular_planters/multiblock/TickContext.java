package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.logic.AteNumbers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public record TickContext(ServerLevel level, RandomSource random, AteNumbers numbers, PowerPool power,
                          FertilizerPool fertilizer, OutputSink outputs) {
}
