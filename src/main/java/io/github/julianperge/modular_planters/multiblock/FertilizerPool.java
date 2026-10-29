package io.github.julianperge.modular_planters.multiblock;

import com.misterd.agritechevolved.datamap.FertilizerData;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FertilizerPool {
    private final List<FertilizerBlockEntity> blocks;
    private final ResourceHandler<ItemResource> insertView;

    public FertilizerPool(List<FertilizerBlockEntity> inOrder) {
        this.blocks = List.copyOf(inOrder);
        this.insertView = new CombinedResourceHandler<>(
                blocks.stream().map(FertilizerBlockEntity::externalHandler).toList());
    }

    public ItemResource selected() {
        for (FertilizerBlockEntity block : blocks) {
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
        for (FertilizerBlockEntity block : blocks) {
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
            for (FertilizerBlockEntity block : blocks) {
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
}
