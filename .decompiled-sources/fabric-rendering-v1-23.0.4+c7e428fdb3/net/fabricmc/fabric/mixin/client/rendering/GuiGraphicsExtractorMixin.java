/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.ExtractItemDecorationsCallback;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiGraphicsExtractor.class})
abstract class GuiGraphicsExtractorMixin {
    GuiGraphicsExtractorMixin() {
    }

    @Inject(method={"itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"}, at={@At(value="RETURN")})
    public void drawStackOverlay(Font font, ItemStack stack, int x, int y, @Nullable String stackCountText, CallbackInfo callback) {
        if (!stack.isEmpty()) {
            ExtractItemDecorationsCallback.EVENT.invoker().onExtractItemDecorations((GuiGraphicsExtractor)((Object)this), font, stack, x, y);
        }
    }
}

