package io.github.julianperge.modular_planters.blockentity;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import io.github.julianperge.modular_planters.registry.MPDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PowerBlockEntity extends BlockEntity {
    private int stored;
    private final EnergyHandler externalHandler = new ExternalHandler();

    public PowerBlockEntity(BlockPos pos, BlockState state) {
        super(MPBlockEntities.POWER.get(), pos, state);
    }

    public int stored() {
        return stored;
    }

    public void setStored(int stored) {
        this.stored = Math.max(0, Math.min(stored, capacity()));
        setChanged();
    }

    public int capacity() {
        return MPConfig.POWER_BLOCK_BUFFER.get();
    }

    public EnergyHandler externalHandler() {
        return externalHandler;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("stored", stored);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        stored = Math.min(input.getIntOr("stored", 0), capacity());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(MPDataComponents.STORED_ENERGY.get(), stored);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        stored = Math.min(components.getOrDefault(MPDataComponents.STORED_ENERGY.get(), 0), capacity());
    }

    private final class ExternalHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        @Override
        protected Integer createSnapshot() {
            return stored;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            stored = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            setChanged();
        }

        @Override
        public long getAmountAsLong() {
            return stored;
        }

        @Override
        public long getCapacityAsLong() {
            return capacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            int accepted = Math.min(amount, capacity() - stored);
            if (accepted <= 0) {
                return 0;
            }
            updateSnapshots(transaction);
            stored += accepted;
            return accepted;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
