/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PlayerList.class})
public class PlayerListMixin {
    @Inject(method={"placeNewPlayer"}, at={@At(value="NEW", target="net/minecraft/network/protocol/game/ClientboundUpdateRecipesPacket")})
    private void hookOnPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie arg, CallbackInfo ci) {
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(player, true);
    }

    @Inject(method={"reloadResources"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/common/ClientboundUpdateTagsPacket;<init>(Ljava/util/Map;)V")})
    private void hookOnDataPacksReloaded(CallbackInfo ci) {
        for (ServerPlayer player : ((PlayerList)((Object)this)).getPlayers()) {
            ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(player, false);
        }
    }
}

