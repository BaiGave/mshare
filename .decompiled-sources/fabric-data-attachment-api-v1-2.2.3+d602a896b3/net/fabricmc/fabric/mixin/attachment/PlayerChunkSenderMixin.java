/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentChange;
import net.fabricmc.fabric.impl.attachment.sync.AttachmentSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={PlayerChunkSender.class})
abstract class PlayerChunkSenderMixin {
    PlayerChunkSenderMixin() {
    }

    @WrapOperation(method={"sendNextChunks"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/network/PlayerChunkSender;sendChunk(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;)V")})
    private void sendInitialAttachmentData(ServerGamePacketListenerImpl handler, ServerLevel level, LevelChunk chunk, Operation<Void> original, ServerPlayer player) {
        original.call(handler, level, chunk);
        ArrayList<AttachmentChange> changes = new ArrayList<AttachmentChange>();
        ((AttachmentTargetImpl)((Object)chunk)).fabric_computeInitialSyncChanges(player, changes::add);
        if (!changes.isEmpty()) {
            AttachmentSync.trySync(changes, player);
        }
    }
}

