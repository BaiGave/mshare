/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin {
    @Inject(method={"collectEquipmentChanges"}, at={@At(value="INVOKE", target="Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")})
    private void getEquipmentChanges(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, @Local(name={"slot"}) EquipmentSlot slot, @Local(name={"previous"}) ItemStack previous, @Local(name={"current"}) ItemStack current) {
        ServerEntityEvents.EQUIPMENT_CHANGE.invoker().onChange((LivingEntity)((Object)this), slot, previous, current);
    }
}

