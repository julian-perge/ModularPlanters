package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class OutputSink {
    private final List<ExportBlockEntity> blocks;
    private final @Nullable MeLink me;
    private final Runnable onStale;
    private final ResourceHandler<ItemResource> allBlocks;
    private final ResourceHandler<ItemResource> extractView = new DelegatingResourceHandler<>(this::liveHandler);

    public OutputSink(List<ExportBlockEntity> inOrder, @Nullable MeLink me, Runnable onStale) {
        this.blocks = List.copyOf(inOrder);
        this.me = me;
        this.onStale = onStale;
        this.allBlocks = new CombinedResourceHandler<>(blocks.stream().map(ExportBlockEntity::externalHandler).toList());
    }

    private List<ExportBlockEntity> live() {
        return LiveBlocks.of(blocks, onStale);
    }

    boolean allLive() {
        return LiveBlocks.allLive(blocks, onStale);
    }

    // A combined handler fixes its slot count when it is built, so a removed block needs a new one until the core rebuilds the pool.
    private ResourceHandler<ItemResource> liveHandler() {
        return allLive() ? allBlocks
                : new CombinedResourceHandler<>(live().stream().map(ExportBlockEntity::externalHandler).toList());
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
        int[] remaining = new int[stacks.size()];
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            remaining[i] = stack.isEmpty() ? 0 : stack.getCount() - meTakes(ItemResource.of(stack), stack.getCount(), true);
        }
        try (Transaction tx = Transaction.openRoot()) {
            // ME is asked about each stack on its own and keeps nothing, so stacks that share ME room can both pass, and the part ME refuses later goes to the export blocks or is held in the planter block.
            for (int i = 0; i < stacks.size(); i++) {
                if (remaining[i] > 0 && insertIntoBlocks(ItemResource.of(stacks.get(i)), remaining[i], tx) < remaining[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public ItemStack insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = ItemResource.of(stack);
        int remaining = stack.getCount() - meTakes(resource, stack.getCount(), false);
        remaining -= insertIntoBlocks(resource, remaining, null);
        return remaining == 0 ? ItemStack.EMPTY : resource.toStack(remaining);
    }

    public ResourceHandler<ItemResource> extractView() {
        return extractView;
    }

    // ME calls must happen with no transaction open, so each slot gives up what ME says it will take in one committed transaction, offers that to ME, and puts what ME refused back into the same slot in a second one.
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
                int taken;
                try (Transaction tx = Transaction.openRoot()) {
                    taken = storage.extract(slot, resource, asked, tx);
                    tx.commit();
                }
                int refused = taken - (int) me.insertItem(resource, taken, false);
                if (refused == 0) {
                    continue;
                }
                int returned;
                try (Transaction tx = Transaction.openRoot()) {
                    returned = storage.insert(slot, resource, refused, tx);
                    tx.commit();
                }
                // The put-back fits because the slot just gave up at least that much of the same item and nothing else writes to it during the ME call, so dropping the rest at the block only keeps the items if another mod breaks that.
                if (returned < refused) {
                    ItemStack lost = resource.toStack(refused - returned);
                    BlockPos pos = block.getBlockPos();
                    while (!lost.isEmpty()) {
                        Containers.dropItemStack(block.getLevel(), pos.getX(), pos.getY(), pos.getZ(),
                                lost.split(lost.getMaxStackSize()));
                    }
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

    private int meTakes(ItemResource resource, int amount, boolean simulate) {
        MeLink link = liveMe();
        return link != null && link.online() ? (int) link.insertItem(resource, amount, simulate) : 0;
    }

    private int insertIntoBlocks(ItemResource resource, int amount, @Nullable TransactionContext transaction) {
        int inserted = 0;
        for (ExportBlockEntity block : live()) {
            if (inserted == amount) {
                break;
            }
            inserted += ResourceHandlerUtil.insertStacking(block.storage(), resource, amount - inserted, transaction);
        }
        return inserted;
    }
}
