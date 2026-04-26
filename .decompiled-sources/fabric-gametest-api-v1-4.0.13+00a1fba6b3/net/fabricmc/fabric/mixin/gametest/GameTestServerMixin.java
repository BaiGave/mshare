/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gametest;

import net.minecraft.gametest.framework.GameTestServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={GameTestServer.class})
public abstract class GameTestServerMixin {
    @Inject(method={"isDedicatedServer"}, at={@At(value="HEAD")}, cancellable=true)
    public void isDedicated(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}

