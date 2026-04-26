/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AdvancementWidget.class})
abstract class AdvancementWidgetMixin {
    @Shadow
    @Final
    private AdvancementNode advancementNode;
    @Shadow
    private @Nullable AdvancementProgress progress;

    AdvancementWidgetMixin() {
    }

    @WrapOperation(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;II)V")})
    private void extractAdvancementIcon(GuiGraphicsExtractor graphics, ItemStack icon, int x, int y, Operation<Void> original) {
        this.extractAdvancementIcon(graphics, x, y, false, () -> original.call(graphics, icon, x, y));
    }

    @WrapOperation(method={"extractHover"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;II)V")})
    private void extractAdvancementIconHover(GuiGraphicsExtractor graphics, ItemStack icon, int x, int y, Operation<Void> original) {
        this.extractAdvancementIcon(graphics, x, y, true, () -> original.call(graphics, icon, x, y));
    }

    @Unique
    private void extractAdvancementIcon(GuiGraphicsExtractor graphics, int x, int y, boolean hovered, Runnable original) {
        AdvancementRenderer.IconRenderer iconRenderer = AdvancementRendererRegistryImpl.getIconRenderer(this.advancementNode.holder().id());
        if (iconRenderer == null || iconRenderer.shouldRenderOriginalIcon()) {
            original.run();
        }
        if (iconRenderer != null) {
            iconRenderer.extractAdvancementIcon(new AdvancementRenderContextImpl.IconImpl(graphics, this.advancementNode.holder(), this.progress, x, y, hovered, false));
        }
    }

    @WrapOperation(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")})
    private void extractAdvancementFrame(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        this.extractAdvancementFrame(graphics, x, y, false, () -> original.call(graphics, renderPipeline, location, x, y, width, height));
    }

    @WrapOperation(method={"extractHover"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal=3)})
    private void renderAdvancementFrameHover(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, Operation<Void> original) {
        this.extractAdvancementFrame(graphics, x, y, true, () -> original.call(graphics, renderPipeline, location, x, y, width, height));
    }

    @Unique
    private void extractAdvancementFrame(GuiGraphicsExtractor graphics, int x, int y, boolean hovered, Runnable original) {
        AdvancementRenderer.FrameRenderer frameRenderer = AdvancementRendererRegistryImpl.getFrameRenderer(this.advancementNode.holder().id());
        if (frameRenderer == null || frameRenderer.shouldRenderOriginalFrame()) {
            original.run();
        }
        if (frameRenderer != null) {
            frameRenderer.extractAdvancementFrame(new AdvancementRenderContextImpl.FrameImpl(graphics, this.advancementNode.holder(), this.progress, x, y, hovered));
        }
    }

    @Inject(method={"extractHover"}, at={@At(value="INVOKE", target="Ljava/util/List;isEmpty()Z")})
    private void captureExtractTooltip(GuiGraphicsExtractor graphics, int xo, int yo, float fade, int screenxo, int screenyo, CallbackInfo ci, @Share(value="renderTooltip") LocalBooleanRef renderTooltip) {
        AdvancementRenderer.FrameRenderer frameRenderer = AdvancementRendererRegistryImpl.getFrameRenderer(this.advancementNode.holder().id());
        renderTooltip.set(frameRenderer == null || frameRenderer.shouldRenderTooltip());
    }

    @WrapWithCondition(method={"extractHover"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;extractMultilineText(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;III)V")})
    private boolean cancelTooltipMultilineTextRendering(AdvancementWidget widget, GuiGraphicsExtractor graphics, List<FormattedCharSequence> lines, int x, int y, int color, @Share(value="renderTooltip") LocalBooleanRef renderTooltip) {
        return renderTooltip.get();
    }

    @WrapWithCondition(method={"extractHover"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")})
    private boolean cancelTooltipStringRendering(GuiGraphicsExtractor graphics, Font font, Component str, int x, int y, int color, @Share(value="renderTooltip") LocalBooleanRef renderTooltip) {
        return renderTooltip.get();
    }

    @WrapWithCondition(method={"extractHover"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V")})
    private boolean cancelTooltipTitleBarRendering(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier location, int spriteWidth, int spriteHeight, int textureX, int textureY, int x, int y, int width, int height, @Share(value="renderTooltip") LocalBooleanRef renderTooltip) {
        return renderTooltip.get();
    }

    @WrapWithCondition(method={"extractHover"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V")}, slice={@Slice(from=@At(value="INVOKE", target="Ljava/util/List;isEmpty()Z"), to=@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal=2))})
    private boolean cancelTooltipTitleBoxRendering(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier location, int x, int y, int width, int height, @Share(value="renderTooltip") LocalBooleanRef renderTooltip) {
        return renderTooltip.get();
    }
}

