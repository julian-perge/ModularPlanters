package io.github.julianperge.modular_planters.menu;

import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.logic.SeedBin;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Shows the whole seed bin as one stack, but the bin is the only store, so every change goes through insert and takeRun.
// PlanterMenu handles the click types that would otherwise copy the shown stack out or swap it, because vanilla would duplicate or void seeds there.
// Creative-mode clone still copies the shown stack onto the cursor, as it does for any slot, and leaves the bin unchanged.
public final class SeedBinSlot extends Slot {
    private final PlanterBlockEntity planter;
    private final Level level;
    private List<ItemStack> shown = List.of();

    public SeedBinSlot(PlanterBlockEntity planter, Level level, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
        this.planter = planter;
        this.level = level;
    }

    // Only the server holds the real bin. The client shows the combos the server last sent and never changes seeds itself.
    public boolean authoritative() {
        return !level.isClientSide();
    }

    public void show(List<ItemStack> combos) {
        shown = List.copyOf(combos);
    }

    public List<ItemStack> combos() {
        if (!authoritative()) {
            return shown;
        }
        List<ItemStack> combos = new ArrayList<>();
        for (SeedBin.Combo<ItemStack> combo : planter.seeds().combos()) {
            combos.add(combo.seed().copyWithCount(combo.count()));
        }
        return combos;
    }

    public int seedCount() {
        int total = 0;
        for (ItemStack combo : combos()) {
            total += combo.getCount();
        }
        return total;
    }

    @Override
    public ItemStack getItem() {
        List<ItemStack> combos = combos();
        return combos.isEmpty() ? ItemStack.EMPTY : combos.getFirst().copyWithCount(seedCount());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ItemStack first = getItem();
        if (!first.isEmpty() && !ItemStack.isSameItem(first, stack)) {
            return false;
        }
        if (!(level instanceof ServerLevel server)) {
            return true;
        }
        ItemStack soil = planter.soilAndModules().getResource(PlanterBlockEntity.SOIL)
                .toStack(planter.soilAndModules().getAmountAsInt(PlanterBlockEntity.SOIL));
        return AteBridge.recipe(server, stack)
                .filter(recipe -> soil.isEmpty() || recipe.soilMatches().test(soil))
                .isPresent();
    }

    // Moves as many of amount as fit from stack into the bin and shrinks stack by that many. Returns how many moved.
    public int insert(ItemStack stack, int amount) {
        if (!authoritative() || !mayPlace(stack)) {
            return 0;
        }
        int added = planter.seeds().add(stack, Math.min(amount, stack.getCount()));
        stack.shrink(added);
        if (added > 0) {
            planter.setChanged();
        }
        return added;
    }

    // Removes up to amount seeds from the end of the bin, all with the same components, as one stack.
    public ItemStack takeRun(int amount) {
        SeedBin<ItemStack> bin = planter.seeds();
        if (!authoritative() || bin.size() == 0) {
            return ItemStack.EMPTY;
        }
        int limit = Math.min(amount, bin.seed(bin.size() - 1).getMaxStackSize());
        List<ItemStack> run = bin.takeTail(limit);
        if (run.isEmpty()) {
            return ItemStack.EMPTY;
        }
        planter.setChanged();
        return run.getFirst().copyWithCount(run.size());
    }

    public ItemStack peekTail() {
        SeedBin<ItemStack> bin = planter.seeds();
        return bin.size() == 0 ? ItemStack.EMPTY : bin.seed(bin.size() - 1).copy();
    }

    // No current caller reaches this with a non-empty stack, because quickMoveStack only takes what roomInInventory says fits, and it stays as a guard against losing seeds.
    // Puts seeds that were just taken from the bin back as new seeds without re-checking mayPlace, so a changed recipe or soil cannot make them vanish, though their growth starts over.
    public void giveBack(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.shrink(planter.seeds().add(stack, stack.getCount()));
            planter.setChanged();
        }
    }

    @Override
    public ItemStack remove(int amount) {
        return takeRun(amount);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int amount) {
        insert(stack, amount);
        return stack;
    }

    // Vanilla calls this with an empty stack after a take, which needs no change because the bin was already updated.
    // On the server any other stack throws before anything changes, because ignoring it would void a stack that the caller already took from somewhere else.
    // Ignoring it could also duplicate seeds, because Inventory Essentials' Ctrl+click sets a one-item stack here, shift-clicks the slot, and then gives the player the rest of the shown stack while the bin still holds those seeds.
    // PlanterMenu handles every vanilla click type that could reach this, so only clicks from other mods, such as that Ctrl+click, reach the throw, and the server logs the error and skips that click.
    @Override
    public void set(ItemStack stack) {
        if (!stack.isEmpty() && authoritative()) {
            throw new IllegalStateException("The seed bin slot can only be changed through insert and takeRun");
        }
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return SeedBin.CAPACITY;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return SeedBin.CAPACITY;
    }

    @Override
    public void setChanged() {
        if (authoritative()) {
            planter.setChanged();
        }
    }
}
