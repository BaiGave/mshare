/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerEntity.class})
abstract class ServerEntityMixin {
    @Shadow
    @Final
    private Entity entity;

    ServerEntityMixin() {
    }

    @Inject(method={"addPairing"}, at={@At(value="TAIL")})
    private void onStartTracking(ServerPlayer player, CallbackInfo ci) {
        EntityTrackingEvents.START_TRACKING.invoker().onStartTracking(this.entity, player);
    }

    @Inject(method={"removePairing"}, at={@At(value="HEAD")})
    private void onStopTracking(ServerPlayer player, CallbackInfo ci) {
        EntityTrackingEvents.STOP_TRACKING.invoker().onStopTracking(this.entity, player);
    }
}

