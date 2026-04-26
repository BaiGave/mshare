/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={IdDispatchCodec.class})
public abstract class IdDispatchCodecMixin<B extends ByteBuf, V, T>
implements StreamCodec<B, V> {
    @Inject(method={"encode(Lio/netty/buffer/ByteBuf;Ljava/lang/Object;)V"}, at={@At(value="NEW", target="(Ljava/lang/String;Ljava/lang/Throwable;)Lio/netty/handler/codec/EncoderException;")})
    public void encode(B byteBuf, V packet, CallbackInfo ci, @Local(name={"type"}) T packetType, @Local(name={"e"}) Exception e) {
        CustomPacketPayload payload = null;
        if (packet instanceof ServerboundCustomPayloadPacket) {
            ServerboundCustomPayloadPacket customPayloadPacket = (ServerboundCustomPayloadPacket)packet;
            payload = customPayloadPacket.payload();
        } else if (packet instanceof ClientboundCustomPayloadPacket) {
            ClientboundCustomPayloadPacket customPayloadPacket = (ClientboundCustomPayloadPacket)packet;
            payload = customPayloadPacket.payload();
        }
        if (payload != null && payload.type() != null) {
            throw new EncoderException("Failed to encode packet '%s' (%s)".formatted(packetType, payload.type().id().toString()), e);
        }
    }
}

