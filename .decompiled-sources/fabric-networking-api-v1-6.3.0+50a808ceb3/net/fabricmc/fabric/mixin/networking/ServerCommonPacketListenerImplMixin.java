/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerCommonPacketListenerImpl.class})
public abstract class ServerCommonPacketListenerImplMixin
implements PacketListenerExtensions,
PacketContextProvider {
    @Shadow
    @Final
    protected MinecraftServer server;
    @Shadow
    @Final
    protected Connection connection;

    @Inject(method={"handleCustomPayload"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleCustomPayloadReceivedAsync(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        CustomPacketPayload payload = packet.payload();
        try {
            AbstractNetworkAddon<?> abstractNetworkAddon = this.getAddon();
            if (!(abstractNetworkAddon instanceof ServerConfigurationNetworkAddon)) {
                throw new IllegalStateException("Unknown addon");
            }
            ServerConfigurationNetworkAddon addon = (ServerConfigurationNetworkAddon)abstractNetworkAddon;
            boolean handled = addon.handle(payload);
            if (handled) {
                ci.cancel();
            }
        }
        catch (RunningOnDifferentThreadException e) {
            this.server.packetProcessor().scheduleIfPossible((ServerCommonPacketListenerImpl)((Object)this), packet);
            ci.cancel();
        }
    }

    @Inject(method={"handlePong"}, at={@At(value="HEAD")})
    private void onPlayPong(ServerboundPongPacket packet, CallbackInfo ci) {
        AbstractNetworkAddon<?> abstractNetworkAddon = this.getAddon();
        if (abstractNetworkAddon instanceof ServerConfigurationNetworkAddon) {
            ServerConfigurationNetworkAddon addon = (ServerConfigurationNetworkAddon)abstractNetworkAddon;
            addon.onPong(packet.getId());
        }
    }

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

