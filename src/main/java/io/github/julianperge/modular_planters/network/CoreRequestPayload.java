package io.github.julianperge.modular_planters.network;

import io.github.julianperge.modular_planters.ModularPlanters;
import io.netty.handler.codec.DecoderException;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CoreRequestPayload(Action action, Optional<BlockPos> planter)
    implements CustomPacketPayload {
  public enum Action {
    OPEN,
    BACK_TO_CORE
  }

  public static final Type<CoreRequestPayload> TYPE =
      new Type<>(Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "core_request"));
  public static final StreamCodec<RegistryFriendlyByteBuf, CoreRequestPayload> STREAM_CODEC =
      StreamCodec.composite(
          ByteBufCodecs.idMapper(CoreRequestPayload::actionAt, Action::ordinal),
          CoreRequestPayload::action,
          ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
          CoreRequestPayload::planter,
          CoreRequestPayload::new);

  private static Action actionAt(int index) {
    if (index < 0 || index >= Action.values().length) {
      throw new DecoderException("Unknown core request action " + index);
    }
    return Action.values()[index];
  }

  @Override
  public Type<CoreRequestPayload> type() {
    return TYPE;
  }
}
