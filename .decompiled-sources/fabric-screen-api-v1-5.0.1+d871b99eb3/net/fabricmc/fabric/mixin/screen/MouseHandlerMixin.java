/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={MouseHandler.class})
abstract class MouseHandlerMixin {
    MouseHandlerMixin() {
    }

    @WrapOperation(method={"onButton"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z")})
    private boolean invokeMouseClickedEvents(Screen screen, MouseButtonEvent ctx, boolean doubleClick, Operation<Boolean> operation) {
        if (screen != null) {
            if (!ScreenMouseEvents.allowMouseClick(screen).invoker().allowMouseClick(screen, ctx)) {
                return true;
            }
            ScreenMouseEvents.beforeMouseClick(screen).invoker().beforeMouseClick(screen, ctx);
        }
        boolean result = operation.call(screen, ctx, doubleClick);
        if (screen != null) {
            result |= ScreenMouseEvents.afterMouseClick(screen).invoker().afterMouseClick(screen, ctx, result);
        }
        return result;
    }

    @WrapOperation(method={"onButton"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z")})
    private boolean invokeMousePressedEvents(Screen screen, MouseButtonEvent ctx, Operation<Boolean> operation) {
        if (screen != null) {
            if (!ScreenMouseEvents.allowMouseRelease(screen).invoker().allowMouseRelease(screen, ctx)) {
                return true;
            }
            ScreenMouseEvents.beforeMouseRelease(screen).invoker().beforeMouseRelease(screen, ctx);
        }
        boolean result = operation.call(screen, ctx);
        if (screen != null) {
            result |= ScreenMouseEvents.afterMouseRelease(screen).invoker().afterMouseRelease(screen, ctx, result);
        }
        return result;
    }

    @WrapOperation(method={"handleAccumulatedMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z")})
    private boolean invokeMouseDragEvents(Screen screen, MouseButtonEvent ctx, double horizontalAmount, double verticalAmount, Operation<Boolean> operation) {
        if (screen != null) {
            if (!ScreenMouseEvents.allowMouseDrag(screen).invoker().allowMouseDrag(screen, ctx, horizontalAmount, verticalAmount)) {
                return true;
            }
            ScreenMouseEvents.beforeMouseDrag(screen).invoker().beforeMouseDrag(screen, ctx, horizontalAmount, verticalAmount);
        }
        boolean result = operation.call(screen, ctx, horizontalAmount, verticalAmount);
        if (screen != null) {
            result |= ScreenMouseEvents.afterMouseDrag(screen).invoker().afterMouseDrag(screen, ctx, horizontalAmount, verticalAmount, result);
        }
        return result;
    }

    @WrapOperation(method={"onScroll"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z")})
    private boolean invokeMouseScrollEvents(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, Operation<Boolean> operation) {
        if (screen != null) {
            if (!ScreenMouseEvents.allowMouseScroll(screen).invoker().allowMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
            ScreenMouseEvents.beforeMouseScroll(screen).invoker().beforeMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        boolean result = operation.call(screen, mouseX, mouseY, horizontalAmount, verticalAmount);
        if (screen != null) {
            result |= ScreenMouseEvents.afterMouseScroll(screen).invoker().afterMouseScroll(screen, mouseX, mouseY, horizontalAmount, verticalAmount, result);
        }
        return result;
    }
}

