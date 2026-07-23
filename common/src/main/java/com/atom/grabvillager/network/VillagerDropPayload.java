package com.atom.grabvillager.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.ResourceLocation;

public record VillagerDropPayload(boolean isThrow, float charge) implements CustomPacketPayload {
    public static final Type<VillagerDropPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("grabvillager", "drop_villager"));

    // Codec requis en 1.21 pour la sérialisation réseau
    public static final StreamCodec<FriendlyByteBuf, VillagerDropPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, VillagerDropPayload::isThrow,
            ByteBufCodecs.FLOAT, VillagerDropPayload::charge,
            VillagerDropPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}