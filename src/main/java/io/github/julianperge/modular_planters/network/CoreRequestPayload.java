package io.github.julianperge.modular_planters.network;

import io.github.julianperge.modular_planters.ModularPlanters;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CoreRequestPayload(Action action, Optional<BlockPos> planter) implements CustomPacketPayload {
    public enum Action { OPEN, BACK_TO_CORE }

    public static final Type<CoreRequestPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "core_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CoreRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> Action.values()[Math.clamp(index, 0, Action.values().length - 1)], Action::ordinal),
            CoreRequestPayload::action,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), CoreRequestPayload::planter,
            CoreRequestPayload::new);

    @Override
    public Type<CoreRequestPayload> type() {
        return TYPE;
    }
}
