/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerList.class})
abstract class PlayerListMixin {
    PlayerListMixin() {
    }

    @Inject(method={"respawn"}, at={@At(value="TAIL")})
    private void afterRespawn(ServerPlayer oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer newPlayer = cir.getReturnValue();
        ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(oldPlayer, newPlayer, alive);
        if (oldPlayer.level() != newPlayer.level()) {
            ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.invoker().afterChangeLevel(newPlayer, oldPlayer.level(), newPlayer.level());
        }
    }

    @Inject(method={"placeNewPlayer"}, at={@At(value="RETURN")})
    private void firePlayerJoinEvent(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        ServerPlayerEvents.JOIN.invoker().onJoin(player);
    }

    @Inject(method={"remove"}, at={@At(value="HEAD")})
    private void firePlayerLeaveEvent(ServerPlayer player, CallbackInfo ci) {
        ServerPlayerEvents.LEAVE.invoker().onLeave(player);
    }
}

