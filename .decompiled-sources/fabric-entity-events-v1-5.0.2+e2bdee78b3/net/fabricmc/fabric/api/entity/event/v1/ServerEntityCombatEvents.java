/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ServerEntityCombatEvents {
    public static final Event<AfterKilledOtherEntity> AFTER_KILLED_OTHER_ENTITY = EventFactory.createArrayBacked(AfterKilledOtherEntity.class, callbacks -> (level, entity, killedEntity, damageSource) -> {
        for (AfterKilledOtherEntity callback : callbacks) {
            callback.afterKilledOtherEntity(level, entity, killedEntity, damageSource);
        }
    });

    private ServerEntityCombatEvents() {
    }

    @FunctionalInterface
    public static interface AfterKilledOtherEntity {
        public void afterKilledOtherEntity(ServerLevel var1, Entity var2, LivingEntity var3, DamageSource var4);
    }
}

