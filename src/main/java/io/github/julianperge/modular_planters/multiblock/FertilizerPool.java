package io.github.julianperge.modular_planters.multiblock;

import com.misterd.agritechevolved.datamap.FertilizerData;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FertilizerPool {
    private final List<FertilizerBlockEntity> blocks;
    private final Runnable onStale;
    private final ResourceHandler<ItemResource> insertView = new DelegatingResourceHandler<>(
            () -> new CombinedResourceHandler<>(live().stream().map(FertilizerBlockEntity::externalHandler).toList()));

    public FertilizerPool(List<FertilizerBlockEntity> inOrder, Runnable onStale) {
        this.blocks = List.copyOf(inOrder);
        this.onStale = onStale;
    }

    private List<FertilizerBlockEntity> live() {
        return LiveBlocks.of(blocks, onStale);
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
