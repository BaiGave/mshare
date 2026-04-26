/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.impl.client.rendering.ArmorRendererRegistryImpl;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={LivingEntityRenderer.class})
abstract class LivingEntityRendererMixin {
    LivingEntityRendererMixin() {
    }

    @WrapOperation(method={"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;shouldRender(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z")})
    private boolean toggleDefaultHeadItem(ItemStack headStack, EquipmentSlot slot, Operation<Boolean> original, @Local(argsOnly=true) LivingEntity entity) {
        if (original.call(headStack, slot).booleanValue()) {
            return true;
        }
        ArmorRenderer renderer = ArmorRendererRegistryImpl.get(headStack.getItem());
        return renderer != null && !renderer.shouldRenderDefaultHeadItem(entity, headStack);
    }
}

