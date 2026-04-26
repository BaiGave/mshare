/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.impl.item.ItemExtensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
abstract class LivingEntityMixin {
    LivingEntityMixin() {
    }

    @Inject(method={"getEquipmentSlotForItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> info) {
        EquipmentSlotProvider equipmentSlotProvider = ((ItemExtensions)((Object)stack.getItem())).fabric_getEquipmentSlotProvider();
        if (equipmentSlotProvider != null) {
            info.setReturnValue(equipmentSlotProvider.getEquipmentSlotForItem((LivingEntity)((Object)this), stack));
        }
    }
}

