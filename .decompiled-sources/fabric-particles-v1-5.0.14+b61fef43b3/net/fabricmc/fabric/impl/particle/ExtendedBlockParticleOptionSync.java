/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.particle;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class ExtendedBlockParticleOptionSync
implements ModInitializer {
    private static final PacketContext.Key<Boolean> ENCODE_FALLBACK = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "extended_block_particle_fallback"));
    private static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "extended_block_particle_option_sync");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundConfiguration().register(DummyPayload.ID, DummyPayload.CODEC);
        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, minecraftServer) -> listener.getPacketContext().set(ENCODE_FALLBACK, !ServerConfigurationNetworking.canSend(listener, PACKET_ID)));
    }

    public static boolean shouldEncodeFallback() {
        PacketContext context = PacketContext.get();
        if (context == null) {
            return true;
        }
        return context.orElse(ENCODE_FALLBACK, true);
    }

    public record DummyPayload() implements CustomPacketPayload
    {
        public static final DummyPayload INSTANCE = new DummyPayload();
        public static final StreamCodec<FriendlyByteBuf, DummyPayload> CODEC = StreamCodec.unit(INSTANCE);
        public static final CustomPacketPayload.Type<DummyPayload> ID = new CustomPacketPayload.Type(PACKET_ID);

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}

