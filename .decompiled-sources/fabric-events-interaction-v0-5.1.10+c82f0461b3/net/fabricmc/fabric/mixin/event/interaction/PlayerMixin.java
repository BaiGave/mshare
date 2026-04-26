/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Player.class})
public class PlayerMixin {
    @Inject(method={"attack"}, at={@At(value="HEAD")}, cancellable=true)
    public void onPlayerInteractEntity(Entity target, CallbackInfo info) {
        PlayerMixin playerMixin = this;
        if (playerMixin instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)((Object)playerMixin);
            InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, target, null);
            if (result != InteractionResult.PASS) {
                info.cancel();
            }
        }
    }
}

