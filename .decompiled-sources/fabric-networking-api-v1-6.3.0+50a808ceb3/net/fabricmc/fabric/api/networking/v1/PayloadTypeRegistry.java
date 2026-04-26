/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import java.util.function.IntSupplier;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface PayloadTypeRegistry<B extends FriendlyByteBuf> {
    public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> register(CustomPacketPayload.Type<T> var1, StreamCodec<? super B, T> var2);

    public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> var1, StreamCodec<? super B, T> var2, int var3);

    public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> var1, StreamCodec<? super B, T> var2, IntSupplier var3);

    public static PayloadTypeRegistry<FriendlyByteBuf> serverboundConfiguration() {
        return PayloadTypeRegistryImpl.SERVERBOUND_CONFIGURATION;
    }

    public static PayloadTypeRegistry<FriendlyByteBuf> clientboundConfiguration() {
        return PayloadTypeRegistryImpl.CLIENTBOUND_CONFIGURATION;
    }

    public static PayloadTypeRegistry<RegistryFriendlyByteBuf> serverboundPlay() {
        return PayloadTypeRegistryImpl.SERVERBOUND_PLAY;
    }

    public static PayloadTypeRegistry<RegistryFriendlyByteBuf> clientboundPlay() {
        return PayloadTypeRegistryImpl.CLIENTBOUND_PLAY;
    }
}

