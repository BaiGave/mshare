/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.impl.client.gametest.TestInputImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={InputConstants.class})
public class InputConstantsMixin {
    @Inject(method={"isKeyDown"}, at={@At(value="HEAD")}, cancellable=true)
    private static void useGameTestInputForKeyDown(Window window, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(TestInputImpl.isKeyDown(keyCode));
    }

    @Inject(method={"setupKeyboardCallbacks", "setupMouseCallbacks"}, at={@At(value="HEAD")}, cancellable=true)
    private static void dontAttachCallbacks(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method={"grabOrReleaseMouse"}, at={@At(value="HEAD")}, cancellable=true)
    private static void disableCursorGrabbing(CallbackInfo ci) {
        ci.cancel();
    }
}

