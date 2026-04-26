/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Minecraft.class})
public abstract class MinecraftMixin {
    @Inject(at={@At(value="HEAD")}, method={"tick"})
    private void onStartTick(CallbackInfo info) {
        ClientTickEvents.START_CLIENT_TICK.invoker().onStartTick((Minecraft)((Object)this));
    }

    @Inject(at={@At(value="RETURN")}, method={"tick"})
    private void onEndTick(CallbackInfo info) {
        ClientTickEvents.END_CLIENT_TICK.invoker().onEndTick((Minecraft)((Object)this));
    }

    @Inject(at={@At(value="INVOKE", target="Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift=At.Shift.AFTER)}, method={"destroy"})
    private void onStopping(CallbackInfo ci) {
        ClientLifecycleEvents.CLIENT_STOPPING.invoker().onClientStopping((Minecraft)((Object)this));
    }

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/client/Minecraft;gameThread:Ljava/lang/Thread;", shift=At.Shift.AFTER, ordinal=0, opcode=181)}, method={"run"})
    private void onStart(CallbackInfo ci) {
        ClientLifecycleEvents.CLIENT_STARTED.invoker().onClientStarted((Minecraft)((Object)this));
    }

    @Inject(method={"updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V"}, at={@At(value="TAIL")})
    private void afterClientLevelChange(ClientLevel level, boolean stopSound, CallbackInfo ci) {
        if (level != null) {
            Minecraft client = (Minecraft)((Object)this);
            ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.invoker().afterLevelChange(client, level);
        }
    }
}

