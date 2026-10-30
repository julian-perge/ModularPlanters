package io.github.julianperge.modular_planters.gametest;

import io.github.julianperge.modular_planters.multiblock.MeLink;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import java.util.HashMap;
import java.util.Map;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// Each insert and extract opens and closes a root transaction, as AE2's storage bus does, so a test fails if the code under test calls the link while it holds a transaction open.
public final class FakeMeLink implements MeLink {
    public MeStatus status;
    public boolean removed;
    public ItemResource filter = ItemResource.EMPTY;
    public long energy;
    public long modulateShortfall;

    private final long capacity;
    private final Map<ItemResource, Long> items = new HashMap<>();

    public FakeMeLink(MeStatus status, long capacity) {
        this.status = status;
        this.capacity = capacity;
    }

    public long stored(ItemResource resource) {
        return items.getOrDefault(resource, 0L);
    }

    public void put(ItemResource resource, long amount) {
        items.merge(resource, amount, Long::sum);
    }

    @Override
    public MeStatus status() {
        return status;
    }

    @Override
    public long insertItem(ItemResource resource, long amount, boolean simulate) {
        if (status != MeStatus.ONLINE) {
            return 0;
        }
        try (Transaction ignored = Transaction.openRoot()) {
            long total = items.values().stream().mapToLong(Long::longValue).sum();
            long accepted = Math.min(amount, Math.max(0, capacity - total));
            if (simulate) {
                return accepted;
            }
            long inserted = Math.max(0, accepted - modulateShortfall);
            put(resource, inserted);
            return inserted;
        }
    }

    @Override
    public long extractItem(ItemResource resource, long amount, boolean simulate) {
        if (status != MeStatus.ONLINE) {
            return 0;
        }
        try (Transaction ignored = Transaction.openRoot()) {
            long extracted = Math.min(amount, stored(resource));
            if (!simulate) {
                items.merge(resource, -extracted, Long::sum);
            }
            return extracted;
        }
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        if (status != MeStatus.ONLINE) {
            return 0;
        }
        try (Transaction ignored = Transaction.openRoot()) {
            long extracted = Math.min(amount, energy);
            if (!simulate) {
                energy -= extracted;
            }
            return extracted;
        }
    }

    @Override
    public ItemResource fertilizerFilter() {
        return filter;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }
}
