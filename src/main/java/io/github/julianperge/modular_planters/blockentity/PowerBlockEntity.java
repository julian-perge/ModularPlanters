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
        // Energy already above a lowered cap stays and can be drawn down, but nothing can raise it above the larger of the cap and the current amount.
        this.stored = Math.max(0, Math.min(stored, Math.max(capacity(), this.stored)));
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
        stored = Math.max(0, input.getIntOr("stored", 0));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(MPDataComponents.STORED_ENERGY.get(), stored);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        stored = Math.max(0, components.getOrDefault(MPDataComponents.STORED_ENERGY.get(), 0));
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
            int accepted = Math.min(amount, Math.max(0, capacity() - stored));
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
