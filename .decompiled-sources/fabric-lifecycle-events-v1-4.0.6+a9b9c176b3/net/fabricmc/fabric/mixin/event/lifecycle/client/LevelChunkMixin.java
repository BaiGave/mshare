/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelChunk.class})
abstract class LevelChunkMixin {
    LevelChunkMixin() {
    }

    @Shadow
    public abstract Level getLevel();

    @ModifyExpressionValue(method={"setBlockEntity"}, at={@At(value="INVOKE", target="Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")})
    private <V> V onLoadBlockEntity(V removedBlockEntity, BlockEntity blockEntity) {
        if (blockEntity != null && blockEntity != removedBlockEntity) {
            if (this.getLevel() instanceof ServerLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, (ServerLevel)this.getLevel());
            } else if (this.getLevel() instanceof ClientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(blockEntity, (ClientLevel)this.getLevel());
            }
        }
        return removedBlockEntity;
    }

    @Inject(method={"setBlockEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V", shift=At.Shift.AFTER)})
    private void onRemoveBlockEntity(BlockEntity blockEntity, CallbackInfo info, @Local(name={"previousEntry"}) BlockEntity previousEntry) {
        if (previousEntry != null) {
            if (this.getLevel() instanceof ServerLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(previousEntry, (ServerLevel)this.getLevel());
            } else if (this.getLevel() instanceof ClientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(previousEntry, (ClientLevel)this.getLevel());
            }
        }
    }

    @Redirect(method={"getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;"}, at=@At(value="INVOKE", target="Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"), slice=@Slice(from=@At(value="INVOKE", target="Lnet/minecraft/world/level/chunk/LevelChunk;createBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;")))
    private <K, V> Object onRemoveBlockEntity(Map<K, V> map, K key) {
        @Nullable V removed = map.remove(key);
        if (removed != null) {
            if (this.getLevel() instanceof ServerLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity)removed, (ServerLevel)this.getLevel());
            } else if (this.getLevel() instanceof ClientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload((BlockEntity)removed, (ClientLevel)this.getLevel());
            }
        }
        return removed;
    }

    @Inject(method={"removeBlockEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/BlockEntity;setRemoved()V")})
    private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, @Local(name={"removeThis"}) @Nullable BlockEntity removeThis) {
        if (removeThis != null) {
            if (this.getLevel() instanceof ServerLevel) {
                ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removeThis, (ServerLevel)this.getLevel());
            } else if (this.getLevel() instanceof ClientLevel) {
                ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(removeThis, (ClientLevel)this.getLevel());
            }
        }
    }
}

