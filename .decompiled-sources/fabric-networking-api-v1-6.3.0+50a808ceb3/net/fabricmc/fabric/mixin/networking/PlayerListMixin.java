/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PlayerList.class})
abstract class PlayerListMixin {
    PlayerListMixin() {
    }

    @Inject(method={"placeNewPlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/game/ClientboundPlayerAbilitiesPacket;<init>(Lnet/minecraft/world/entity/player/Abilities;)V")})
    private void handlePlayerConnection(Connection connection, ServerPlayer player, CommonListenerCookie arg, CallbackInfo ci) {
        ServerNetworkingImpl.getAddon(player.connection).onClientReady();
    }
}

