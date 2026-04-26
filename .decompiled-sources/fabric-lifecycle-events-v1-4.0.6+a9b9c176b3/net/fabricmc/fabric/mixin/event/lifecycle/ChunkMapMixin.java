/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChunkMap.class})
public abstract class ChunkMapMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method={"lambda$scheduleUnload$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ChunkMap;save(Lnet/minecraft/world/level/chunk/ChunkAccess;)Z")})
    private void onChunkUnload(ChunkHolder chunkHolder, CompletableFuture<?> completableFuture, long l, CallbackInfo ci, @Local(name={"chunk"}) ChunkAccess chunk) {
        if (chunk instanceof LevelChunk) {
            LevelChunk levelChunk = (LevelChunk)chunk;
            ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, levelChunk);
        }
    }
}

