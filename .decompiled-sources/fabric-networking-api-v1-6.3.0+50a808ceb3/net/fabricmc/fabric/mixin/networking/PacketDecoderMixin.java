/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextSetter;
import net.minecraft.network.PacketDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={PacketDecoder.class}, priority=500)
public class PacketDecoderMixin
implements PacketContextSetter {
    @Unique
    private PacketContext packetContext;

    @WrapMethod(method={"decode"})
    private void wrapWithContext(ChannelHandlerContext ctx, ByteBuf input, List<Object> out, Operation<Void> original) {
        ScopedValue.where(PacketContextImpl.VALUE, this.packetContext).run(() -> original.call(ctx, input, out));
    }

    @Override
    public void fabric_setPacketContext(PacketContext context) {
        this.packetContext = context;
    }
}

