package io.github.julianperge.modular_planters.menu;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public final class StorageMenu extends AbstractContainerMenu {
    private static final int COLUMNS = 9;

    private final int containerSlots;
    private final int rows;
    private final Block block;
    private final ContainerLevelAccess access;

    private StorageMenu(MenuType<StorageMenu> type, int id, Inventory inventory, ItemStacksResourceHandler storage,
                        boolean takeOnly, Block block, ContainerLevelAccess access) {
        super(type, id);
        this.containerSlots = storage.size();
        this.rows = containerSlots / COLUMNS;
        this.block = block;
        this.access = access;
        for (int index = 0; index < containerSlots; index++) {
            int x = 8 + index % COLUMNS * 18;
            int y = 18 + index / COLUMNS * 18;
            addSlot(takeOnly ? new TakeOnlySlot(storage, index, x, y) : new ResourceHandlerSlot(storage, storage::set, index, x, y));
        }
        addStandardInventorySlots(inventory, 8, rows * 18 + 31);
    }

    public static StorageMenu fertilizer(int id, Inventory inventory, FertilizerBlockEntity entity) {
        return new StorageMenu(MPMenus.FERTILIZER.get(), id, inventory, entity.storage(), false,
                MPBlocks.FERTILIZER.get(), ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos()));
    }

    public static StorageMenu export(int id, Inventory inventory, ExportBlockEntity entity) {
        return new StorageMenu(MPMenus.EXPORT.get(), id, inventory, entity.storage(), true,
                MPBlocks.EXPORT.get(), ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos()));
    }

    public static StorageMenu fertilizerOnClient(int id, Inventory inventory) {
        return new StorageMenu(MPMenus.FERTILIZER.get(), id, inventory,
                FertilizerBlockEntity.createStorage(() -> { }), false, MPBlocks.FERTILIZER.get(), ContainerLevelAccess.NULL);
    }

    public static StorageMenu exportOnClient(int id, Inventory inventory) {
        return new StorageMenu(MPMenus.EXPORT.get(), id, inventory,
                new ItemStacksResourceHandler(ExportBlockEntity.SLOTS), true, MPBlocks.EXPORT.get(), ContainerLevelAccess.NULL);
    }

    public int rows() {
        return rows;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < containerSlots
                ? moveItemStackTo(stack, containerSlots, slots.size(), true)
                : moveItemStackTo(stack, 0, containerSlots, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, block);
    }

    private static final class TakeOnlySlot extends ResourceHandlerSlot {
        TakeOnlySlot(ItemStacksResourceHandler storage, int index, int x, int y) {
            super(storage, storage::set, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
