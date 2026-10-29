package io.github.julianperge.modular_planters.blockentity;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.logic.PartType;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.logic.ScanResult;
import io.github.julianperge.modular_planters.logic.StructureScanner;
import io.github.julianperge.modular_planters.multiblock.Multiblock;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

public class CoreBlockEntity extends BlockEntity {
    private static final int RESCAN_INTERVAL = 20;

    private ScanResult<BlockPos> lastScan = new ScanResult<>(Problem.WAITING_FOR_CHUNKS, null, Map.of());
    private @Nullable Multiblock multiblock;
    private boolean dirty = true;

    public CoreBlockEntity(BlockPos pos, BlockState state) {
        super(MPBlockEntities.CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreBlockEntity core) {
        if (core.dirty || level.getGameTime() % RESCAN_INTERVAL == 0) {
            core.scan(level);
        }
    }

    public void markDirty() {
        dirty = true;
    }

    public ScanResult<BlockPos> lastScan() {
        return lastScan;
    }

    public @Nullable Multiblock multiblock() {
        return multiblock;
    }

    public boolean allPartsLoaded() {
        return level != null && (multiblock == null || multiblock.all().stream().allMatch(level::isLoaded));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        dirty = true;
    }

    private void scan(Level level) {
        dirty = false;
        ScanResult<BlockPos> result = StructureScanner.scan(worldPosition,
                pos -> Arrays.stream(Direction.values()).map(pos::relative).toList(),
                pos -> typeAt(level, pos), MPConfig.PART_CAP.get());
        if (result.equals(lastScan)) {
            return;
        }
        lastScan = result;
        multiblock = result.formed() ? Multiblock.from(result) : null;
        level.invalidateCapabilities(worldPosition);
        setChanged();
    }

    private static PartType typeAt(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return PartType.UNLOADED;
        }
        return level.getBlockState(pos).getBlock() instanceof PartBlock part ? part.partType() : PartType.NOT_A_PART;
    }
}
