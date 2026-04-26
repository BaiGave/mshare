/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ServerPlayer.class})
public class ServerPlayerMixin {
    @Inject(method={"calculateGameModeForNewPlayer"}, at={@At(value="HEAD")}, cancellable=true)
    public void fakePlayerGameMode(GameType backupGameMode, CallbackInfoReturnable<GameType> cir) {
        if (this instanceof FakePlayer) {
            cir.setReturnValue(GameType.SURVIVAL);
        }
    }
}

