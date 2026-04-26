/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event.elytra;

import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
abstract class LivingEntityMixin
extends Entity {
    LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
        throw new AssertionError();
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/util/Util;getRandom(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/lang/Object;")}, method={"updateFallFlying()V"}, allow=1, cancellable=true)
    void injectElytraTick(CallbackInfo info) {
        LivingEntity self = (LivingEntity)((Object)this);
        if (!EntityElytraEvents.ALLOW.invoker().allowElytraFlight(self)) {
            if (!this.level().isClientSide()) {
                this.setSharedFlag(7, false);
            }
            info.cancel();
        }
        if (EntityElytraEvents.CUSTOM.invoker().useCustomElytra(self, true)) {
            info.cancel();
        }
    }

    @Inject(at={@At(value="FIELD", target="Lnet/minecraft/world/entity/EquipmentSlot;VALUES:Ljava/util/List;", opcode=178)}, method={"canGlide"}, allow=1, cancellable=true)
    void injectElytraCheck(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)((Object)this);
        if (!EntityElytraEvents.ALLOW.invoker().allowElytraFlight(self)) {
            cir.setReturnValue(false);
            return;
        }
        if (EntityElytraEvents.CUSTOM.invoker().useCustomElytra(self, false)) {
            cir.setReturnValue(true);
        }
    }
}

