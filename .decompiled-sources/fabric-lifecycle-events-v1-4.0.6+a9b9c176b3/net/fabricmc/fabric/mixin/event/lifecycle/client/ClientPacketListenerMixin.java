/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
abstract class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;
    @Shadow
    @Final
    private RegistryAccess.Frozen registryAccess;

    ClientPacketListenerMixin() {
    }

    @Inject(method={"handleRespawn"}, at={@At(value="NEW", target="net/minecraft/client/multiplayer/ClientLevel")})
    private void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (this.level != null) {
            for (Entity entity : this.level.entitiesForRendering()) {
                ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
            }
            for (LevelChunk chunk : ((LoadedChunksCache)((Object)this.level)).fabric_getLoadedChunks()) {
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
                }
            }
        }
    }

    @Inject(method={"handleLogin"}, at={@At(value="NEW", target="net/minecraft/client/multiplayer/ClientLevel")})
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (this.level != null) {
            for (Entity entity : this.level.entitiesForRendering()) {
                ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
            }
            for (LevelChunk chunk : ((LoadedChunksCache)((Object)this.level)).fabric_getLoadedChunks()) {
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
                }
            }
        }
    }

    @Inject(method={"clearLevel"}, at={@At(value="HEAD")})
    private void onClearLevel(CallbackInfo ci) {
        if (this.level != null) {
            for (Entity entity : this.level.entitiesForRendering()) {
                ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
            }
            for (LevelChunk chunk : ((LoadedChunksCache)((Object)this.level)).fabric_getLoadedChunks()) {
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
                }
            }
        }
    }

    @Inject(method={"handleUpdateTags"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/FuelValues;vanillaBurnTimes(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/world/level/block/entity/FuelValues;")})
    private void invokeTagsLoaded(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
        CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(this.registryAccess, true);
    }
}

