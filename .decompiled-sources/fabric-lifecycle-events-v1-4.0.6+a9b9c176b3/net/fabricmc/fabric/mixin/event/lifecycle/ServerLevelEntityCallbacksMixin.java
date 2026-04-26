/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.minecraft.server.level.ServerLevel$EntityCallbacks"})
abstract class ServerLevelEntityCallbacksMixin {
    @Shadow
    @Final
    ServerLevel this$0;

    ServerLevelEntityCallbacksMixin() {
    }

    @Inject(method={"onTrackingStart(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="TAIL")})
    private void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
        ServerEntityEvents.ENTITY_LOAD.invoker().onLoad(entity, this.this$0);
    }

    @Inject(method={"onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")})
    private void invokeEntityUnloadEvent(Entity entity, CallbackInfo info) {
        ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.this$0);
    }
}

