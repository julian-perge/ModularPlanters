package io.github.julianperge.modular_planters.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.StorageHelper;
import io.github.julianperge.modular_planters.multiblock.MeLink;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class MeBlockEntity extends BlockEntity implements MeLink, IInWorldGridNodeHost, IActionHost {
    private final IManagedGridNode mainNode = GridHelper.createManagedNode(this,
                    (MeBlockEntity owner, IGridNode node) -> owner.setChanged())
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setInWorldNode(true)
            .setIdlePowerUsage(1.0)
            .setVisualRepresentation(Ae2Integration.ME_ITEM.get());
    private final IActionSource source = IActionSource.ofMachine(this);

    public MeBlockEntity(BlockPos pos, BlockState state) {
        super(Ae2Integration.ME_BLOCK_ENTITY.get(), pos, state);
    }

    public IManagedGridNode mainNode() {
        return mainNode;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        GridHelper.onFirstTick(this, be -> be.mainNode().create(be.getLevel(), be.getBlockPos()));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        mainNode.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        mainNode.deserialize(input);
    }

    @Override
    public @Nullable IGridNode getGridNode(Direction dir) {
        return mainNode.getNode();
    }

    @Override
    public @Nullable IGridNode getActionableNode() {
        return mainNode.getNode();
    }

    @Override
    public MeStatus status() {
        if (!mainNode.isPowered()) {
            return MeStatus.OFFLINE;
        }
        return mainNode.isActive() ? MeStatus.ONLINE : MeStatus.NO_CHANNEL;
    }

    @Override
    public long insertItem(ItemResource resource, long amount, boolean simulate) {
        AEItemKey key = AEItemKey.of(resource);
        IGrid grid = mainNode.getGrid();
        if (key == null || grid == null || !mainNode.isActive()) {
            return 0;
        }
        return Math.min(amount, StorageHelper.poweredInsert(grid.getEnergyService(),
                grid.getStorageService().getInventory(), key, amount, source,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE));
    }

    @Override
    public long extractItem(ItemResource resource, long amount, boolean simulate) {
        AEItemKey key = AEItemKey.of(resource);
        IGrid grid = mainNode.getGrid();
        if (key == null || grid == null || !mainNode.isActive()) {
            return 0;
        }
        return Math.min(amount, StorageHelper.poweredExtraction(grid.getEnergyService(),
                grid.getStorageService().getInventory(), key, amount, source,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE));
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        IGrid grid = mainNode.getGrid();
        if (!Ae2Integration.APPLIED_FLUX || grid == null || !mainNode.isActive()) {
            return 0;
        }
        return Math.min(amount, FluxBridge.extractFe(grid.getStorageService().getInventory(), amount,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE, source));
    }

    @Override
    public ItemResource fertilizerFilter() {
        return ItemResource.EMPTY;
    }
}
