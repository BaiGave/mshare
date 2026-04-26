/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.AttachmentTypeImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.fabricmc.fabric.mixin.attachment.AttachmentTargetsMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelChunk.class})
abstract class LevelChunkMixin
extends AttachmentTargetsMixin
implements AttachmentTargetImpl {
    @Shadow
    @Final
    private Level level;

    LevelChunkMixin() {
    }

    @Shadow
    public abstract Map<BlockPos, BlockEntity> getBlockEntities();

    @Inject(method={"<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V"}, at={@At(value="TAIL")})
    private void transferProtoChunkAttachment(ServerLevel level, ProtoChunk protoChunk, LevelChunk.PostLoadProcessor entityLoader, CallbackInfo ci) {
        AttachmentTargetImpl.transfer(protoChunk, this, false);
    }

    @Override
    public void fabric_computeInitialSyncChanges(ServerPlayer player, Consumer<AttachmentChange> changeOutput) {
        super.fabric_computeInitialSyncChanges(player, changeOutput);
        for (BlockEntity be : this.getBlockEntities().values()) {
            ((AttachmentTargetImpl)((Object)be)).fabric_computeInitialSyncChanges(player, changeOutput);
        }
    }

    @Override
    public void fabric_syncChange(AttachmentType<?> type, AttachmentChange change) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            PlayerLookup.tracking(serverLevel, ((ChunkAccess)((Object)this)).getPos()).forEach(player -> {
                if (((AttachmentTypeImpl)type).syncPredicate().test(this, player)) {
                    AttachmentSync.trySync(change, player);
                }
            });
        }
    }

    @Override
    public boolean fabric_shouldTryToSync() {
        return !this.level.isClientSide();
    }

    @Override
    public RegistryAccess fabric_getRegistryAccess() {
        return this.level.registryAccess();
    }
}

