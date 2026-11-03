package io.github.julianperge.modular_planters.ae2;

import io.github.julianperge.modular_planters.multiblock.MeStatus;
import java.util.function.IntSupplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class MeMenu extends AbstractContainerMenu {
  private static final int FILTER = 0;

  private final @Nullable MeBlockEntity me;
  private final ContainerLevelAccess access;
  private final DataSlot status;

  public MeMenu(int id, Inventory inventory, MeBlockEntity me) {
    this(id, inventory, me, ContainerLevelAccess.create(me.getLevel(), me.getBlockPos()));
  }

  public MeMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
    this(
        id,
        inventory,
        null,
        ContainerLevelAccess.create(inventory.player.level(), buffer.readBlockPos()));
  }

  private MeMenu(
      int id, Inventory inventory, @Nullable MeBlockEntity me, ContainerLevelAccess access) {
    super(Ae2Integration.ME_MENU.get(), id);
    this.me = me;
    this.access = access;
    addSlot(new FilterSlot(me, 8, 18));
    addStandardInventorySlots(inventory, 8, 67);
    status =
        addDataSlot(me == null ? DataSlot.standalone() : readOnly(() -> me.status().ordinal()));
  }

  private static DataSlot readOnly(IntSupplier value) {
    return new DataSlot() {
      @Override
      public int get() {
        return value.getAsInt();
      }

      @Override
      public void set(int ignored) {}
    };
  }

  public MeStatus status() {
    MeStatus[] all = MeStatus.values();
    return all[Math.clamp(status.get(), 0, all.length - 1)];
  }

  // A click on the ghost filter slot never moves an item: a fertilizer on the cursor becomes the
  // filter, an empty cursor clears it, and any other item changes nothing.
  // The client menu has no block entity, so it ignores the click and shows the filter that the
  // server sends back.
  @Override
  public void clicked(int slotIndex, int button, ContainerInput input, Player clicker) {
    if (slotIndex != FILTER) {
      super.clicked(slotIndex, button, input, clicker);
      return;
    }
    resetQuickCraft();
    if (me != null) {
      me.setFertilizerFilter(ItemResource.of(getCarried()));
    }
  }

  @Override
  public ItemStack quickMoveStack(Player clicker, int index) {
    ItemStack stack = slots.get(index).getItem();
    if (me != null && !stack.isEmpty()) {
      me.setFertilizerFilter(ItemResource.of(stack));
    }
    return ItemStack.EMPTY;
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return slot.index != FILTER;
  }

  @Override
  public boolean stillValid(Player clicker) {
    return stillValid(access, clicker, Ae2Integration.ME_BLOCK.get());
  }

  // The slot shows a one-item copy of the filter on the server, and holds whatever the server last
  // sent on the client.
  private static final class FilterSlot extends Slot {
    private final @Nullable MeBlockEntity me;

    FilterSlot(@Nullable MeBlockEntity me, int x, int y) {
      super(new SimpleContainer(1), 0, x, y);
      this.me = me;
    }

    @Override
    public ItemStack getItem() {
      return me == null ? super.getItem() : me.fertilizerFilter().toStack(1);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      return false;
    }

    @Override
    public boolean mayPickup(Player player) {
      return false;
    }
  }
}
