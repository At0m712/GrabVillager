package atom.grabvillager.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(float throwMultiplier, boolean allowTools) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ConfigSyncPayload> PACKET_ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("grabvillager", "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> PACKET_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeFloat(payload.throwMultiplier());
                buf.writeBoolean(payload.allowTools());
            },
            buf -> new ConfigSyncPayload(buf.readFloat(), buf.readBoolean())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
