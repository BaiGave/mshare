/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.nautilus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.behavior.ChargeAttack;
import net.minecraft.world.entity.ai.behavior.CountDownCooldownTicks;
import net.minecraft.world.entity.ai.behavior.FollowTemptation;
import net.minecraft.world.entity.ai.behavior.GateBehavior;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.nautilus.NautilusAi;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.schedule.Activity;

public class ZombieNautilusAi {
    private static final float SPEED_MULTIPLIER_WHEN_IDLING_IN_WATER = 1.0f;
    private static final float SPEED_MULTIPLIER_WHEN_TEMPTED = 0.9f;
    private static final float SPEED_WHEN_ATTACKING = 0.5f;
    private static final float ATTACK_KNOCKBACK_FORCE = 2.0f;
    private static final int TIME_BETWEEN_ATTACKS = 80;
    private static final double MAX_CHARGE_DISTANCE = 12.0;
    private static final double MAX_TARGET_DETECTION_DISTANCE = 11.0;

    public static List<ActivityData<ZombieNautilus>> getActivities() {
        return List.of(ZombieNautilusAi.initCoreActivity(), ZombieNautilusAi.initIdleActivity(), ZombieNautilusAi.initFightActivity());
    }

    private static ActivityData<ZombieNautilus> initCoreActivity() {
        return ActivityData.create(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.CHARGE_COOLDOWN_TICKS), new CountDownCooldownTicks(MemoryModuleType.ATTACK_TARGET_COOLDOWN)));
    }

    private static ActivityData<ZombieNautilus> initIdleActivity() {
        return ActivityData.create(Activity.IDLE, ImmutableList.of(Pair.of(1, new FollowTemptation(mob -> Float.valueOf(0.9f), mob -> mob.isBaby() ? 2.5 : 3.5)), Pair.of(2, StartAttacking.create(NautilusAi::findNearestValidAttackTarget)), Pair.of(3, new GateBehavior(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableSet.of(), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.TRY_ALL, ImmutableList.of(Pair.of(RandomStroll.swim(1.0f), 2), Pair.of(SetWalkTargetFromLookTarget.create(1.0f, 3), 3))))));
    }

    private static ActivityData<ZombieNautilus> initFightActivity() {
        return ActivityData.create(Activity.FIGHT, ImmutableList.of(Pair.of(0, new ChargeAttack(80, NautilusAi.ATTACK_TARGET_CONDITIONS, 0.5f, 2.0f, 12.0, 11.0, SoundEvents.ZOMBIE_NAUTILUS_DASH))), ImmutableSet.of(Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT)));
    }

    public static void updateActivity(ZombieNautilus body) {
        body.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }
}

