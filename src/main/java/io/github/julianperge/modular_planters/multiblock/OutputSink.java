package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class OutputSink {
    private final List<ExportBlockEntity> blocks;
    private final @Nullable MeLink me;
    private final Runnable onStale;
    private final ResourceHandler<ItemResource> extractView = new DelegatingResourceHandler<>(
            () -> new CombinedResourceHandler<>(live().stream().map(ExportBlockEntity::externalHandler).toList()));

    public OutputSink(List<ExportBlockEntity> inOrder, @Nullable MeLink me, Runnable onStale) {
        this.blocks = List.copyOf(inOrder);
        this.me = me;
        this.onStale = onStale;
    }

    private List<ExportBlockEntity> live() {
        return LiveBlocks.of(blocks, onStale);
    }

    private @Nullable MeLink liveMe() {
        if (me == null) {
            return null;
        }
        if (me.isRemoved()) {
            onStale.run();
            return null;
        }
        return me;
    }

    public boolean hasRoomFor(List<ItemStack> stacks) {
        try (Transaction tx = Transaction.openRoot()) {
            // ME has no transactions, so stacks that share ME room can both pass, and the part ME refuses later goes to the export blocks or is held in the planter block.
            for (ItemStack stack : stacks) {
                if (!insertInto(stack, tx).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }

    public ItemStack insert(ItemStack stack) {
        return insertInto(stack, null);
    }

    public ResourceHandler<ItemResource> extractView() {
        return extractView;
    }

    // ME cannot join a transaction, so each slot gives up the items inside one, offers them to ME, takes back whatever ME refused, and only then commits, which keeps items from being copied or lost.
    public void moveExportsInto(MeLink me) {
        if (!me.online()) {
            return;
        }
        for (ExportBlockEntity block : live()) {
            ItemStacksResourceHandler storage = block.storage();
            for (int slot = 0; slot < storage.size(); slot++) {
                ItemResource resource = storage.getResource(slot);
                if (resource.isEmpty()) {
                    continue;
                }
                int asked = (int) me.insertItem(resource, storage.getAmountAsInt(slot), true);
                if (asked == 0) {
                    continue;
                }
                try (Transaction tx = Transaction.openRoot()) {
                    int taken = storage.extract(slot, resource, asked, tx);
                    int accepted = (int) me.insertItem(resource, taken, false);
                    storage.insert(slot, resource, taken - accepted, tx);
                    tx.commit();
                }
            }
        }
    }

    public int fillPercent() {
        long stored = 0;
        long capacity = 0;
        for (ExportBlockEntity block : live()) {
            ItemStacksResourceHandler storage = block.storage();
            for (int slot = 0; slot < storage.size(); slot++) {
                stored += storage.getAmountAsLong(slot);
                capacity += storage.getCapacityAsLong(slot, storage.getResource(slot));
            }
        }
        return capacity == 0 ? 0 : (int) (stored * 100 / capacity);
    }

    private ItemStack insertInto(ItemStack stack, @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = ItemResource.of(stack);
        int remaining = stack.getCount();
        MeLink link = liveMe();
        if (link != null && link.online()) {
            remaining -= (int) link.insertItem(resource, remaining, transaction != null);
        }
        for (ExportBlockEntity block : live()) {
            if (remaining == 0) {
                break;
            }
            remaining -= ResourceHandlerUtil.insertStacking(block.storage(), resource, remaining, transaction);
        }
        return remaining == 0 ? ItemStack.EMPTY : resource.toStack(remaining);
    }
}
