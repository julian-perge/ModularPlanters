package io.github.julianperge.modular_planters;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MPConfig {
  private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

  public static final ModConfigSpec.IntValue PART_CAP =
      BUILDER
          .comment("The most parts one multiblock can have, not counting the core. Frames count.")
          .defineInRange("part_cap", 24, 1, 256);

  public static final ModConfigSpec.IntValue POWER_BLOCK_BUFFER =
      BUILDER
          .comment("How much energy, in RF, each power block holds.")
          .defineInRange("power_block_buffer", 4_000_000, 1, Integer.MAX_VALUE);

  public static final ModConfigSpec.IntValue ME_INTERVAL =
      BUILDER
          .comment(
              "How often, in ticks, the ME block moves export contents into ME and tops up the"
                  + " fertilizer blocks.")
          .defineInRange("me_interval", 20, 1, 1200);

  public static final ModConfigSpec SPEC = BUILDER.build();

  private MPConfig() {}
}
