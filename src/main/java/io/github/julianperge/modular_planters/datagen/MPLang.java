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
        addBlock(MPBlocks.PLANTER, "Modular Planter Block");
    }
}
