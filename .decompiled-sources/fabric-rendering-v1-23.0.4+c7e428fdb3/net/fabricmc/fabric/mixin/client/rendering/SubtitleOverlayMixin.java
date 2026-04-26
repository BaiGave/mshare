/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={SubtitleOverlay.class})
public class SubtitleOverlayMixin {
    @WrapMethod(method={"extractRenderState"})
    private void wrapExtractRenderState(GuiGraphicsExtractor context, Operation<Void> original) {
        HudElementRegistryImpl.getRoot(VanillaHudElements.SUBTITLES).extractRenderState(context, Minecraft.getInstance().getDeltaTracker(), (ctx, tc) -> original.call(ctx));
    }
}

