/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import io.netty.channel.ChannelFutureListener;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface LoginPacketSender
extends PacketSender {
    public Packet<?> createPacket(Identifier var1, FriendlyByteBuf var2);

    default public void sendPacket(Identifier channel, FriendlyByteBuf buf) {
        Objects.requireNonNull(channel, "Channel cannot be null");
        Objects.requireNonNull(buf, "Payload cannot be null");
        this.sendPacket(this.createPacket(channel, buf));
    }

    default public void sendPacket(Identifier channel, FriendlyByteBuf buf, @Nullable ChannelFutureListener callback) {
        Objects.requireNonNull(channel, "Channel cannot be null");
        Objects.requireNonNull(buf, "Payload cannot be null");
        this.sendPacket(this.createPacket(channel, buf), callback);
    }
}

