package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public final class PowerPool {
    private final List<PowerBlockEntity> blocks;
    private final Runnable onStale;
    private final EnergyHandler insertView = new InsertView();

    public PowerPool(List<PowerBlockEntity> inOrder, Runnable onStale) {
        this.blocks = List.copyOf(inOrder);
        this.onStale = onStale;
    }

    private List<PowerBlockEntity> live() {
        return LiveBlocks.of(blocks, onStale);
    }

    public long stored() {
        long total = 0;
        for (PowerBlockEntity block : live()) {
            total += block.stored();
        }
        return total;
    }

    public long capacity() {
        long total = 0;
        for (PowerBlockEntity block : live()) {
            total += block.capacity();
        }
        return total;
    }

    public boolean tryTake(long amount) {
        if (amount < 0 || stored() < amount) {
            return false;
        }
        long remaining = amount;
        for (PowerBlockEntity block : live()) {
            int taken = (int) Math.min(remaining, block.stored());
            if (taken > 0) {
                block.setStored(block.stored() - taken);
                remaining -= taken;
            }
        }
        return true;
    }

    public long refillFrom(MeLink me) {
        if (!me.online()) {
            return 0;
        }
        List<PowerBlockEntity> live = live();
        long room = 0;
        // A power block can hold more than its capacity after the config lowers the cap, so each block's free room is floored at zero here and below.
        for (PowerBlockEntity block : live) {
            room += Math.max(0, block.capacity() - block.stored());
        }
        if (room == 0) {
            return 0;
        }
        long moved = me.extractEnergy(room, false);
        long remaining = moved;
        for (PowerBlockEntity block : live) {
            int added = (int) Math.min(remaining, Math.max(0, block.capacity() - block.stored()));
            if (added > 0) {
                block.setStored(block.stored() + added);
                remaining -= added;
            }
        }
        return moved;
    }

    public EnergyHandler insertView() {
        return insertView;
    }

    private final class InsertView implements EnergyHandler {
        @Override
        public long getAmountAsLong() {
            return stored();
        }

        @Override
        public long getCapacityAsLong() {
            return capacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            int inserted = 0;
            for (PowerBlockEntity block : live()) {
                if (inserted == amount) {
                    break;
                }
                inserted += block.externalHandler().insert(amount - inserted, transaction);
            }
            return inserted;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
