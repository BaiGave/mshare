/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.item.v1;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface CustomDamageHandler {
    public int hurtAndBreak(ItemStack var1, int var2, LivingEntity var3, EquipmentSlot var4, Runnable var5);
}

