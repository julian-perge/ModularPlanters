package io.github.julianperge.modular_planters.blockentity;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.logic.PartType;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.logic.ScanResult;
import io.github.julianperge.modular_planters.logic.StructureScanner;
import io.github.julianperge.modular_planters.multiblock.FertilizerPool;
import io.github.julianperge.modular_planters.multiblock.MeLink;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import io.github.julianperge.modular_planters.multiblock.Multiblock;
import io.github.julianperge.modular_planters.multiblock.OutputSink;
import io.github.julianperge.modular_planters.multiblock.PowerPool;
import io.github.julianperge.modular_planters.multiblock.TickContext;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CoreBlockEntity extends BlockEntity {
    private static final int RESCAN_INTERVAL = 20;

    private ScanResult<BlockPos> lastScan = new ScanResult<>(Problem.WAITING_FOR_CHUNKS, null, Map.of());
    private @Nullable Multiblock multiblock;
    private @Nullable PowerPool power;
    private @Nullable FertilizerPool fertilizer;
    private @Nullable OutputSink outputs;
    private @Nullable MeLink me;
    private boolean dirty = true;
    private boolean poolsStale;

    public CoreBlockEntity(BlockPos pos, BlockState state) {
        super(MPBlockEntities.CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoreBlockEntity core) {
        if (core.dirty || level.getGameTime() % RESCAN_INTERVAL == 0) {
            core.scan(level);
        }
        if (level instanceof ServerLevel serverLevel) {
            core.tickPlanters(serverLevel);
        }
    }

    // The pools are null while the core waits for an unloaded chunk, and the planters then keep their progress without ticking.
    private void tickPlanters(ServerLevel level) {
        if (multiblock == null || power == null || fertilizer == null || outputs == null || !allPartsLoaded()) {
            return;
        }
        TickContext context = new TickContext(level, level.getRandom(), AteBridge.numbers(), power, fertilizer.budget(), outputs);
        for (BlockPos pos : multiblock.planters()) {
            if (level.getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
                planter.tickFromCore(context);
            }
        }
    }

    public void markDirty() {
        dirty = true;
    }

    // A pooled block was removed, and a replacement at the same position gives an equal scan result, so the next scan must rebuild the pools anyway.
    private void markPoolsStale() {
        dirty = true;
        poolsStale = true;
    }

    public ScanResult<BlockPos> lastScan() {
        return lastScan;
    }

    public @Nullable Multiblock multiblock() {
        return multiblock;
    }

    public @Nullable PowerPool power() {
        return power;
    }

    public @Nullable FertilizerPool fertilizer() {
        return fertilizer;
    }

    public @Nullable OutputSink outputs() {
        return outputs;
    }

    public MeStatus meStatus() {
        return me == null || me.isRemoved() ? MeStatus.NONE : me.status();
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
        boolean rebuild = poolsStale;
        poolsStale = false;
        ScanResult<BlockPos> result = StructureScanner.scan(worldPosition,
                pos -> Arrays.stream(Direction.values()).map(pos::relative).toList(),
                pos -> typeAt(level, pos), MPConfig.PART_CAP.get());
        if (result.equals(lastScan) && !rebuild) {
            return;
        }
        lastScan = result;
        if (result.problem() == Problem.WAITING_FOR_CHUNKS) {
            // The multiblock is kept so a structure with an unloaded part pauses instead of unforming, while the pools are dropped so nothing touches a block entity in an unloaded chunk.
            power = null;
            fertilizer = null;
            outputs = null;
            me = null;
        } else {
            Multiblock previous = multiblock;
            multiblock = result.formed() ? Multiblock.from(result) : null;
            if (previous != null) {
                stopPlanters(level, previous.planters(), multiblock == null ? List.of() : multiblock.planters());
            }
            buildPools(level);
        }
        level.invalidateCapabilities(worldPosition);
        setChanged();
    }

    // No scan runs once the core is gone, so the core stops its planters here, or they would stay ACTIVE and GROWING.
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && multiblock != null) {
            stopPlanters(level, multiblock.planters(), List.of());
        }
    }

    private static void stopPlanters(Level level, List<BlockPos> planters, List<BlockPos> stillFormed) {
        for (BlockPos pos : planters) {
            if (!stillFormed.contains(pos) && level.isLoaded(pos)
                    && level.getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
                planter.stopFromCore();
            }
        }
    }

    private void buildPools(Level level) {
        if (multiblock == null) {
            power = null;
            fertilizer = null;
            outputs = null;
            me = null;
            return;
        }
        power = new PowerPool(blockEntities(level, multiblock.power(), PowerBlockEntity.class), this::markPoolsStale);
        fertilizer = new FertilizerPool(blockEntities(level, multiblock.fertilizer(), FertilizerBlockEntity.class), this::markPoolsStale);
        BlockPos mePos = multiblock.me();
        me = mePos != null && level.getBlockEntity(mePos) instanceof MeLink link ? link : null;
        outputs = new OutputSink(blockEntities(level, multiblock.export(), ExportBlockEntity.class), me, this::markPoolsStale);
    }

    private static <T extends BlockEntity> List<T> blockEntities(Level level, List<BlockPos> positions, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (BlockPos pos : positions) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (type.isInstance(blockEntity)) {
                found.add(type.cast(blockEntity));
            }
        }
        return found;
    }

    private static PartType typeAt(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return PartType.UNLOADED;
        }
        return level.getBlockState(pos).getBlock() instanceof PartBlock part ? part.partType() : PartType.NOT_A_PART;
    }
}
