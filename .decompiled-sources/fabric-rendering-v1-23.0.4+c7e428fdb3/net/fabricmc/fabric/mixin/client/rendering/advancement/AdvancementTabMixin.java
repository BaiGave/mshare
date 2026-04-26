/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;
import net.fabricmc.fabric.mixin.client.rendering.advancement.AdvancementWidgetAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AdvancementTab.class})
abstract class AdvancementTabMixin {
    @Shadow
    @Final
    private AdvancementNode rootNode;
    @Shadow
    private double scrollX;
    @Shadow
    private double scrollY;
    @Shadow
    @Final
    private AdvancementWidget root;

    AdvancementTabMixin() {
    }

    @WrapOperation(method={"extractIcon"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/advancements/AdvancementTabType;extractIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/world/item/ItemStack;)V")})
    private void wrapDrawIcon(@Coerce Object type, GuiGraphicsExtractor graphics, int xo, int yo, int index, ItemStack icon, Operation<Void> original) {
        if (AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT.isBound()) {
            AdvancementRenderContextImpl.IconImpl context = AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT.get();
            ScopedValue.where(AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT, context).call(() -> (Void)original.call(type, graphics, xo, yo, index, icon));
        } else {
            original.call(type, graphics, xo, yo, index, icon);
        }
    }

    @WrapOperation(method={"extractContents"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;enableScissor(IIII)V")})
    private void captureWindowSize(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, Operation<Void> original, @Share(value="bounds") LocalRef<ScreenRectangle> bounds) {
        bounds.set(new ScreenRectangle(0, 0, x1 - x0, y1 - y0));
        original.call(graphics, x0, y0, x1, y1);
    }

    @ModifyExpressionValue(method={"extractContents"}, at={@At(value="CONSTANT", args={"intValue=-1"}, ordinal=0)})
    private int preBackgroundRender(int original, @Share(value="backgroundRenderer") LocalRef<AdvancementRenderer.BackgroundRenderer> backgroundRenderer) {
        AdvancementHolder holder = this.rootNode.holder();
        backgroundRenderer.set(AdvancementRendererRegistryImpl.getBackgroundRenderer(holder.id()));
        return backgroundRenderer.get() == null || backgroundRenderer.get().shouldRenderOriginalBackground() ? original : Integer.MAX_VALUE;
    }

    @Inject(method={"extractContents"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/advancements/AdvancementWidget;extractConnectivity(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIZ)V", ordinal=0)})
    private void extractAdvancementBackground(GuiGraphicsExtractor graphics, int windowLeft, int windowTop, CallbackInfo ci, @Share(value="backgroundRenderer") LocalRef<AdvancementRenderer.BackgroundRenderer> backgroundRenderer, @Share(value="bounds") LocalRef<ScreenRectangle> bounds) {
        if (backgroundRenderer.get() != null) {
            AdvancementProgress progress = ((AdvancementWidgetAccessor)((Object)this.root)).fabric_getProgress();
            backgroundRenderer.get().extractAdvancementBackground(new AdvancementRenderContextImpl.BackgroundImpl(graphics, this.rootNode.holder(), progress, bounds.get(), this.scrollX, this.scrollY));
        }
    }
}

