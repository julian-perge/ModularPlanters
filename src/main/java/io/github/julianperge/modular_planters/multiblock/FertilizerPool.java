package io.github.julianperge.modular_planters.multiblock;

import com.misterd.agritechevolved.datamap.FertilizerData;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
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

public final class FertilizerPool {
    private final List<FertilizerBlockEntity> blocks;
    private final Runnable onStale;
    private final ResourceHandler<ItemResource> allBlocks;
    private final ResourceHandler<ItemResource> insertView = new DelegatingResourceHandler<>(this::liveHandler);

    public FertilizerPool(List<FertilizerBlockEntity> inOrder, Runnable onStale) {
        this.blocks = List.copyOf(inOrder);
        this.onStale = onStale;
        this.allBlocks = new CombinedResourceHandler<>(blocks.stream().map(FertilizerBlockEntity::externalHandler).toList());
    }

    private List<FertilizerBlockEntity> live() {
        return LiveBlocks.of(blocks, onStale);
    }

    boolean allLive() {
        return LiveBlocks.allLive(blocks, onStale);
    }

    // A combined handler fixes its slot count when it is built, so a removed block needs a new one until the core rebuilds the pool.
    private ResourceHandler<ItemResource> liveHandler() {
        return allLive() ? allBlocks
                : new CombinedResourceHandler<>(live().stream().map(FertilizerBlockEntity::externalHandler).toList());
    }

    public ItemResource selected() {
        for (FertilizerBlockEntity block : live()) {
            ItemStacksResourceHandler storage = block.storage();
            for (int slot = 0; slot < storage.size(); slot++) {
                ItemResource resource = storage.getResource(slot);
                if (!resource.isEmpty()) {
                    return resource;
                }
            }
        }
        return ItemResource.EMPTY;
    }

    public int available() {
        ItemResource selected = selected();
        if (selected.isEmpty()) {
            return 0;
        }
        long total = 0;
        for (FertilizerBlockEntity block : live()) {
            ItemStacksResourceHandler storage = block.storage();
            for (int slot = 0; slot < storage.size(); slot++) {
                if (storage.getResource(slot).equals(selected)) {
                    total += storage.getAmountAsLong(slot);
                }
            }
        }
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    public @Nullable FertilizerData data() {
        ItemResource selected = selected();
        return selected.isEmpty() ? null : AteBridge.fertilizer(selected);
    }

    public boolean consume(int count) {
        ItemResource selected = selected();
        if (selected.isEmpty() || count < 0) {
            return false;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int remaining = count;
            for (FertilizerBlockEntity block : live()) {
                if (remaining == 0) {
                    break;
                }
                remaining -= block.storage().extract(selected, remaining, tx);
            }
            if (remaining > 0) {
                return false;
            }
            tx.commit();
            return true;
        }
    }

    public ResourceHandler<ItemResource> insertView() {
        return insertView;
    }

    public void topUpFrom(MeLink me) {
        if (!me.online()) {
            return;
        }
        ItemResource filter = me.fertilizerFilter();
        if (AteBridge.fertilizer(filter) == null) {
            return;
        }
        List<FertilizerBlockEntity> live = live();
        int room;
        // The transaction is never committed, so measuring the room leaves the storage as it was.
        try (Transaction tx = Transaction.openRoot()) {
            room = insertInto(live, filter, Integer.MAX_VALUE, tx);
        }
        if (room == 0) {
            return;
        }
        int taken = (int) me.extractItem(filter, room, false);
        if (taken == 0) {
            return;
        }
        int inserted;
        try (Transaction tx = Transaction.openRoot()) {
            inserted = insertInto(live, filter, taken, tx);
            tx.commit();
        }
        // The insert fits because the room was measured just before and the ME call in between adds nothing to these blocks, so returning the rest to ME and dropping what ME refuses at the first block only keeps the items if another mod breaks that.
        if (inserted < taken) {
            int left = taken - inserted - (int) me.insertItem(filter, taken - inserted, false);
            ItemStack lost = filter.toStack(left);
            BlockPos pos = live.getFirst().getBlockPos();
            while (!lost.isEmpty()) {
                Containers.dropItemStack(live.getFirst().getLevel(), pos.getX(), pos.getY(), pos.getZ(),
                        lost.split(lost.getMaxStackSize()));
            }
        }
    }

    private static int insertInto(List<FertilizerBlockEntity> blocks, ItemResource resource, int amount,
                                  TransactionContext transaction) {
        int inserted = 0;
        for (FertilizerBlockEntity block : blocks) {
            if (inserted == amount) {
                break;
            }
            inserted += ResourceHandlerUtil.insertStacking(block.storage(), resource, amount - inserted, transaction);
        }
        return inserted;
    }

    public Budget budget() {
        FertilizerData data = data();
        return new Budget(this, data, data == null ? 0 : available());
    }

    // Every planter block of a core claims from one budget per tick, in the position order the core ticks them, so claims never add up to more than the pool held when the tick started, and a claim takes items only when its group harvests, one per seed.
    public static final class Budget {
        private final FertilizerPool pool;
        private final @Nullable FertilizerData data;
        private int unclaimed;

        private Budget(FertilizerPool pool, @Nullable FertilizerData data, int unclaimed) {
            this.pool = pool;
            this.data = data;
            this.unclaimed = unclaimed;
        }

        public @Nullable FertilizerData claim(int seeds) {
            if (data == null || seeds > unclaimed) {
                return null;
            }
            unclaimed -= seeds;
            return data;
        }

        public void consume(int count) {
            pool.consume(count);
        }
    }
}
