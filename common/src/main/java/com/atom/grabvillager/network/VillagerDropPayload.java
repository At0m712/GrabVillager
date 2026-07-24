package com.atom.grabvillager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record VillagerDropPayload(boolean isThrow, float charge) implements CustomPacketPayload {

    // Utilisation de la méthode de fabrication officielle de Mojang pour la 1.21
    public static final Type<VillagerDropPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("grabvillager", "villager_drop")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerDropPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            VillagerDropPayload::isThrow,
            ByteBufCodecs.FLOAT,
            VillagerDropPayload::charge,
            VillagerDropPayload::new
    );

    @Override
    public Type<VillagerDropPayload> type() {
        return TYPE;
    }
}