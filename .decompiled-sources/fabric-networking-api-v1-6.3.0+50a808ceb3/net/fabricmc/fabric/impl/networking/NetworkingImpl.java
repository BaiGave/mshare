/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.impl.networking.splitter.FabricSplitPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkingImpl {
    public static final String MOD_ID = "fabric-networking-api-v1";
    public static final Logger LOGGER = LoggerFactory.getLogger("fabric-networking-api-v1");
    public static final Identifier REGISTER_CHANNEL = Identifier.withDefaultNamespace("register");
    public static final Identifier UNREGISTER_CHANNEL = Identifier.withDefaultNamespace("unregister");

    public static boolean isReservedCommonChannel(Identifier channelName) {
        return channelName.equals(REGISTER_CHANNEL) || channelName.equals(UNREGISTER_CHANNEL);
    }

    public static void init() {
        PayloadTypeRegistry.clientboundConfiguration().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RegistrationPayload.REGISTER, RegistrationPayload.REGISTER_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(RegistrationPayload.UNREGISTER, RegistrationPayload.UNREGISTER_CODEC);
        NetworkingImpl.registerGeneric(FabricSplitPacketPayload.TYPE, FabricSplitPacketPayload.CODEC);
    }

    private static <T extends CustomPacketPayload> void registerGeneric(CustomPacketPayload.Type<T> id, StreamCodec<? super FriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundConfiguration().register(id, codec);
        PayloadTypeRegistry.serverboundConfiguration().register(id, codec);
        PayloadTypeRegistry.clientboundPlay().register(id, codec);
        PayloadTypeRegistry.serverboundPlay().register(id, codec);
    }
}

