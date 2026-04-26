/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.UntrackedPacketListener;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerGamePacketListenerImpl.class}, priority=999)
abstract class ServerGamePacketListenerImplMixin
extends ServerCommonPacketListenerImpl
implements PacketListenerExtensions {
    @Unique
    private ServerPlayNetworkAddon addon;

    ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie arg) {
        super(server, connection, arg);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddon(CallbackInfo ci) {
        this.addon = new ServerPlayNetworkAddon((ServerGamePacketListenerImpl)((Object)this), this.connection, this.server);
        if (!(this instanceof UntrackedPacketListener)) {
            this.addon.lateInit();
        }
    }

    @Inject(method={"handleCustomPayload"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleCustomPayloadReceivedAsync(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        try {
            if (this.getAddon().handle(packet.payload())) {
                ci.cancel();
            }
        }
        catch (RunningOnDifferentThreadException e) {
            this.server.packetProcessor().scheduleIfPossible(this, packet);
            ci.cancel();
        }
    }

    @WrapOperation(method={"handleConfigurationAcknowledged"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V")})
    private <T extends PacketListener> void onAcknowledgeReconfiguration(Connection instance, ProtocolInfo<T> protocolInfo, T packetListener, Operation<Void> original) {
        original.call(instance, protocolInfo, packetListener);
        ServerConfigurationPacketListenerImpl configPacketListener = (ServerConfigurationPacketListenerImpl)packetListener;
        ServerNetworkingImpl.getAddon(configPacketListener).setReconfiguring();
        if (this.addon.requestedReconfigure()) {
            configPacketListener.startConfiguration();
        }
    }

    public ServerPlayNetworkAddon getAddon() {
        return this.addon;
    }
}

