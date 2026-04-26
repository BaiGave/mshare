/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={HumanoidMobRenderer.class})
abstract class HumanoidMobRendererMixin {
    HumanoidMobRendererMixin() {
    }

    @WrapOperation(method={"getEquipmentIfRenderable"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z")})
    private static boolean permitArmorWithCustomRenderers(ItemStack stack, EquipmentSlot slot, Operation<Boolean> original) {
        return original.call(stack, slot) != false || ArmorRendererRegistryImpl.get(stack.getItem()) != null;
    }
}

