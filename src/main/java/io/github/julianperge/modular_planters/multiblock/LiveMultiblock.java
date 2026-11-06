package io.github.julianperge.modular_planters.multiblock;

import io.github.julianperge.modular_planters.MPConfig;
import io.github.julianperge.modular_planters.ate.AteBridge;
import io.github.julianperge.modular_planters.blockentity.ExportBlockEntity;
import io.github.julianperge.modular_planters.blockentity.FertilizerBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PlanterBlockEntity;
import io.github.julianperge.modular_planters.blockentity.PowerBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class LiveMultiblock {
  public record PoolTotals(
      long stored, long capacity, int fertilizer, int outputFillPercent, MeStatus me) {
    public static final PoolTotals NONE = new PoolTotals(0, 0, 0, 0, MeStatus.NONE);
  }

  private final Multiblock multiblock;
  // Runs when a pooled block or the ME link is found removed, so the owner can rebuild this.
  private final Runnable onStale;
  private final @Nullable PowerPool power;
  private final @Nullable FertilizerPool fertilizer;
  private final @Nullable OutputSink outputs;
  private final @Nullable MeLink me;
  private final @Nullable ResourceHandler<ItemResource> items;

  private LiveMultiblock(
      Multiblock multiblock,
      Runnable onStale,
      @Nullable PowerPool power,
      @Nullable FertilizerPool fertilizer,
      @Nullable OutputSink outputs,
      @Nullable MeLink me) {
    this.multiblock = multiblock;
    this.onStale = onStale;
    this.power = power;
    this.fertilizer = fertilizer;
    this.outputs = outputs;
    this.me = me;
    this.items = fertilizer == null || outputs == null ? null : itemDoorway(fertilizer, outputs);
  }

  // Every part must be loaded when this is called, because the pools look up their block entities
  // here.
  public static LiveMultiblock running(Level level, Multiblock multiblock, Runnable onStale) {
    PowerPool power =
        new PowerPool(blockEntities(level, multiblock.power(), PowerBlockEntity.class), onStale);
    FertilizerPool fertilizer =
        new FertilizerPool(
            blockEntities(level, multiblock.fertilizer(), FertilizerBlockEntity.class), onStale);
    BlockPos mePos = multiblock.me();
    MeLink me = mePos != null && level.getBlockEntity(mePos) instanceof MeLink link ? link : null;
    OutputSink outputs =
        new OutputSink(
            blockEntities(level, multiblock.export(), ExportBlockEntity.class), me, onStale);
    return new LiveMultiblock(multiblock, onStale, power, fertilizer, outputs, me);
  }

  // A paused instance is what the Core holds while it waits for unloaded chunks (ADR 0009).
  // It keeps the Multiblock so its planters are not stopped, but it holds no pools and offers
  // no Doorway.
  public static LiveMultiblock paused(Multiblock multiblock) {
    return new LiveMultiblock(multiblock, () -> {}, null, null, null, null);
  }

  public Multiblock multiblock() {
    return multiblock;
  }

  public List<BlockPos> planters() {
    return multiblock.planters();
  }

  public boolean ownsPlanter(BlockPos pos) {
    return isRunning() && multiblock.planters().contains(pos);
  }

  // A paused instance holds no pools.
  private boolean isRunning() {
    return power != null;
  }

  public PoolTotals totals() {
    if (power == null || fertilizer == null || outputs == null) {
      return PoolTotals.NONE;
    }
    MeStatus meStatus = me == null || me.isRemoved() ? MeStatus.NONE : me.status();
    return new PoolTotals(
        power.stored(), power.capacity(), fertilizer.available(), outputs.fillPercent(), meStatus);
  }

  public @Nullable EnergyHandler energy() {
    return power == null ? null : power.insertView();
  }

  public @Nullable ResourceHandler<ItemResource> items() {
    return items;
  }

  // Only the GameTests call this, to put harvests into the export blocks the way a Modular Planter
  // Block does.
  public @Nullable OutputSink outputs() {
    return outputs;
  }

  // Only the GameTests call this, to check the fertilizer pool the Core holds.
  public @Nullable FertilizerPool fertilizer() {
    return fertilizer;
  }

  // A paused instance, or one with an unloaded part, does nothing, so the planters keep their
  // progress without ticking. The phase is the game time plus the Core's offset, and it spreads the
  // ME transfers of different Cores over different ticks.
  public void tick(ServerLevel level, long phase) {
    if (power == null || fertilizer == null || outputs == null || !allPartsLoaded(level)) {
      return;
    }
    MeLink link = onlineMe();
    if (link != null) {
      power.refillFrom(link);
    }
    TickContext context =
        new TickContext(
            level, level.getRandom(), AteBridge.numbers(), power, fertilizer.budget(), outputs);
    for (BlockPos pos : multiblock.planters()) {
      if (level.getBlockEntity(pos) instanceof PlanterBlockEntity planter) {
        planter.tickFromCore(context);
      }
    }
    if (link != null && phase % MPConfig.ME_INTERVAL.get() == 0) {
      outputs.moveExportsInto(link);
      fertilizer.topUpFrom(link);
    }
  }

  private boolean allPartsLoaded(Level level) {
    return multiblock.all().stream().allMatch(level::isLoaded);
  }

  // This returns the link only while it is online. A removed link counts as absent, and onStale
  // runs so the owner can rebuild without it.
  private @Nullable MeLink onlineMe() {
    if (me == null) {
      return null;
    }
    if (me.isRemoved()) {
      onStale.run();
      return null;
    }
    return me.online() ? me : null;
  }

  // The handler is built once, but a removed fertilizer or export block changes the slot count.
  // Until the owner rebuilds, each call falls back to combining the pool views again.
  private static ResourceHandler<ItemResource> itemDoorway(
      FertilizerPool fertilizer, OutputSink outputs) {
    ResourceHandler<ItemResource> whole =
        new CombinedResourceHandler<>(fertilizer.insertView(), outputs.extractView());
    return new DelegatingResourceHandler<>(
        () ->
            fertilizer.allLive() && outputs.allLive()
                ? whole
                : new CombinedResourceHandler<>(fertilizer.insertView(), outputs.extractView()));
  }

  private static <T extends BlockEntity> List<T> blockEntities(
      Level level, List<BlockPos> positions, Class<T> type) {
    List<T> found = new ArrayList<>();
    for (BlockPos pos : positions) {
      BlockEntity blockEntity = level.getBlockEntity(pos);
      if (type.isInstance(blockEntity)) {
        found.add(type.cast(blockEntity));
      }
    }
    return found;
  }
}
