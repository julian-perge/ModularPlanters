package io.github.julianperge.modular_planters.blockentity;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.block.PartBlock;
import io.github.julianperge.modular_planters.logic.PartType;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.logic.ScanResult;
import io.github.julianperge.modular_planters.logic.StructureScanner;
import io.github.julianperge.modular_planters.multiblock.LiveMultiblock;
import io.github.julianperge.modular_planters.multiblock.Multiblock;
import io.github.julianperge.modular_planters.registry.MPBlockEntities;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public class CoreBlockEntity extends BlockEntity {
  private static final int RESCAN_INTERVAL = 20;

  private ScanResult<BlockPos> lastScan =
      new ScanResult<>(Problem.WAITING_FOR_CHUNKS, null, Map.of());
  private @Nullable LiveMultiblock live;
  private boolean dirty = true;
  private boolean poolsStale;

  public CoreBlockEntity(BlockPos pos, BlockState state) {
    super(MPBlockEntities.CORE.get(), pos, state);
  }

  public static void serverTick(Level level, BlockPos pos, BlockState state, CoreBlockEntity core) {
    if (core.dirty || (level.getGameTime() + core.tickOffset()) % RESCAN_INTERVAL == 0) {
      core.scan(level);
    }
    if (core.live != null && level instanceof ServerLevel serverLevel) {
      core.live.tick(serverLevel, serverLevel.getGameTime() + core.tickOffset());
    }
  }

  // Cores spread their rescans and ME transfers over different ticks by position, so many cores
  // don't all work on the same tick.
  private int tickOffset() {
    return Math.floorMod(worldPosition.hashCode(), RESCAN_INTERVAL);
  }

  public void markDirty() {
    dirty = true;
  }

  // A pooled block was removed, and a replacement at the same position gives an equal scan result,
  // so the next scan must rebuild the pools anyway.
  private void markPoolsStale() {
    dirty = true;
    poolsStale = true;
  }

  public ScanResult<BlockPos> lastScan() {
    return lastScan;
  }

  public @Nullable Multiblock multiblock() {
    return live == null ? null : live.multiblock();
  }

  // Only the GameTests need the live multiblock itself, to reach the output pool.
  public @Nullable LiveMultiblock live() {
    return live;
  }

  public List<BlockPos> planters() {
    return live == null ? List.of() : live.planters();
  }

  public boolean ownsPlanter(BlockPos pos) {
    return live != null && live.ownsPlanter(pos);
  }

  public LiveMultiblock.PoolTotals totals() {
    return live == null ? LiveMultiblock.PoolTotals.NONE : live.totals();
  }

  public @Nullable EnergyHandler energy() {
    return live == null ? null : live.energy();
  }

  public @Nullable ResourceHandler<ItemResource> items() {
    return live == null ? null : live.items();
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
    ScanResult<BlockPos> result =
        StructureScanner.scan(
            worldPosition,
            pos -> Arrays.stream(Direction.values()).map(pos::relative).toList(),
            pos -> typeAt(level, pos),
            MPConfig.PART_CAP.get());
    if (result.equals(lastScan) && !rebuild) {
      return;
    }
    lastScan = result;
    if (result.problem() == Problem.WAITING_FOR_CHUNKS) {
      // The multiblock is kept so an unloaded part does not stop its planters, while the pools are
      // dropped so nothing touches a block entity in an unloaded chunk.
      live = live == null ? null : LiveMultiblock.paused(live.multiblock());
    } else {
      LiveMultiblock previous = live;
      live =
          result.formed()
              ? LiveMultiblock.running(level, Multiblock.from(result), this::markPoolsStale)
              : null;
      if (previous != null) {
        stopPlanters(level, previous.planters(), live == null ? List.of() : live.planters());
      }
    }
    level.invalidateCapabilities(worldPosition);
    setChanged();
  }

  // No scan runs once the core is gone, so the core stops its planters here, or they would stay
  // ACTIVE and GROWING.
  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level != null && live != null) {
      stopPlanters(level, live.planters(), List.of());
    }
  }

  private static void stopPlanters(
      Level level, List<BlockPos> planters, List<BlockPos> stillFormed) {
    for (BlockPos pos : planters) {
      if (!stillFormed.contains(pos)
          && level.isLoaded(pos)
          && level.getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
        planter.stopFromCore();
      }
    }
  }

  private static PartType typeAt(Level level, BlockPos pos) {
    if (!level.isLoaded(pos)) {
      return PartType.UNLOADED;
    }
    return level.getBlockState(pos).getBlock() instanceof PartBlock part
        ? part.partType()
        : PartType.NOT_A_PART;
  }
}
