package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

public final class PowerPool {
    private final List<PowerBlockEntity> blocks;
    private final EnergyHandler insertView = new InsertView();

    public PowerPool(List<PowerBlockEntity> inOrder) {
        this.blocks = List.copyOf(inOrder);
    }

    public long stored() {
        long total = 0;
        for (PowerBlockEntity block : blocks) {
            total += block.stored();
        }
        return total;
    }

    public long capacity() {
        long total = 0;
        for (PowerBlockEntity block : blocks) {
            total += block.capacity();
        }
        return total;
    }

    public boolean tryTake(long amount) {
        if (amount < 0 || stored() < amount) {
            return false;
        }
        long remaining = amount;
        for (PowerBlockEntity block : blocks) {
            int taken = (int) Math.min(remaining, block.stored());
            if (taken > 0) {
                block.setStored(block.stored() - taken);
                remaining -= taken;
            }
        }
        return true;
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
            for (PowerBlockEntity block : blocks) {
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
