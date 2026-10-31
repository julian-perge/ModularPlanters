package io.github.julianperge.modular_planters.jade;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import java.util.List;
import java.util.Locale;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

// This class imports nothing from Jade, so the mod works without it and the GameTests cover the lines.
public final class JadeInfo {
    private JadeInfo() {
    }

    public static CompoundTag core(CoreBlockEntity core) {
        CompoundTag data = new CompoundTag();
        data.putBoolean("formed", core.lastScan().formed());
        data.putInt("parts", core.lastScan().partCount());
        data.putInt("cap", MPConfig.PART_CAP.get());
        data.putString("problem", core.lastScan().problem().name());
        return data;
    }

    public static List<Component> coreLines(CompoundTag data) {
        if (data.getBoolean("formed").isEmpty()) {
            return List.of();
        }
        if (data.getBooleanOr("formed", false)) {
            return List.of(Component.translatable("modular_planters.jade.formed",
                    data.getIntOr("parts", 0), data.getIntOr("cap", 0)));
        }
        return List.of(Component.translatable("modular_planters.jade.not_formed",
                Component.translatable("modular_planters.problem." + data.getStringOr("problem", "").toLowerCase(Locale.ROOT))));
    }

    public static CompoundTag planter(PlanterBlockEntity planter) {
        CompoundTag data = new CompoundTag();
        data.putString("status", planter.status().name());
        data.putInt("growing", planter.growing());
        data.putInt("seeds", planter.seeds().size());
        return data;
    }

    public static List<Component> planterLines(CompoundTag data) {
        if (data.getString("status").isEmpty()) {
            return List.of();
        }
        return List.of(
                Component.translatable("modular_planters.status." + data.getStringOr("status", "").toLowerCase(Locale.ROOT)),
                Component.translatable("modular_planters.jade.growing", data.getIntOr("growing", 0), data.getIntOr("seeds", 0)));
    }
}
