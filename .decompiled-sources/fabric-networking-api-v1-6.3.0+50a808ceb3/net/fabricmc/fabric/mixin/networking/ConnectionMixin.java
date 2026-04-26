/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.PacketCallbackListener;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.impl.networking.VanillaPacketTypes;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextSetter;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketMerger;
import net.fabricmc.fabric.impl.networking.splitter.FabricPacketSplitter;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
abstract class ConnectionMixin
implements ChannelInfoHolder,
PacketContextProvider {
    @Shadow
    private PacketListener packetListener;
    @Unique
    private Map<ConnectionProtocol, Collection<Identifier>> playChannels;
    @Unique
    private final PacketContextImpl packetContext = new PacketContextImpl((Connection)((Object)this));

    ConnectionMixin() {
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddedFields(PacketFlow flow, CallbackInfo ci) {
        this.playChannels = new ConcurrentHashMap<ConnectionProtocol, Collection<Identifier>>();
    }

    @Inject(method={"sendPacket"}, at={@At(value="FIELD", target="Lnet/minecraft/network/Connection;sentPackets:I", opcode=180)})
    private void checkPacket(Packet<?> packet, ChannelFutureListener callback, boolean flush, CallbackInfo ci) {
        if (this.packetListener instanceof PacketCallbackListener) {
            ((PacketCallbackListener)((Object)this.packetListener)).sent(packet);
        }
    }

    @Inject(method={"validateListener"}, at={@At(value="HEAD")})
    private void unwatchAddon(ProtocolInfo<?> protocolInfo, PacketListener listener, CallbackInfo ci) {
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof PacketListenerExtensions) {
            PacketListenerExtensions oldListener = (PacketListenerExtensions)((Object)packetListener);
            oldListener.getAddon().endSession();
        }
    }

    @Inject(method={"channelInactive"}, at={@At(value="HEAD")})
    private void disconnectAddon(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) {
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof PacketListenerExtensions) {
            PacketListenerExtensions extension = (PacketListenerExtensions)((Object)packetListener);
            extension.getAddon().handleDisconnect();
        }
    }

    @Inject(method={"handleDisconnection"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/PacketListener;onDisconnect(Lnet/minecraft/network/DisconnectionDetails;)V")})
    private void disconnectAddon(CallbackInfo ci) {
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof PacketListenerExtensions) {
            PacketListenerExtensions extension = (PacketListenerExtensions)((Object)packetListener);
            extension.getAddon().handleDisconnect();
        }
    }

    @ModifyArg(method={"setupInboundProtocol"}, at=@At(value="INVOKE", target="Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;"))
    private Object injectFabricPacketSlitterHandlerInbound(Object transitioner, @Local(argsOnly=true) ProtocolInfo<?> protocolInfo) {
        transitioner = ((UnconfiguredPipelineHandler.InboundConfigurationTask)transitioner).andThen(context -> {
            ChannelHandler patt0$temp = context.pipeline().get("decoder");
            if (patt0$temp instanceof PacketContextSetter) {
                PacketContextSetter setter = (PacketContextSetter)((Object)patt0$temp);
                setter.fabric_setPacketContext(this.packetContext);
            }
        });
        PayloadTypeRegistryImpl<?> payloadTypeRegistry = PayloadTypeRegistryImpl.get(protocolInfo);
        if (payloadTypeRegistry == null) {
            return transitioner;
        }
        return ((UnconfiguredPipelineHandler.InboundConfigurationTask)transitioner).andThen(context -> {
            FabricPacketMerger merger = new FabricPacketMerger(context.pipeline().get(PacketDecoder.class), payloadTypeRegistry, VanillaPacketTypes.get(protocolInfo));
            context.pipeline().addAfter("decoder", "fabric:merger", merger);
        });
    }

    @ModifyArg(method={"setupOutboundProtocol"}, at=@At(value="INVOKE", target="Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;"))
    private Object injectFabricPacketSlitterHandlerOutbound(Object transitioner, @Local(argsOnly=true) ProtocolInfo<?> protocolInfo) {
        transitioner = ((UnconfiguredPipelineHandler.OutboundConfigurationTask)transitioner).andThen(context -> {
            ChannelHandler patt0$temp = context.pipeline().get("encoder");
            if (patt0$temp instanceof PacketContextSetter) {
                PacketContextSetter setter = (PacketContextSetter)((Object)patt0$temp);
                setter.fabric_setPacketContext(this.packetContext);
            }
        });
        PayloadTypeRegistryImpl<?> payloadTypeRegistry = PayloadTypeRegistryImpl.get(protocolInfo);
        if (payloadTypeRegistry == null) {
            return transitioner;
        }
        return ((UnconfiguredPipelineHandler.OutboundConfigurationTask)transitioner).andThen(context -> {
            FabricPacketSplitter splitter = new FabricPacketSplitter(context.pipeline().get(PacketEncoder.class), payloadTypeRegistry);
            context.pipeline().addAfter("encoder", "fabric:splitter", splitter);
        });
    }

    @Override
    public Collection<Identifier> fabric_getPendingChannelsNames(ConnectionProtocol protocol) {
        return this.playChannels.computeIfAbsent(protocol, key -> Collections.newSetFromMap(new ConcurrentHashMap()));
    }

    @Override
    public PacketContext getPacketContext() {
        return this.packetContext;
    }
}

