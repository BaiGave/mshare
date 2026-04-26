/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface PacketSender {
    public Packet<?> createPacket(CustomPacketPayload var1);

    default public void sendPacket(Packet<?> packet) {
        this.sendPacket(packet, null);
    }

    default public void sendPacket(CustomPacketPayload payload) {
        this.sendPacket(this.createPacket(payload));
    }

    public void sendPacket(Packet<?> var1, @Nullable ChannelFutureListener var2);

    default public void sendPacket(CustomPacketPayload payload, @Nullable ChannelFutureListener callback) {
        this.sendPacket(this.createPacket(payload), callback);
    }

    public void disconnect(Component var1);
}

