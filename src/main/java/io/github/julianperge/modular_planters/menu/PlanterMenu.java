package io.github.julianperge.modular_planters.menu;

import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.blockentity.CoreBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.network.SeedBinCombosPayload;
import io.github.julianperge.modular_planters.registry.MPBlocks;
import io.github.julianperge.modular_planters.registry.MPMenus;
import java.util.List;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public final class PlanterMenu extends AbstractContainerMenu {
  public static final int SEED = 0, SOIL = 1, UPGRADE_1 = 2, UPGRADE_2 = 3;
  private static final int PLAYER_START = 4;
  private static final int HOTBAR_SIZE = 9;
  private static final int OFFHAND = 40;

  private final PlanterBlockEntity planter;
  private final BlockPos anchor;
  private final boolean fromCore;
  private final Player player;
  private final SeedBinSlot seedSlot;
  private final DataSlot status;
  private final DataSlot growing;
  private final DataSlot upgraded;
  private List<ItemStack> sentCombos = List.of();

  public PlanterMenu(
      int id, Inventory inventory, PlanterBlockEntity planter, BlockPos anchor, boolean fromCore) {
    super(MPMenus.PLANTER.get(), id);
    this.planter = planter;
    this.anchor = anchor;
    this.fromCore = fromCore;
    this.player = inventory.player;
    Level level = inventory.player.level();
    seedSlot = new SeedBinSlot(planter, level, 26, 18);
    addSlot(seedSlot);
    addSlot(new SoilSlot(planter, level, 62, 18));
    addSlot(new UpgradeSlot(planter, PlanterBlockEntity.UPGRADE_1, 98, 18));
    addSlot(new UpgradeSlot(planter, PlanterBlockEntity.UPGRADE_2, 116, 18));
    addStandardInventorySlots(inventory, 8, 85);
    if (seedSlot.authoritative()) {
      status = addDataSlot(readOnly(() -> planter.status().ordinal()));
      growing = addDataSlot(readOnly(planter::growing));
      upgraded = addDataSlot(readOnly(planter::upgraded));
    } else {
      status = addDataSlot(DataSlot.standalone());
      growing = addDataSlot(DataSlot.standalone());
      upgraded = addDataSlot(DataSlot.standalone());
    }
  }

  // The planter here only holds the items the server syncs into the slots, because the real one
  // lives on the server.
  public static PlanterMenu onClient(
      int id, Inventory inventory, BlockPos pos, BlockPos anchor, boolean fromCore) {
    PlanterBlockEntity detached =
        new PlanterBlockEntity(pos, MPBlocks.PLANTER.get().defaultBlockState());
    return new PlanterMenu(id, inventory, detached, anchor, fromCore);
  }

  public static void open(
      ServerPlayer player, PlanterBlockEntity planter, BlockPos anchor, boolean fromCore) {
    player.openMenu(
        new SimpleMenuProvider(
            (id, inventory, opener) -> new PlanterMenu(id, inventory, planter, anchor, fromCore),
            Component.translatable("container.modular_planters.planter")),
        buffer -> {
          buffer.writeBlockPos(planter.getBlockPos());
          buffer.writeBlockPos(anchor);
          buffer.writeBoolean(fromCore);
        });
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

  public BlockPos anchor() {
    return anchor;
  }

  public boolean fromCore() {
    return fromCore;
  }

  public PlanterStatus status() {
    PlanterStatus[] all = PlanterStatus.values();
    return all[Math.clamp(status.get(), 0, all.length - 1)];
  }

  public int growing() {
    return growing.get();
  }

  public int upgraded() {
    return upgraded.get();
  }

  public List<ItemStack> combos() {
    return seedSlot.combos();
  }

  public int seedCount() {
    return seedSlot.seedCount();
  }

  public void showCombos(List<ItemStack> combos) {
    seedSlot.show(combos);
  }

  @Override
  public void clicked(int slotIndex, int button, ContainerInput input, Player clicker) {
    if (slotIndex == SEED && input == ContainerInput.PICKUP && (button == 0 || button == 1)) {
      resetQuickCraft();
      clickSeedSlot(button == 0);
    } else if (slotIndex == SEED
        && input == ContainerInput.SWAP
        && (button >= 0 && button < HOTBAR_SIZE || button == OFFHAND)) {
      swapSeedSlot(clicker, button);
    } else {
      super.clicked(slotIndex, button, input, clicker);
    }
  }

  private void clickSeedSlot(boolean primary) {
    if (!seedSlot.authoritative()) {
      return;
    }
    ItemStack carried = getCarried();
    if (carried.isEmpty()) {
      int count = seedSlot.seedCount();
      setCarried(seedSlot.takeRun(primary ? count : (count + 1) / 2));
    } else {
      seedSlot.insert(carried, primary ? carried.getCount() : 1);
      if (carried.isEmpty()) {
        setCarried(ItemStack.EMPTY);
      }
    }
  }

  private void swapSeedSlot(Player clicker, int hotbarSlot) {
    if (!seedSlot.authoritative()) {
      return;
    }
    ItemStack held = clicker.getInventory().getItem(hotbarSlot);
    if (held.isEmpty()) {
      clicker.getInventory().setItem(hotbarSlot, seedSlot.takeRun(Integer.MAX_VALUE));
    } else {
      seedSlot.insert(held, held.getCount());
      if (held.isEmpty()) {
        clicker.getInventory().setItem(hotbarSlot, ItemStack.EMPTY);
      }
    }
  }

  @Override
  public boolean canDragTo(Slot slot) {
    return slot != seedSlot;
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
    return target != seedSlot;
  }

  @Override
  public ItemStack quickMoveStack(Player clicker, int index) {
    if (index == SEED) {
      return quickMoveSeeds();
    }
    Slot slot = slots.get(index);
    if (!slot.hasItem()) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = slot.getItem();
    ItemStack original = stack.copy();
    if (index < PLAYER_START) {
      moveItemStackTo(stack, PLAYER_START, slots.size(), true);
    } else {
      seedSlot.insert(stack, stack.getCount());
      if (!stack.isEmpty() && AteBridge.isUpgrade(stack)) {
        moveItemStackTo(stack, UPGRADE_1, UPGRADE_2 + 1, false);
      }
      // Only the server moves items into the soil slot, because the client has no recipes to tell
      // soil from seeds and would show seeds landing there until the server corrects it.
      if (!stack.isEmpty() && seedSlot.authoritative() && slots.get(SOIL).mayPlace(stack)) {
        moveItemStackTo(stack, SOIL, SOIL + 1, false);
      }
    }
    if (stack.getCount() == original.getCount()) {
      return ItemStack.EMPTY;
    }
    if (stack.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }
    return original;
  }

  // Only as many seeds as the inventory has room for are taken, so the seeds that stay in the bin
  // keep their growth progress.
  private ItemStack quickMoveSeeds() {
    ItemStack tail = seedSlot.peekTail();
    int room = roomInInventory(tail);
    if (tail.isEmpty() || room == 0) {
      return ItemStack.EMPTY;
    }
    ItemStack run = seedSlot.takeRun(room);
    ItemStack original = run.copy();
    moveItemStackTo(run, PLAYER_START, slots.size(), true);
    seedSlot.giveBack(run);
    return original;
  }

  private int roomInInventory(ItemStack stack) {
    int room = 0;
    for (int i = PLAYER_START; i < slots.size() && !stack.isEmpty(); i++) {
      Slot slot = slots.get(i);
      ItemStack held = slot.getItem();
      if (held.isEmpty()) {
        room += slot.mayPlace(stack) ? slot.getMaxStackSize(stack) : 0;
      } else if (ItemStack.isSameItemSameComponents(held, stack)) {
        room += Math.max(0, slot.getMaxStackSize(held) - held.getCount());
      }
    }
    return room;
  }

  @Override
  public void sendAllDataToRemote() {
    super.sendAllDataToRemote();
    sendCombos(true);
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    sendCombos(false);
  }

  private void sendCombos(boolean force) {
    if (!(player instanceof ServerPlayer serverPlayer)) {
      return;
    }
    List<ItemStack> current = seedSlot.combos();
    if (force || !sameCombos(current, sentCombos)) {
      sentCombos = current;
      PacketDistributor.sendToPlayer(serverPlayer, new SeedBinCombosPayload(containerId, current));
    }
  }

  private static boolean sameCombos(List<ItemStack> left, List<ItemStack> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      if (!ItemStack.matches(left.get(i), right.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean stillValid(Player clicker) {
    if (planter.isRemoved() || !clicker.isWithinBlockInteractionRange(anchor, 4.0)) {
      return false;
    }
    return !fromCore || stillInCoreStructure();
  }

  private boolean stillInCoreStructure() {
    Level level = planter.getLevel();
    return level != null
        && level.getBlockEntity(anchor) instanceof CoreBlockEntity core
        && core.ownsPlanter(planter.getBlockPos());
  }

  private static final class SoilSlot extends ResourceHandlerSlot {
    private final PlanterBlockEntity planter;
    private final Level level;

    SoilSlot(PlanterBlockEntity planter, Level level, int x, int y) {
      super(
          planter.soilAndUpgrades(), planter.soilAndUpgrades()::set, PlanterBlockEntity.SOIL, x, y);
      this.planter = planter;
      this.level = level;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      if (!super.mayPlace(stack)) {
        return false;
      }
      if (!(level instanceof ServerLevel server)) {
        return true;
      }
      if (!AteBridge.isSoilForAnyRecipe(server, stack)) {
        return false;
      }
      if (planter.seeds().size() == 0) {
        return true;
      }
      return AteBridge.recipe(server, planter.seeds().seed(0))
          .map(recipe -> recipe.soilMatches().test(stack))
          .orElse(true);
    }
  }

  private static final class UpgradeSlot extends ResourceHandlerSlot {
    UpgradeSlot(PlanterBlockEntity planter, int handlerSlot, int x, int y) {
      super(planter.soilAndUpgrades(), planter.soilAndUpgrades()::set, handlerSlot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      return super.mayPlace(stack) && AteBridge.isUpgrade(stack);
    }
  }
}
