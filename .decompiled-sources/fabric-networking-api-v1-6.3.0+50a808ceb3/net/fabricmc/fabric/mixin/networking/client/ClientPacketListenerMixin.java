/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client;

import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class}, priority=999)
abstract class ClientPacketListenerMixin
extends ClientCommonPacketListenerImpl
implements PacketListenerExtensions {
    @Unique
    private ClientPlayNetworkAddon addon;

    protected ClientPacketListenerMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddon(CallbackInfo ci) {
        this.addon = new ClientPlayNetworkAddon((ClientPacketListener)((Object)this), this.minecraft);
        ClientNetworkingImpl.setClientPlayAddon(this.addon);
        this.addon.lateInit();
    }

    @Inject(method={"handleLogin"}, at={@At(value="RETURN")})
    private void handleServerPlayReady(ClientboundLoginPacket packet, CallbackInfo ci) {
        this.addon.onServerReady();
    }

    public ClientPlayNetworkAddon getAddon() {
        return this.addon;
    }
}

