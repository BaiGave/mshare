/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientConfigurationPacketListenerImpl.class}, priority=999)
public abstract class ClientConfigurationPacketListenerImplMixin
extends ClientCommonPacketListenerImpl
implements PacketListenerExtensions {
    @Unique
    private ClientConfigurationNetworkAddon addon;

    protected ClientConfigurationPacketListenerImplMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddon(CallbackInfo ci) {
        this.addon = new ClientConfigurationNetworkAddon((ClientConfigurationPacketListenerImpl)((Object)this), this.minecraft);
        ClientNetworkingImpl.setClientConfigurationAddon(this.addon);
        this.addon.lateInit();
    }

    @Inject(method={"handleConfigurationFinished"}, at={@At(value="NEW", target="(Lnet/minecraft/client/Minecraft;Lnet/minecraft/network/Connection;Lnet/minecraft/client/multiplayer/CommonListenerCookie;)Lnet/minecraft/client/multiplayer/ClientPacketListener;")})
    public void handleComplete(ClientboundFinishConfigurationPacket packet, CallbackInfo ci, @Local RegistryAccess.Frozen registryAccess) {
        this.connection.getPacketContext().set(PacketContextImpl.REGISTRY_ACCESS, registryAccess);
        this.addon.handleComplete();
    }

    public ClientConfigurationNetworkAddon getAddon() {
        return this.addon;
    }
}

