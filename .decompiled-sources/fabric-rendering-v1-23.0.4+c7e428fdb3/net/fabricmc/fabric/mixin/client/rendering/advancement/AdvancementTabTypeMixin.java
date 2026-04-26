/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net/minecraft/client/gui/screens/advancements/AdvancementTabType"})
abstract class AdvancementTabTypeMixin {
    AdvancementTabTypeMixin() {
    }

    @WrapOperation(method={"extractIcon"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;II)V")})
    private void extractAdvancementIcon(GuiGraphicsExtractor graphics, ItemStack icon, int x, int y, Operation<Void> original) {
        if (AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT.isBound()) {
            AdvancementRenderContextImpl.IconImpl context = AdvancementRendererRegistryImpl.TAB_ICON_RENDER_CONTEXT.get();
            context.setPos(x, y);
            AdvancementRenderer.IconRenderer iconRenderer = AdvancementRendererRegistryImpl.getIconRenderer(context.holder().id());
            if (iconRenderer.shouldRenderOriginalIcon()) {
                original.call(graphics, icon, x, y);
            }
            iconRenderer.extractAdvancementIcon(context);
        } else {
            original.call(graphics, icon, x, y);
        }
    }
}

