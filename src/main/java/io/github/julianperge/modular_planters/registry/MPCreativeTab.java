package io.github.julianperge.modular_planters.registry;

import io.github.julianperge.modular_planters.ModularPlanters;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularPlanters.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MODULAR_PLANTERS = TABS.register(
            ModularPlanters.MOD_ID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modular_planters"))
                    .icon(() -> new ItemStack(MPItems.CORE.get()))
                    .displayItems(MPItems.ITEMS.getEntries())
                    .build());

    private MPCreativeTab() {
    }
}
