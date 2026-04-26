/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextSetter;
import net.fabricmc.fabric.impl.networking.splitter.PassthroughPacket;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PacketEncoder.class}, priority=500)
public class PacketEncoderMixin
implements PacketContextSetter {
    @Unique
    private PacketContext packetContext;

    @Inject(method={"encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void handlePassthroughPacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        if (packet instanceof PassthroughPacket) {
            PassthroughPacket passthroughPacket = (PassthroughPacket)packet;
            byteBuf.writeBytes(passthroughPacket.buf());
            ci.cancel();
        }
    }

    @Override
    public void fabric_setPacketContext(PacketContext context) {
        this.packetContext = context;
    }

    @WrapMethod(method={"encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V"})
    private void wrapWithContext(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf output, Operation<Void> original) {
        ScopedValue.where(PacketContextImpl.VALUE, this.packetContext).run(() -> original.call(ctx, packet, output));
    }
}

