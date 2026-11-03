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
    add("oracle_index.title.modular_planters", "Modular Planters");
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
    add("modular_planters.screen.more_combos", "and %s more combinations");
    add("modular_planters.screen.upgraded", "%s/%s seeds upgraded");
    add("modular_planters.screen.back_to_core", "Back to core");
    add("container.modular_planters.core", "Modular Planter Core");
    add("modular_planters.screen.core.formed", "FORMED %s / %s");
    add("modular_planters.screen.core.problem", "%s (%s, %s, %s)");
    add("modular_planters.screen.core.power", "POWER");
    add("modular_planters.screen.core.power_value", "%s / %s RF");
    add("modular_planters.screen.core.draw", "-%s RF/t");
    add("modular_planters.screen.core.fertilizer", "FERTILIZER");
    add("modular_planters.screen.core.fertilizer_value", "%s items");
    add("modular_planters.screen.core.export", "EXPORT");
    add("modular_planters.screen.core.export_value", "%s%% full");
    add("modular_planters.screen.core.planters", "PLANTERS");
    add("modular_planters.screen.core.planters_value", "%s blocks");
    add("modular_planters.screen.core.me", "ME");
    add("modular_planters.screen.core.growing", "GROWING");
    add("modular_planters.screen.core.upgrades", "UPGRADES");
    add("modular_planters.screen.core.status", "STATUS");
    add("modular_planters.screen.core.group", "%s (%s)");
    add("modular_planters.screen.core.empty", "Empty");
    add("modular_planters.screen.core.no_planters", "No planter blocks in this structure");
    add("modular_planters.screen.core.highlight", "Highlight in world");
    add("modular_planters.screen.core.open", "Open planter block");
    add("modular_planters.problem.waiting_for_chunks", "Waiting for chunks to load");
    add("modular_planters.problem.second_core", "Second core in the structure");
    add("modular_planters.problem.too_many_parts", "Too many parts");
    add("modular_planters.problem.second_me", "Second ME block in the structure");
    add("modular_planters.problem.missing_power", "Needs a power block");
    add("modular_planters.problem.missing_export", "Needs an export block");
    add("modular_planters.problem.missing_planter", "Needs a planter block");
    add("modular_planters.jade.formed", "Formed: %s / %s parts");
    add("modular_planters.jade.not_formed", "Not formed: %s");
    add("modular_planters.jade.growing", "%s/%s seeds growing");
    add("config.jade.plugin_modular_planters.core", "Modular Planters core");
    add("config.jade.plugin_modular_planters.planter", "Modular Planters planter block");
    add("block.modular_planters.me", "Modular Planter ME Block");
    add("modular_planters.screen.me.filter", "Fertilizer from ME");
    add("modular_planters.screen.me.status", "Status: %s");
    add("modular_planters.me_status.none", "None");
    add("modular_planters.me_status.offline", "Offline");
    add("modular_planters.me_status.no_channel", "No channel");
    add("modular_planters.me_status.online", "Online");
  }
}
