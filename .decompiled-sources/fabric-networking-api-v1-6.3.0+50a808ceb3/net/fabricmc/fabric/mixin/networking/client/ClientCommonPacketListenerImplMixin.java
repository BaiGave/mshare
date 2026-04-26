/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.RunningOnDifferentThreadException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientCommonPacketListenerImpl.class})
public abstract class ClientCommonPacketListenerImplMixin
implements PacketListenerExtensions,
PacketContextProvider {
    @Shadow
    @Final
    protected Minecraft minecraft;
    @Shadow
    @Final
    protected Connection connection;

    @Inject(method={"handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        CustomPacketPayload payload = packet.payload();
        try {
            boolean handled;
            AbstractNetworkAddon<?> abstractNetworkAddon = this.getAddon();
            if (abstractNetworkAddon instanceof ClientPlayNetworkAddon) {
                ClientPlayNetworkAddon addon = (ClientPlayNetworkAddon)abstractNetworkAddon;
                handled = addon.handle(payload);
            } else {
                abstractNetworkAddon = this.getAddon();
                if (abstractNetworkAddon instanceof ClientConfigurationNetworkAddon) {
                    ClientConfigurationNetworkAddon addon = (ClientConfigurationNetworkAddon)abstractNetworkAddon;
                    handled = addon.handle(payload);
                } else {
                    throw new IllegalStateException("Unknown network addon");
                }
            }
            if (handled) {
                ci.cancel();
            }
        }
        catch (RunningOnDifferentThreadException e) {
            this.minecraft.packetProcessor().scheduleIfPossible((ClientCommonPacketListenerImpl)((Object)this), packet);
            ci.cancel();
        }
    }

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

