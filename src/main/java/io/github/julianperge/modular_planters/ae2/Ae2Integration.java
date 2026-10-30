package io.github.julianperge.modular_planters.ae2;

import appeng.api.AECapabilities;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class Ae2Integration {
    public static final DeferredBlock<MeBlock> ME_BLOCK =
            MPBlocks.BLOCKS.registerBlock("me", MeBlock::new, MPBlocks::metal);
    public static final DeferredItem<BlockItem> ME_ITEM = MPItems.ITEMS.registerSimpleBlockItem(ME_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MeBlockEntity>> ME_BLOCK_ENTITY =
            MPBlockEntities.TYPES.register("me", () -> new BlockEntityType<>(MeBlockEntity::new, ME_BLOCK.get()));

    private Ae2Integration() {
    }

    // Calling this loads the class, and loading it adds the ME block, its item and its block entity to the mod's registers through the static fields above.
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(Ae2Integration::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ME_BLOCK_ENTITY.get(), (be, side) -> be);
    }
}
