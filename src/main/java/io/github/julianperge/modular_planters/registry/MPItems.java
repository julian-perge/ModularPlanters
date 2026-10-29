package io.github.julianperge.modular_planters.registry;

import io.github.julianperge.modular_planters.ModularPlanters;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MPItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModularPlanters.MOD_ID);

    public static final DeferredItem<BlockItem> CORE = ITEMS.registerSimpleBlockItem(MPBlocks.CORE);
    public static final DeferredItem<BlockItem> FRAME = ITEMS.registerSimpleBlockItem(MPBlocks.FRAME);
    public static final DeferredItem<BlockItem> POWER = ITEMS.registerSimpleBlockItem(MPBlocks.POWER);
    public static final DeferredItem<BlockItem> FERTILIZER = ITEMS.registerSimpleBlockItem(MPBlocks.FERTILIZER);
    public static final DeferredItem<BlockItem> EXPORT = ITEMS.registerSimpleBlockItem(MPBlocks.EXPORT);
    public static final DeferredItem<BlockItem> PLANTER = ITEMS.registerSimpleBlockItem(MPBlocks.PLANTER);

    private MPItems() {
    }
}
