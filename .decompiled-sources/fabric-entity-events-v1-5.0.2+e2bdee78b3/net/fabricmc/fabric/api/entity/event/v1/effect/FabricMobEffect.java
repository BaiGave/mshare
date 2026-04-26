/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public interface FabricMobEffect {
    default public void onEffectAdded(MobEffectInstance effectInstance, LivingEntity entity) {
    }

    default public void onEffectStarted(MobEffectInstance effectInstance, LivingEntity entity) {
    }

    default public void onEffectRemoved(MobEffectInstance effectInstance, LivingEntity entity) {
    }
}

