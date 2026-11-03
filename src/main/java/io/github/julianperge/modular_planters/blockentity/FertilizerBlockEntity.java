package io.github.julianperge.modular_planters.blockentity;

import io.github.julianperge.modular_planters.ate.AteBridge;
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

public class FertilizerBlockEntity extends BlockEntity implements MenuProvider {
  public static final int SLOTS = 27;

  private final ItemStacksResourceHandler storage = createStorage(this::setChanged);
  private final ResourceHandler<ItemResource> externalHandler =
      new DelegatingResourceHandler<>(storage) {
        @Override
        public int extract(
            int index, ItemResource resource, int amount, TransactionContext transaction) {
          return 0;
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
          return 0;
        }
      };

  public FertilizerBlockEntity(BlockPos pos, BlockState state) {
    super(MPBlockEntities.FERTILIZER.get(), pos, state);
  }

  public static ItemStacksResourceHandler createStorage(Runnable onChanged) {
    return new ItemStacksResourceHandler(SLOTS) {
      @Override
      public boolean isValid(int index, ItemResource resource) {
        return AteBridge.fertilizer(resource) != null;
      }

      @Override
      protected void onContentsChanged(int index, ItemStack previousContents) {
        onChanged.run();
      }
    };
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
    return Component.translatable("container.modular_planters.fertilizer");
  }

  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
    return StorageMenu.fertilizer(id, inventory, this);
  }
}
