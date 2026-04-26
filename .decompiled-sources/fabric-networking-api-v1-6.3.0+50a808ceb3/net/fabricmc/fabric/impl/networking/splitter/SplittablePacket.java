/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.channel.ChannelHandlerContext;
import java.util.function.Consumer;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;

public interface SplittablePacket {
    public void fabric_split(PayloadTypeRegistryImpl<?> var1, ChannelHandlerContext var2, PacketEncoder<?> var3, Packet<?> var4, Consumer<Packet<?>> var5) throws Exception;
}

