/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.minecraft.client.multiplayer.ClientLevel$EntityCallbacks"})
abstract class ClientLevelEntityCallbacksMixin {
    @Shadow
    @Final
    ClientLevel this$0;

    ClientLevelEntityCallbacksMixin() {
    }

    @Inject(method={"onTrackingStart(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="TAIL")})
    private void invokeLoadEntity(Entity entity, CallbackInfo ci) {
        ClientEntityEvents.ENTITY_LOAD.invoker().onLoad(entity, this.this$0);
    }

    @Inject(method={"onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V"}, at={@At(value="HEAD")})
    private void invokeUnloadEntity(Entity entity, CallbackInfo ci) {
        ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.this$0);
    }
}

