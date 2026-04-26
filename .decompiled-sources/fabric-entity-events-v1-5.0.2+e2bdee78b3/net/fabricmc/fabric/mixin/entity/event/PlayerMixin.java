/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Player.class})
abstract class PlayerMixin {
    PlayerMixin() {
    }

    @Inject(method={"startSleepInBed"}, at={@At(value="HEAD")}, cancellable=true)
    private void onStartSleepInBed(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info) {
        Player.BedSleepingProblem failureReason = EntitySleepEvents.ALLOW_SLEEPING.invoker().allowSleep((Player)((Object)this), pos);
        if (failureReason != null) {
            info.setReturnValue(Either.left(failureReason));
        }
    }

    @Inject(method={"isSleepingLongEnough"}, at={@At(value="RETURN")}, cancellable=true)
    private void onIsSleepingLongEnough(CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValueZ()) {
            info.setReturnValue(EntitySleepEvents.ALLOW_RESETTING_TIME.invoker().allowResettingTime((Player)((Object)this)));
        }
    }
}

