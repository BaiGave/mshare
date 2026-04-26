/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={KeyboardHandler.class})
abstract class KeyboardHandlerMixin {
    KeyboardHandlerMixin() {
    }

    @WrapOperation(method={"keyPress"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;keyPressed(Lnet/minecraft/client/input/KeyEvent;)Z")})
    private boolean invokeKeyPressedEvents(Screen screen, KeyEvent ctx, Operation<Boolean> operation) {
        if (screen != null) {
            if (!ScreenKeyboardEvents.allowKeyPress(screen).invoker().allowKeyPress(screen, ctx)) {
                return true;
            }
            ScreenKeyboardEvents.beforeKeyPress(screen).invoker().beforeKeyPress(screen, ctx);
        }
        boolean result = operation.call(screen, ctx);
        if (screen != null) {
            ScreenKeyboardEvents.afterKeyPress(screen).invoker().afterKeyPress(screen, ctx);
        }
        return result;
    }

    @WrapOperation(method={"keyPress"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;keyReleased(Lnet/minecraft/client/input/KeyEvent;)Z")})
    private boolean invokeKeyReleasedEvents(Screen screen, KeyEvent ctx, Operation<Boolean> operation) {
        if (screen != null) {
            if (!ScreenKeyboardEvents.allowKeyRelease(screen).invoker().allowKeyRelease(screen, ctx)) {
                return true;
            }
            ScreenKeyboardEvents.beforeKeyRelease(screen).invoker().beforeKeyRelease(screen, ctx);
        }
        boolean result = operation.call(screen, ctx);
        if (screen != null) {
            ScreenKeyboardEvents.afterKeyRelease(screen).invoker().afterKeyRelease(screen, ctx);
        }
        return result;
    }
}

