package io.github.julianperge.modular_planters.blockentity;

import io.github.julianperge.modular_planters.menu.StorageMenu;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ExportBlockEntity extends BlockEntity implements MenuProvider {
  public static final int SLOTS = 54;

  private final ItemStacksResourceHandler storage =
      new ItemStacksResourceHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
          setChanged();
        }
      };
  private final ResourceHandler<ItemResource> externalHandler =
      new DelegatingResourceHandler<>(storage) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
          return false;
        }

        @Override
        public int insert(
            int index, ItemResource resource, int amount, TransactionContext transaction) {
          return 0;
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
          return 0;
        }
      };

  public ExportBlockEntity(BlockPos pos, BlockState state) {
    super(MPBlockEntities.EXPORT.get(), pos, state);
  }

  public ItemStacksResourceHandler storage() {
    return storage;
  }

  public ResourceHandler<ItemResource> externalHandler() {
    return externalHandler;
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level != null) {
      NonNullList<ItemStack> contents = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
      for (int slot = 0; slot < SLOTS; slot++) {
        contents.set(slot, storage.getResource(slot).toStack(storage.getAmountAsInt(slot)));
      }
      Containers.dropContents(level, pos, contents);
      for (int slot = 0; slot < SLOTS; slot++) {
        storage.set(slot, ItemResource.EMPTY, 0);
      }
    }
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    storage.serialize(output);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    storage.deserialize(input);
  }

  @Override
  public Component getDisplayName() {
    return Component.translatable("container.modular_planters.export");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return StorageMenu.export(id, inventory, this);
  }
}
