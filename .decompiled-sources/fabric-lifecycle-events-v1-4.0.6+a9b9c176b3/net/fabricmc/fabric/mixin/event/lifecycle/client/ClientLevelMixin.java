/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientLevel.class})
abstract class ClientLevelMixin {
    ClientLevelMixin() {
    }

    @Inject(method={"tickEntities"}, at={@At(value="HEAD")})
    private void startLevelTick(CallbackInfo ci) {
        ClientTickEvents.START_LEVEL_TICK.invoker().onStartTick((ClientLevel)((Object)this));
    }

    @Inject(method={"tick"}, at={@At(value="RETURN")})
    public void endLevelTick(CallbackInfo ci) {
        ClientTickEvents.END_LEVEL_TICK.invoker().onEndTick((ClientLevel)((Object)this));
    }
}

