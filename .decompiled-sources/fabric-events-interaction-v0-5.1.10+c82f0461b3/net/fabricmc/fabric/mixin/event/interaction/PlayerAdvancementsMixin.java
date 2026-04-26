/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.interaction;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerAdvancements.class})
public class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    @Inject(method={"setPlayer"}, at={@At(value="HEAD")}, cancellable=true)
    void preventOwnerOverride(ServerPlayer newOwner, CallbackInfo ci) {
        if (newOwner instanceof FakePlayer) {
            ci.cancel();
        }
    }

    @Inject(method={"award"}, at={@At(value="HEAD")}, cancellable=true)
    void preventGrantCriterion(AdvancementHolder advancement, String criterionName, CallbackInfoReturnable<Boolean> ci) {
        if (this.player instanceof FakePlayer) {
            ci.setReturnValue(false);
        }
    }
}

