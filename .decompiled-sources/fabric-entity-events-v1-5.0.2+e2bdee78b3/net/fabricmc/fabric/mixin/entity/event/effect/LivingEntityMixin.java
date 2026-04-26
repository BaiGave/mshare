/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin
extends Entity {
    private LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapMethod(method={"canBeAffected"})
    private boolean allowAddEffect(MobEffectInstance effectInstance, Operation<Boolean> original) {
        if (this.isClient()) {
            return original.call(effectInstance);
        }
        if (!ServerMobEffectEvents.ALLOW_ADD.invoker().allowAdd(effectInstance, this.self(), MobEffectUtil.getCommandContext())) {
            return false;
        }
        return original.call(effectInstance);
    }

    @Inject(method={"addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"}, at={@At(value="INVOKE", target="Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")})
    private void beforeAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (this.isClient()) {
            return;
        }
        ServerMobEffectEvents.BEFORE_ADD.invoker().beforeAdd(effectInstance, this.self(), MobEffectUtil.getCommandContext());
    }

    @Inject(method={"forceAddEffect"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;canBeAffected(Lnet/minecraft/world/effect/MobEffectInstance;)Z", shift=At.Shift.AFTER)})
    private void beforeForceAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
        if (this.isClient()) {
            return;
        }
        ServerMobEffectEvents.BEFORE_ADD.invoker().beforeAdd(effectInstance, this.self(), MobEffectUtil.getCommandContext());
    }

    @Inject(method={"onEffectAdded"}, at={@At(value="RETURN")})
    private void afterAddEffect(MobEffectInstance effectInstance, Entity entity, CallbackInfo ci) {
        if (this.isClient()) {
            return;
        }
        ServerMobEffectEvents.AFTER_ADD.invoker().afterAdd(effectInstance, this.self(), MobEffectUtil.getCommandContext());
    }

    @WrapOperation(method={"removeAllEffects"}, at={@At(value="INVOKE", target="Ljava/util/Map;clear()V")})
    private void allowRemoveAllEffects(Map<Holder<MobEffect>, MobEffectInstance> instance, Operation<Void> original) {
        if (this.isClient()) {
            return;
        }
        Set<Map.Entry<Holder<MobEffect>, MobEffectInstance>> effectEntries = Set.copyOf(instance.entrySet());
        original.call(instance);
        for (Map.Entry<Holder<MobEffect>, MobEffectInstance> entry : effectEntries) {
            Holder<MobEffect> effect = entry.getKey();
            MobEffectInstance effectInstance = entry.getValue();
            boolean cannotRemove = !ServerMobEffectEvents.ALLOW_EARLY_REMOVE.invoker().allowEarlyRemove(effectInstance, this.self(), MobEffectUtil.getCommandContext());
            if (!cannotRemove) continue;
            instance.put(effect, effectInstance);
        }
    }

    @WrapMethod(method={"removeEffect"})
    private boolean allowRemoveEffect(Holder<MobEffect> holder, Operation<Boolean> original) {
        boolean cannotRemove;
        if (this.isClient()) {
            return original.call(holder);
        }
        MobEffectInstance effectInstance = this.self().getEffect(holder);
        if (effectInstance == null) {
            return original.call(holder);
        }
        boolean bl = cannotRemove = !ServerMobEffectEvents.ALLOW_EARLY_REMOVE.invoker().allowEarlyRemove(effectInstance, this.self(), MobEffectUtil.getCommandContext());
        if (cannotRemove) {
            return false;
        }
        return original.call(holder);
    }

    @Inject(method={"removeEffect"}, at={@At(value="HEAD")})
    private void beforeRemoveEffect(Holder<MobEffect> holder, CallbackInfoReturnable<Boolean> cir) {
        if (this.isClient()) {
            return;
        }
        MobEffectInstance effectInstance = this.self().getEffect(holder);
        if (effectInstance == null) {
            return;
        }
        ServerMobEffectEvents.BEFORE_REMOVE.invoker().beforeRemove(effectInstance, (LivingEntity)((Object)this), MobEffectUtil.getCommandContext());
    }

    @Inject(method={"tickEffects"}, at={@At(value="INVOKE", target="Ljava/util/Iterator;remove()V")})
    private void beforeExpireRemoveEffect(CallbackInfo ci, @Local(name={"effect"}) MobEffectInstance effect) {
        if (this.isClient()) {
            return;
        }
        ServerMobEffectEvents.BEFORE_REMOVE.invoker().beforeRemove(effect, this.self(), MobEffectUtil.getCommandContext());
    }

    @Inject(method={"removeAllEffects"}, at={@At(value="INVOKE", target="Lcom/google/common/collect/Maps;newHashMap(Ljava/util/Map;)Ljava/util/HashMap;")})
    private void beforeRemoveAllEffects(CallbackInfoReturnable<Boolean> cir) {
        if (this.isClient()) {
            return;
        }
        for (MobEffectInstance effectInstance : this.self().getActiveEffects()) {
            ServerMobEffectEvents.BEFORE_REMOVE.invoker().beforeRemove(effectInstance, this.self(), MobEffectUtil.getCommandContext());
        }
    }

    @Inject(method={"onEffectsRemoved"}, at={@At(value="RETURN")})
    private void afterRemoveEffect(Collection<MobEffectInstance> collection, CallbackInfo ci) {
        if (this.isClient()) {
            return;
        }
        for (MobEffectInstance effectInstance : collection) {
            ServerMobEffectEvents.AFTER_REMOVE.invoker().afterRemove(effectInstance, this.self(), MobEffectUtil.getCommandContext());
        }
    }

    @Unique
    private boolean isClient() {
        return this.level().isClientSide();
    }

    @Unique
    private LivingEntity self() {
        return (LivingEntity)((Object)this);
    }
}

