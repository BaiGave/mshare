/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.util.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
abstract class LivingEntityMixin {
    LivingEntityMixin() {
    }

    @Shadow
    public abstract boolean isDeadOrDying();

    @Shadow
    public abstract Optional<BlockPos> getSleepingPos();

    @WrapOperation(method={"die"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;killedEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)Z")})
    private boolean onEntityKilledOther(Entity entity, ServerLevel serverLevel, @Nullable LivingEntity attacker, DamageSource damageSource, Operation<Boolean> original) {
        boolean result = original.call(entity, serverLevel, attacker, damageSource);
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.invoker().afterKilledOtherEntity(serverLevel, entity, attacker, damageSource);
        return result;
    }

    @Inject(method={"die"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V")})
    private void notifyDeath(DamageSource source, CallbackInfo ci) {
        ServerLivingEntityEvents.AFTER_DEATH.invoker().afterDeath((LivingEntity)((Object)this), source);
    }

    @Redirect(method={"hurtServer"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;isDeadOrDying()Z", ordinal=1))
    boolean beforeEntityKilled(LivingEntity livingEntity, ServerLevel level, DamageSource source, float amount) {
        return this.isDeadOrDying() && ServerLivingEntityEvents.ALLOW_DEATH.invoker().allowDeath(livingEntity, source, amount);
    }

    @Inject(method={"hurtServer"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z")}, cancellable=true)
    private void beforeDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage((LivingEntity)((Object)this), source, amount)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"hurtServer"}, at={@At(value="TAIL")})
    private void afterDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, @Local(name={"originalDamage"}) float originalDamage, @Local(name={"blocked"}) boolean blocked) {
        if (!this.isDeadOrDying()) {
            ServerLivingEntityEvents.AFTER_DAMAGE.invoker().afterDamage((LivingEntity)((Object)this), source, originalDamage, amount, blocked);
        }
    }

    @Inject(method={"startSleeping"}, at={@At(value="RETURN")})
    private void onSleep(BlockPos pos, CallbackInfo info) {
        EntitySleepEvents.START_SLEEPING.invoker().onStartSleeping((LivingEntity)((Object)this), pos);
    }

    @Inject(method={"stopSleeping"}, at={@At(value="HEAD")})
    private void onWakeUp(CallbackInfo info) {
        BlockPos sleepingPos = this.getSleepingPos().orElse(null);
        if (sleepingPos != null) {
            EntitySleepEvents.STOP_SLEEPING.invoker().onStopSleeping((LivingEntity)((Object)this), sleepingPos);
        }
    }

    @Inject(method={"lambda$checkBedExists$0"}, at={@At(value="RETURN")}, cancellable=true)
    @Dynamic(value="lambda$checkBedExists$0: Synthetic lambda body for Optional.map in checkBedExists")
    private void onIsSleepingInBed(BlockPos sleepingPos, CallbackInfoReturnable<Boolean> info) {
        BlockState bedState = ((LivingEntity)((Object)this)).level().getBlockState(sleepingPos);
        EventResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed((LivingEntity)((Object)this), sleepingPos, bedState, info.getReturnValueZ());
        if (result != EventResult.PASS) {
            info.setReturnValue(result.allowAction());
        }
    }

    @WrapOperation(method={"getBedOrientation"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/BedBlock;getBedOrientation(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Direction;")})
    private Direction onGetSleepingDirection(BlockGetter level, BlockPos sleepingPos, Operation<Direction> operation) {
        Direction sleepingDirection = operation.call(level, sleepingPos);
        return EntitySleepEvents.MODIFY_SLEEPING_DIRECTION.invoker().modifySleepDirection((LivingEntity)((Object)this), sleepingPos, sleepingDirection);
    }

    @ModifyVariable(method={"lambda$stopSleeping$0", "startSleeping"}, at=@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    @Dynamic(value="lambda$stopSleeping$0: Synthetic lambda body for Optional.ifPresent in stopSleeping")
    private BlockState modifyBedForOccupiedState(BlockState state, BlockPos sleepingPos) {
        EventResult result = EntitySleepEvents.ALLOW_BED.invoker().allowBed((LivingEntity)((Object)this), sleepingPos, state, state.getBlock() instanceof BedBlock);
        return result.allowAction(false) ? Blocks.RED_BED.defaultBlockState() : state;
    }

    @Redirect(method={"lambda$stopSleeping$0", "startSleeping"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    @Dynamic(value="lambda$stopSleeping$0: Synthetic lambda body for Optional.ifPresent in stopSleeping")
    private boolean setOccupiedState(Level level, BlockPos pos, BlockState state, int flags) {
        BlockState originalState = level.getBlockState(pos);
        boolean occupied = state.getValue(BedBlock.OCCUPIED);
        if (EntitySleepEvents.SET_BED_OCCUPATION_STATE.invoker().setBedOccupationState((LivingEntity)((Object)this), pos, originalState, occupied)) {
            return true;
        }
        if (originalState.hasProperty(BedBlock.OCCUPIED)) {
            return level.setBlock(pos, (BlockState)originalState.setValue(BedBlock.OCCUPIED, occupied), flags);
        }
        return false;
    }

    @Redirect(method={"lambda$stopSleeping$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/BedBlock;findStandUpPosition(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;F)Ljava/util/Optional;"))
    @Dynamic(value="lambda$stopSleeping$0: Synthetic lambda body for Optional.ifPresent in stopSleeping")
    private Optional<Vec3> modifyWakeUpPosition(EntityType<?> type, CollisionGetter level, BlockPos pos, Direction direction, float yaw) {
        Optional<Object> original = Optional.empty();
        BlockState bedState = level.getBlockState(pos);
        if (bedState.getBlock() instanceof BedBlock) {
            original = BedBlock.findStandUpPosition(type, level, pos, direction, yaw);
        }
        Vec3 newPos = EntitySleepEvents.MODIFY_WAKE_UP_POSITION.invoker().modifyWakeUpPosition((LivingEntity)((Object)this), pos, bedState, original.orElse(null));
        return Optional.ofNullable(newPos);
    }
}

