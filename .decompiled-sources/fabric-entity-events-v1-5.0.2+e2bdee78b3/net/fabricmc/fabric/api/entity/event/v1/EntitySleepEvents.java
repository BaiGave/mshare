/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.util.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EntitySleepEvents {
    public static final Event<AllowSleeping> ALLOW_SLEEPING = EventFactory.createArrayBacked(AllowSleeping.class, callbacks -> (player, sleepingPos) -> {
        for (AllowSleeping callback : callbacks) {
            Player.BedSleepingProblem reason = callback.allowSleep(player, sleepingPos);
            if (reason == null) continue;
            return reason;
        }
        return null;
    });
    public static final Event<StartSleeping> START_SLEEPING = EventFactory.createArrayBacked(StartSleeping.class, callbacks -> (entity, sleepingPos) -> {
        for (StartSleeping callback : callbacks) {
            callback.onStartSleeping(entity, sleepingPos);
        }
    });
    public static final Event<StopSleeping> STOP_SLEEPING = EventFactory.createArrayBacked(StopSleeping.class, callbacks -> (entity, sleepingPos) -> {
        for (StopSleeping callback : callbacks) {
            callback.onStopSleeping(entity, sleepingPos);
        }
    });
    public static final Event<AllowBed> ALLOW_BED = EventFactory.createArrayBacked(AllowBed.class, callbacks -> (entity, sleepingPos, state, vanillaResult) -> {
        for (AllowBed callback : callbacks) {
            EventResult result = callback.allowBed(entity, sleepingPos, state, vanillaResult);
            if (result == EventResult.PASS) continue;
            return result;
        }
        return EventResult.PASS;
    });
    public static final Event<AllowNearbyMonsters> ALLOW_NEARBY_MONSTERS = EventFactory.createArrayBacked(AllowNearbyMonsters.class, callbacks -> (player, sleepingPos, vanillaResult) -> {
        for (AllowNearbyMonsters callback : callbacks) {
            EventResult result = callback.allowNearbyMonsters(player, sleepingPos, vanillaResult);
            if (result == EventResult.PASS) continue;
            return result;
        }
        return EventResult.PASS;
    });
    public static final Event<AllowResettingTime> ALLOW_RESETTING_TIME = EventFactory.createArrayBacked(AllowResettingTime.class, callbacks -> player -> {
        for (AllowResettingTime callback : callbacks) {
            if (callback.allowResettingTime(player)) continue;
            return false;
        }
        return true;
    });
    public static final Event<ModifySleepingDirection> MODIFY_SLEEPING_DIRECTION = EventFactory.createArrayBacked(ModifySleepingDirection.class, callbacks -> (entity, sleepingPos, sleepingDirection) -> {
        for (ModifySleepingDirection callback : callbacks) {
            sleepingDirection = callback.modifySleepDirection(entity, sleepingPos, sleepingDirection);
        }
        return sleepingDirection;
    });
    public static final Event<AllowSettingSpawn> ALLOW_SETTING_SPAWN = EventFactory.createArrayBacked(AllowSettingSpawn.class, callbacks -> (player, sleepingPos) -> {
        for (AllowSettingSpawn callback : callbacks) {
            if (callback.allowSettingSpawn(player, sleepingPos)) continue;
            return false;
        }
        return true;
    });
    public static final Event<SetBedOccupationState> SET_BED_OCCUPATION_STATE = EventFactory.createArrayBacked(SetBedOccupationState.class, callbacks -> (entity, sleepingPos, bedState, occupied) -> {
        for (SetBedOccupationState callback : callbacks) {
            if (!callback.setBedOccupationState(entity, sleepingPos, bedState, occupied)) continue;
            return true;
        }
        return false;
    });
    public static final Event<ModifyWakeUpPosition> MODIFY_WAKE_UP_POSITION = EventFactory.createArrayBacked(ModifyWakeUpPosition.class, callbacks -> (entity, sleepingPos, bedState, wakeUpPos) -> {
        for (ModifyWakeUpPosition callback : callbacks) {
            wakeUpPos = callback.modifyWakeUpPosition(entity, sleepingPos, bedState, wakeUpPos);
        }
        return wakeUpPos;
    });

    private EntitySleepEvents() {
    }

    @FunctionalInterface
    public static interface ModifyWakeUpPosition {
        public @Nullable Vec3 modifyWakeUpPosition(LivingEntity var1, BlockPos var2, BlockState var3, @Nullable Vec3 var4);
    }

    @FunctionalInterface
    public static interface SetBedOccupationState {
        public boolean setBedOccupationState(LivingEntity var1, BlockPos var2, BlockState var3, boolean var4);
    }

    @FunctionalInterface
    public static interface AllowSettingSpawn {
        public boolean allowSettingSpawn(Player var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface ModifySleepingDirection {
        public @Nullable Direction modifySleepDirection(LivingEntity var1, BlockPos var2, @Nullable Direction var3);
    }

    @FunctionalInterface
    public static interface AllowResettingTime {
        public boolean allowResettingTime(Player var1);
    }

    @FunctionalInterface
    public static interface AllowNearbyMonsters {
        public EventResult allowNearbyMonsters(Player var1, BlockPos var2, boolean var3);
    }

    @FunctionalInterface
    public static interface AllowBed {
        public EventResult allowBed(LivingEntity var1, BlockPos var2, BlockState var3, boolean var4);
    }

    @FunctionalInterface
    public static interface StopSleeping {
        public void onStopSleeping(LivingEntity var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface StartSleeping {
        public void onStartSleeping(LivingEntity var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface AllowSleeping {
        public @Nullable Player.BedSleepingProblem allowSleep(Player var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface AllowSleepTime {
        public EventResult allowSleepTime(Player var1, BlockPos var2, boolean var3);
    }
}

