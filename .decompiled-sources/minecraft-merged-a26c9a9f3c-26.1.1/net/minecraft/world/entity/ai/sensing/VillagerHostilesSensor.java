/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;

public class VillagerHostilesSensor
extends NearestVisibleLivingEntitySensor {
    private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.builder().put(EntityType.DROWNED, Float.valueOf(8.0f)).put(EntityType.EVOKER, Float.valueOf(12.0f)).put(EntityType.HUSK, Float.valueOf(8.0f)).put(EntityType.ILLUSIONER, Float.valueOf(12.0f)).put(EntityType.PILLAGER, Float.valueOf(15.0f)).put(EntityType.RAVAGER, Float.valueOf(12.0f)).put(EntityType.VEX, Float.valueOf(8.0f)).put(EntityType.VINDICATOR, Float.valueOf(10.0f)).put(EntityType.ZOGLIN, Float.valueOf(10.0f)).put(EntityType.ZOMBIE, Float.valueOf(8.0f)).put(EntityType.ZOMBIE_VILLAGER, Float.valueOf(8.0f)).build();

    @Override
    protected boolean isMatchingEntity(ServerLevel level, LivingEntity body, LivingEntity mob) {
        return this.isHostile(mob) && this.isClose(body, mob);
    }

    private boolean isClose(LivingEntity body, LivingEntity mob) {
        float distThreshold = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(mob.getType()).floatValue();
        return mob.distanceToSqr(body) <= (double)(distThreshold * distThreshold);
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemoryToSet() {
        return MemoryModuleType.NEAREST_HOSTILE;
    }

    private boolean isHostile(LivingEntity entity) {
        return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(entity.getType());
    }
}

