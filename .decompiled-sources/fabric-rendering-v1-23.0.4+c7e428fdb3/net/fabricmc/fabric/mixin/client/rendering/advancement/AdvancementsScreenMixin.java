/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;
import net.fabricmc.fabric.mixin.client.rendering.advancement.AdvancementTabAccessor;
import net.fabricmc.fabric.mixin.client.rendering.advancement.AdvancementWidgetAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={AdvancementsScreen.class})
abstract class AdvancementsScreenMixin {
    @Shadow
    private @Nullable AdvancementTab selectedTab;

    AdvancementsScreenMixin() {
    }

    @WrapOperation(method={"extractWindow"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;extractIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V")})
    private void wrapDrawIcon(AdvancementTab tab, GuiGraphicsExtractor graphics, int xo, int yo, Operation<Void> original, @Local(name={"mouseX"}) int mouseX, @Local(name={"mouseY"}) int mouseY) {
        AdvancementHolder holder = tab.getRootNode().holder();
        if (AdvancementRendererRegistryImpl.getIconRenderer(holder.id()) != null) {
            boolean hovered = tab.isMouseOver(xo, yo, mouseX, mouseY);
            boolean selected = this.selectedTab == tab;
            AdvancementProgress progress = ((AdvancementWidgetAccessor)((Object)((AdvancementTabAccessor)((Object)tab)).fabric_getRoot())).fabric_getProgress();
            ScopedValue.where(AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT, new AdvancementRenderContextImpl.IconImpl(graphics, holder, progress, hovered, selected)).call(() -> (Void)original.call(tab, graphics, xo, yo));
        } else {
            original.call(tab, graphics, xo, yo);
        }
    }
}

