/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.advancement;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;
import net.fabricmc.fabric.mixin.client.rendering.advancement.ClientAdvancementsAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={AdvancementToast.class})
abstract class AdvancementToastMixin {
    @Shadow
    @Final
    private AdvancementHolder advancement;

    AdvancementToastMixin() {
    }

    @WrapOperation(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;fakeItem(Lnet/minecraft/world/item/ItemStack;II)V")})
    private void extractAdvancementIcon(GuiGraphicsExtractor graphics, ItemStack icon, int x, int y, Operation<Void> original) {
        AdvancementRenderer.IconRenderer iconRenderer = AdvancementRendererRegistryImpl.getIconRenderer(this.advancement.id());
        if (iconRenderer == null || iconRenderer.shouldRenderOriginalIcon()) {
            original.call(graphics, icon, x, y);
        }
        if (iconRenderer != null) {
            ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
            AdvancementProgress progress = ((ClientAdvancementsAccessor)((Object)advancements)).fabric_getProgress().get(this.advancement);
            iconRenderer.extractAdvancementIcon(new AdvancementRenderContextImpl.IconImpl(graphics, this.advancement, progress, x, y, false, false));
        }
    }
}

