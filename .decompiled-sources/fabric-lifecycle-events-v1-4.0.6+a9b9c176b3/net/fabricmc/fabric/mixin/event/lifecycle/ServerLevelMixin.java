/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerLevel.class})
public abstract class ServerLevelMixin {
    @Inject(method={"tick"}, at={@At(value="FIELD", target="Lnet/minecraft/server/level/ServerLevel;handlingTick:Z", opcode=181, ordinal=0, shift=At.Shift.AFTER)})
    private void startLevelTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerTickEvents.START_LEVEL_TICK.invoker().onStartTick((ServerLevel)((Object)this));
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void endLevelTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerTickEvents.END_LEVEL_TICK.invoker().onEndTick((ServerLevel)((Object)this));
    }
}

