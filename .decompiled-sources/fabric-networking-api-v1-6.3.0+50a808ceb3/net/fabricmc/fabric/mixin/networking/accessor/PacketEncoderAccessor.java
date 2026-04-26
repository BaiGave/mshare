/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.accessor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={PacketEncoder.class})
public interface PacketEncoderAccessor {
    @Invoker(value="encode")
    public void fabric_encode(ChannelHandlerContext var1, Packet<?> var2, ByteBuf var3) throws Exception;
}

