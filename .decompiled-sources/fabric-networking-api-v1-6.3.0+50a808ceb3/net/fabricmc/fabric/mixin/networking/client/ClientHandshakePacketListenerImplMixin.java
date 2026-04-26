/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.client.ClientLoginNetworkAddon;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.payload.FriendlyByteBufLoginQueryRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientHandshakePacketListenerImpl.class})
abstract class ClientHandshakePacketListenerImplMixin
implements PacketListenerExtensions,
PacketContextProvider {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private Connection connection;
    @Unique
    private ClientLoginNetworkAddon addon;

    ClientHandshakePacketListenerImplMixin() {
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddon(CallbackInfo ci) {
        this.addon = new ClientLoginNetworkAddon((ClientHandshakePacketListenerImpl)((Object)this), this.minecraft);
        this.addon.lateInit();
    }

    @Inject(method={"handleCustomQuery"}, at={@At(value="INVOKE", target="Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void handleQueryRequest(ClientboundCustomQueryPacket packet, CallbackInfo ci) {
        CustomQueryPayload customQueryPayload = packet.payload();
        if (customQueryPayload instanceof FriendlyByteBufLoginQueryRequestPayload) {
            FriendlyByteBufLoginQueryRequestPayload payload = (FriendlyByteBufLoginQueryRequestPayload)customQueryPayload;
            boolean handled = ScopedValue.where(ClientNetworkingImpl.CONNECTION_SCOPED_VALUE, this.connection).call(() -> this.addon.handlePacket(packet));
            if (handled) {
                ci.cancel();
            } else {
                payload.data().skipBytes(payload.data().readableBytes());
            }
        }
    }

    @Inject(method={"handleLoginFinished"}, at={@At(value="HEAD")})
    private void setGameProfileContext(ClientboundLoginFinishedPacket packet, CallbackInfo ci) {
        this.connection.getPacketContext().set(PacketContextImpl.GAME_PROFILE, packet.gameProfile());
    }

    public ClientLoginNetworkAddon getAddon() {
        return this.addon;
    }

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

