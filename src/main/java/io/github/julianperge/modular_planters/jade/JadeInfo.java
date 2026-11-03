package io.github.julianperge.modular_planters.jade;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

// This class imports nothing from Jade, so the mod works without it and the GameTests cover the
// lines.
public final class JadeInfo {
  // Jade merges every mod's server data into one tag per block, so the keys carry the mod id as a
  // prefix.
  private static final String PREFIX = "modular_planters:";
  private static final String KEY_FORMED = PREFIX + "formed";
  private static final String KEY_PARTS = PREFIX + "parts";
  private static final String KEY_CAP = PREFIX + "cap";
  private static final String KEY_PROBLEM = PREFIX + "problem";
  private static final String KEY_STATUS = PREFIX + "status";
  private static final String KEY_GROWING = PREFIX + "growing";
  private static final String KEY_SEEDS = PREFIX + "seeds";

  private JadeInfo() {}

  public static CompoundTag core(CoreBlockEntity core) {
    CompoundTag data = new CompoundTag();
    data.putBoolean(KEY_FORMED, core.lastScan().formed());
    data.putInt(KEY_PARTS, core.lastScan().partCount());
    data.putInt(KEY_CAP, MPConfig.PART_CAP.get());
    data.putString(KEY_PROBLEM, core.lastScan().problem().name());
    return data;
  }

  public static List<Component> coreLines(CompoundTag data) {
    if (data.getBoolean(KEY_FORMED).isEmpty()) {
      return List.of();
    }
    if (data.getBooleanOr(KEY_FORMED, false)) {
      return List.of(
          Component.translatable(
              "modular_planters.jade.formed",
              data.getIntOr(KEY_PARTS, 0),
              data.getIntOr(KEY_CAP, 0)));
    }
    return List.of(
        Component.translatable(
            "modular_planters.jade.not_formed",
            Component.translatable(
                "modular_planters.problem."
                    + data.getStringOr(KEY_PROBLEM, "").toLowerCase(Locale.ROOT))));
  }

  public static CompoundTag planter(PlanterBlockEntity planter) {
    CompoundTag data = new CompoundTag();
    data.putString(KEY_STATUS, planter.status().name());
    data.putInt(KEY_GROWING, planter.growing());
    data.putInt(KEY_SEEDS, planter.seeds().size());
    return data;
  }

  public static List<Component> planterLines(CompoundTag data) {
    if (data.getString(KEY_STATUS).isEmpty()) {
      return List.of();
    }
    return List.of(
        Component.translatable(
            "modular_planters.status." + data.getStringOr(KEY_STATUS, "").toLowerCase(Locale.ROOT)),
        Component.translatable(
            "modular_planters.jade.growing",
            data.getIntOr(KEY_GROWING, 0),
            data.getIntOr(KEY_SEEDS, 0)));
  }
}
