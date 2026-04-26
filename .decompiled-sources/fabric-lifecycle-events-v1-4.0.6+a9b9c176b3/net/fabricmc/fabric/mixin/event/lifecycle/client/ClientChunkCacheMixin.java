/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientChunkCache.class})
public abstract class ClientChunkCacheMixin {
    @Final
    @Shadow
    private ClientLevel level;

    @Inject(method={"replaceWithPacketData"}, at={@At(value="TAIL")})
    private void onChunkLoad(int x, int z, FriendlyByteBuf friendlyByteBuf, Map<Heightmap.Types, long[]> highmap, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> info) {
        ClientChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.level, info.getReturnValue());
    }

    @Inject(method={"replaceWithPacketData"}, at={@At(value="NEW", target="net/minecraft/world/level/chunk/LevelChunk")})
    private void onChunkUnload(int x, int z, FriendlyByteBuf buf, Map<Heightmap.Types, long[]> highmap, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> info, @Local(name={"chunk"}) LevelChunk chunk) {
        if (chunk != null) {
            ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, chunk);
        }
    }

    @Inject(method={"drop"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;drop(ILnet/minecraft/world/level/chunk/LevelChunk;)V")})
    private void onChunkUnload(ChunkPos pos, CallbackInfo ci, @Local(name={"currentChunk"}) LevelChunk currentChunk) {
        ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, currentChunk);
    }

    @Inject(method={"updateViewRadius"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;inRange(II)Z")})
    private void onUpdateLoadDistance(int loadDistance, CallbackInfo ci, @Local(name={"newStorage"}) ClientChunkCache.Storage newStorage, @Local(name={"chunk"}) LevelChunk chunk, @Local(name={"pos"}) ChunkPos pos) {
        if (!newStorage.inRange(pos.x(), pos.z())) {
            ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, chunk);
        }
    }
}

