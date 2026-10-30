package io.github.julianperge.modular_planters.network;

import io.github.julianperge.modular_planters.ModularPlanters;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record SeedBinCombosPayload(int containerId, List<ItemStack> combos) implements CustomPacketPayload {
    public static final Type<SeedBinCombosPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(ModularPlanters.MOD_ID, "seed_bin_combos"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SeedBinCombosPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SeedBinCombosPayload::containerId,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), SeedBinCombosPayload::combos,
            SeedBinCombosPayload::new);

    @Override
    public Type<SeedBinCombosPayload> type() {
        return TYPE;
    }
}
