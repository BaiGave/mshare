/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.impl.networking.FabricCustomPayloadStreamCodec;
import net.fabricmc.fabric.impl.networking.GenericPayloadAccessor;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketSplitter;
import net.fabricmc.fabric.impl.networking.splitter.SplittablePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ClientboundCustomPayloadPacket.class})
public class ClientboundCustomPayloadPacketMixin
implements SplittablePacket,
GenericPayloadAccessor {
    @Shadow
    @Final
    private CustomPacketPayload payload;

    @WrapOperation(method={"<clinit>"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;", ordinal=0)})
    private static StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> wrapPlayCodec(CustomPacketPayload.FallbackProvider<RegistryFriendlyByteBuf> unknownCodecFactory, List<CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, ?>> types, Operation<StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>> original) {
        StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> codec = original.call(unknownCodecFactory, types);
        FabricCustomPayloadStreamCodec fabricCodec = (FabricCustomPayloadStreamCodec)((Object)codec);
        fabricCodec.fabric_setCustomPayloadTypeProvider((buf, identifier) -> PayloadTypeRegistryImpl.CLIENTBOUND_PLAY.get(identifier));
        return codec;
    }

    @WrapOperation(method={"<clinit>"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;", ordinal=1)})
    private static StreamCodec<FriendlyByteBuf, CustomPacketPayload> wrapConfigCodec(CustomPacketPayload.FallbackProvider<FriendlyByteBuf> unknownCodecFactory, List<CustomPacketPayload.TypeAndCodec<FriendlyByteBuf, ?>> types, Operation<StreamCodec<FriendlyByteBuf, CustomPacketPayload>> original) {
        StreamCodec<FriendlyByteBuf, CustomPacketPayload> codec = original.call(unknownCodecFactory, types);
        FabricCustomPayloadStreamCodec fabricCodec = (FabricCustomPayloadStreamCodec)((Object)codec);
        fabricCodec.fabric_setCustomPayloadTypeProvider((buf, identifier) -> PayloadTypeRegistryImpl.CLIENTBOUND_CONFIGURATION.get(identifier));
        return codec;
    }

    @Override
    public void fabric_split(PayloadTypeRegistryImpl<?> payloadTypeRegistry, ChannelHandlerContext channelHandlerContext, PacketEncoder<?> encoder, Packet<?> packet, Consumer<Packet<?>> consumer) throws Exception {
        int size = payloadTypeRegistry.getMaxPacketSizeForSplitting(this.payload.type().id());
        if (size == -1) {
            consumer.accept((Packet)((Object)this));
            return;
        }
        FabricPacketSplitter.genericPacketSplitter(this.payload.type().id(), channelHandlerContext, encoder, packet, ClientboundCustomPayloadPacket::new, consumer, 0x100000, size);
    }

    @Override
    public CustomPacketPayload fabric_payload() {
        return this.payload;
    }
}

