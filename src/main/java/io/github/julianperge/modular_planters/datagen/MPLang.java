package io.github.julianperge.modular_planters.datagen;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MPLang extends LanguageProvider {
    public MPLang(PackOutput output) {
        super(output, ModularPlanters.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.modular_planters", "Modular Planters");
        addBlock(MPBlocks.CORE, "Modular Planter Core");
        addBlock(MPBlocks.FRAME, "Modular Planter Frame");
        addBlock(MPBlocks.POWER, "Modular Planter Power Block");
        addBlock(MPBlocks.FERTILIZER, "Modular Planter Fertilizer Block");
        addBlock(MPBlocks.EXPORT, "Modular Planter Export Block");
        add("container.modular_planters.fertilizer", "Fertilizer Storage");
        add("container.modular_planters.export", "Export Storage");
        addBlock(MPBlocks.PLANTER, "Modular Planter Block");
        add("container.modular_planters.planter", "Modular Planter");
        add("modular_planters.status.idle", "Idle");
        add("modular_planters.status.growing", "Growing");
        add("modular_planters.status.short_on_soil", "Short on soil");
        add("modular_planters.status.paused_for_power", "Paused: not enough power");
        add("modular_planters.status.paused_export_full", "Paused: export full");
        add("modular_planters.screen.working_idle", "%s working, %s idle");
        add("modular_planters.screen.combo", "%s x %s");
        add("modular_planters.screen.upgraded", "%s/%s seeds upgraded");
    }
}
