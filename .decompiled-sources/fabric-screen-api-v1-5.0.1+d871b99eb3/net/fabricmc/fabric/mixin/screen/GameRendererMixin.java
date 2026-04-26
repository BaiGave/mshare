/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.screen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={GameRenderer.class})
abstract class GameRendererMixin {
    GameRendererMixin() {
    }

    @WrapOperation(method={"extractGui"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V")})
    private void onExtractGui(Screen currentScreen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta, Operation<Void> operation) {
        ScreenEvents.beforeExtract(currentScreen).invoker().beforeExtract(currentScreen, graphics, mouseX, mouseY, tickDelta);
        operation.call(currentScreen, graphics, mouseX, mouseY, Float.valueOf(tickDelta));
        ScreenEvents.afterExtract(currentScreen).invoker().afterExtract(currentScreen, graphics, mouseX, mouseY, tickDelta);
    }
}

