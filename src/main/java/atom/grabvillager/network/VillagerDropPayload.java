package atom.grabvillager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record VillagerDropPayload(boolean isThrow, float charge, float multiplier) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<VillagerDropPayload> PACKET_ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("grabvillager", "villager_drop"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VillagerDropPayload> PACKET_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.isThrow());
                buf.writeFloat(payload.charge());
                buf.writeFloat(payload.multiplier());
            },
            buf -> new VillagerDropPayload(buf.readBoolean(), buf.readFloat(), buf.readFloat())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
