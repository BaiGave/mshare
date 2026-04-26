/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.PacketCallbackListener;
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.payload.FriendlyByteBufLoginQueryResponse;
import net.fabricmc.fabric.impl.networking.server.ServerLoginNetworkAddon;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerLoginPacketListenerImpl.class})
abstract class ServerLoginPacketListenerImplMixin
implements PacketListenerExtensions,
PacketCallbackListener,
PacketContextProvider {
    @Shadow
    @Final
    private Connection connection;
    @Unique
    private ServerLoginNetworkAddon addon;

    ServerLoginPacketListenerImplMixin() {
    }

    @Shadow
    protected abstract void verifyLoginAndFinishConnectionSetup(GameProfile var1);

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void initAddon(MinecraftServer server, Connection connection, boolean transferred, CallbackInfo ci) {
        connection.getPacketContext().set(PacketContextImpl.SERVER_INSTANCE, server);
        connection.getPacketContext().set(PacketContextImpl.REGISTRY_ACCESS, server.registryAccess());
        this.addon = new ServerLoginNetworkAddon((ServerLoginPacketListenerImpl)((Object)this));
        this.addon.lateInit();
    }

    @Redirect(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;verifyLoginAndFinishConnectionSetup(Lcom/mojang/authlib/GameProfile;)V"))
    private void handlePlayerJoin(ServerLoginPacketListenerImpl instance, GameProfile profile) {
        if (this.addon.queryTick()) {
            this.verifyLoginAndFinishConnectionSetup(profile);
        }
    }

    @Inject(method={"handleCustomQueryPacket"}, at={@At(value="HEAD")}, cancellable=true)
    private void handleCustomPayloadReceivedAsync(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if (this.addon.handle(packet)) {
            ci.cancel();
        } else {
            CustomQueryAnswerPayload customQueryAnswerPayload = packet.payload();
            if (customQueryAnswerPayload instanceof FriendlyByteBufLoginQueryResponse) {
                FriendlyByteBufLoginQueryResponse response = (FriendlyByteBufLoginQueryResponse)customQueryAnswerPayload;
                response.data().skipBytes(response.data().readableBytes());
            }
        }
    }

    @Redirect(method={"verifyLoginAndFinishConnectionSetup"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;getCompressionThreshold()I", ordinal=0))
    private int removeLateCompressionPacketSending(MinecraftServer server) {
        return -1;
    }

    @Inject(method={"finishLoginAndWaitForClient"}, at={@At(value="HEAD")})
    private void storeGameProfileContext(GameProfile gameProfile, CallbackInfo ci) {
        this.getPacketContext().set(PacketContextImpl.GAME_PROFILE, gameProfile);
    }

    @Override
    public void sent(Packet<?> packet) {
        if (packet instanceof ClientboundCustomQueryPacket) {
            this.addon.registerOutgoingPacket((ClientboundCustomQueryPacket)packet);
        }
    }

    public ServerLoginNetworkAddon getAddon() {
        return this.addon;
    }

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

