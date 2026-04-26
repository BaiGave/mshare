/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.accessor;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.PacketDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={PacketDecoder.class})
public interface PacketDecoderAccessor {
    @Invoker(value="decode")
    public void fabric_decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws Exception;
}

