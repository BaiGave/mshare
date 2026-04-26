/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractContainerScreen.class})
public abstract class AbstractContainerScreenMixin
extends Screen {
    private AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")}, cancellable=true)
    private void callSuperMouseReleased(MouseButtonEvent ctx, CallbackInfoReturnable<Boolean> cir) {
        if (super.mouseReleased(ctx)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"mouseDragged"}, at={@At(value="HEAD")}, cancellable=true)
    private void callSuperMouseReleased(MouseButtonEvent ctx, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (super.mouseDragged(ctx, deltaX, deltaY)) {
            cir.setReturnValue(true);
        }
    }
}

