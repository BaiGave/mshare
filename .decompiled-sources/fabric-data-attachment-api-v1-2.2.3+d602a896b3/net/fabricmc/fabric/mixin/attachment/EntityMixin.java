/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentTargetInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Entity.class})
abstract class EntityMixin
implements AttachmentTargetImpl {
    @Shadow
    private int id;

    EntityMixin() {
    }

    @Shadow
    public abstract Level level();

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V")}, method={"load"})
    private void readEntityAttachments(ValueInput data, CallbackInfo ci) {
        this.fabric_readAttachmentsFromNbt(data);
    }

    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V")}, method={"saveWithoutId"})
    private void writeEntityAttachments(ValueOutput output, CallbackInfo ci) {
        this.fabric_writeAttachmentsToNbt(output);
    }

    @Override
    public AttachmentTargetInfo<?> fabric_getSyncTargetInfo() {
        return new AttachmentTargetInfo.EntityTarget(this.id);
    }

    @Override
    public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
        if (!this.level().isClientSide()) {
            ServerPlayer self;
            AttachmentSyncPredicate predicate = ((AttachmentTypeImpl)type).syncPredicate();
            EntityMixin entityMixin = this;
            if (entityMixin instanceof ServerPlayer && predicate.test(this, self = (ServerPlayer)((Object)entityMixin))) {
                AttachmentSync.trySync(change, self);
            }
            PlayerLookup.tracking((Entity)((Object)this)).forEach(player -> {
                if (predicate.test(this, player)) {
                    AttachmentSync.trySync(change, player);
                }
            });
        }
    }

    @Override
    public boolean fabric_shouldTryToSync() {
        return !this.level().isClientSide();
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        return this.level().registryAccess();
    }

    @Inject(method={"setId"}, at={@At(value="HEAD")})
    private void setId(int id, CallbackInfo ci) {
        AttachmentTargetInfo.EntityTarget oldTargetInfo = new AttachmentTargetInfo.EntityTarget(this.id);
        AttachmentTargetInfo.EntityTarget newTargetInfo = new AttachmentTargetInfo.EntityTarget(id);
        this.fabric_updateSyncTarget(oldTargetInfo, newTargetInfo);
    }
}

