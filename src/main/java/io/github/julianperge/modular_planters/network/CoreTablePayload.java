package io.github.julianperge.modular_planters.network;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.github.julianperge.modular_planters.blockentity.PlanterStatus;
import io.github.julianperge.modular_planters.logic.Problem;
import io.github.julianperge.modular_planters.multiblock.MeStatus;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record CoreTablePayload(int containerId, Totals totals, List<Row> rows)
    implements CustomPacketPayload {
  public static final int MAX_ROWS = 512;

  public CoreTablePayload {
    rows = rows.size() > MAX_ROWS ? List.copyOf(rows.subList(0, MAX_ROWS)) : rows;
  }

  public record Totals(
      boolean formed,
      int partCount,
      int partCap,
      Problem problem,
      Optional<BlockPos> problemPos,
      long energyStored,
      long energyCapacity,
      long drawPerTick,
      int fertilizerCount,
      int exportFillPercent,
      MeStatus meStatus) {
    static final StreamCodec<RegistryFriendlyByteBuf, Totals> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL,
            Totals::formed,
            ByteBufCodecs.VAR_INT,
            Totals::partCount,
            ByteBufCodecs.VAR_INT,
            Totals::partCap,
            ByteBufCodecs.idMapper(
                index -> Problem.values()[Math.clamp(index, 0, Problem.values().length - 1)],
                Problem::ordinal),
            Totals::problem,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
            Totals::problemPos,
            ByteBufCodecs.VAR_LONG,
            Totals::energyStored,
            ByteBufCodecs.VAR_LONG,
            Totals::energyCapacity,
            ByteBufCodecs.VAR_LONG,
            Totals::drawPerTick,
            ByteBufCodecs.VAR_INT,
            Totals::fertilizerCount,
            ByteBufCodecs.VAR_INT,
            Totals::exportFillPercent,
            ByteBufCodecs.idMapper(
                index -> MeStatus.values()[Math.clamp(index, 0, MeStatus.values().length - 1)],
                MeStatus::ordinal),
            Totals::meStatus,
            Totals::new);
  }

  public record Row(
      BlockPos pos,
      ItemStack seedIcon,
      int seeds,
      int growing,
      ItemStack upgrade1,
      ItemStack upgrade2,
      int upgraded,
      PlanterStatus status) {
    static final StreamCodec<RegistryFriendlyByteBuf, Row> STREAM_CODEC =
        StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            Row::pos,
            ItemStack.OPTIONAL_STREAM_CODEC,
            Row::seedIcon,
            ByteBufCodecs.VAR_INT,
            Row::seeds,
            ByteBufCodecs.VAR_INT,
            Row::growing,
            ItemStack.OPTIONAL_STREAM_CODEC,
            Row::upgrade1,
            ItemStack.OPTIONAL_STREAM_CODEC,
            Row::upgrade2,
            ByteBufCodecs.VAR_INT,
            Row::upgraded,
            ByteBufCodecs.idMapper(
                index ->
                    PlanterStatus.values()[Math.clamp(index, 0, PlanterStatus.values().length - 1)],
                PlanterStatus::ordinal),
            Row::status,
            Row::new);
  }

  public static final Type<CoreTablePayload> TYPE =
      new Type<>(Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "core_table"));
  public static final StreamCodec<RegistryFriendlyByteBuf, CoreTablePayload> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.VAR_INT,
          CoreTablePayload::containerId,
          Totals.STREAM_CODEC,
          CoreTablePayload::totals,
          Row.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ROWS)),
          CoreTablePayload::rows,
          CoreTablePayload::new);

  @Override
  public Type<CoreTablePayload> type() {
    return TYPE;
  }
}
