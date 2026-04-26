/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChunkHolder.class})
public class ChunkHolderMixin {
    @Inject(method={"broadcastBlockEntity"}, at={@At(value="TAIL")})
    private void broadcastBlockEntity(List<ServerPlayer> players, Level level, BlockPos blockPos, CallbackInfo ci, @Local(name={"blockEntity"}) BlockEntity blockEntity) {
        if (blockEntity != null) {
            ((AttachmentTargetImpl)((Object)blockEntity)).fabric_sendAndClearDeferredSyncChanges(players);
        }
    }
}

