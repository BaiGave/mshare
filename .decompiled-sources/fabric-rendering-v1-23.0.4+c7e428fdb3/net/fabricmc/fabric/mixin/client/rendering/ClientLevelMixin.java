/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.fabricmc.fabric.impl.client.rendering.ColorResolverRegistryImpl;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientLevel.class})
public abstract class ClientLevelMixin {
    @Unique
    private final Reference2ReferenceMap<ColorResolver, BlockTintCache> customColorCache = ColorResolverRegistryImpl.createCustomCacheMap(resolver -> new BlockTintCache(pos -> this.calculateBlockTint((BlockPos)pos, (ColorResolver)resolver)));

    @Shadow
    public abstract int calculateBlockTint(BlockPos var1, ColorResolver var2);

    @Inject(method={"onChunkLoaded(Lnet/minecraft/world/level/ChunkPos;)V"}, at={@At(value="RETURN")})
    private void onResetChunkColor(ChunkPos chunkPos, CallbackInfo ci) {
        for (BlockTintCache cache : this.customColorCache.values()) {
            cache.invalidateForChunk(chunkPos.x(), chunkPos.z());
        }
    }

    @Inject(method={"clearTintCaches()V"}, at={@At(value="RETURN")})
    private void onReloadColor(CallbackInfo ci) {
        for (BlockTintCache cache : this.customColorCache.values()) {
            cache.invalidateAll();
        }
    }

    @ModifyExpressionValue(method={"getBlockTint(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/ColorResolver;)I"}, at={@At(value="INVOKE", target="Lit/unimi/dsi/fastutil/objects/Object2ObjectArrayMap;get(Ljava/lang/Object;)Ljava/lang/Object;")})
    private Object modifyNullCache(Object cache, BlockPos pos, ColorResolver resolver) {
        if (cache == null && (cache = this.customColorCache.get(resolver)) == null) {
            throw new UnsupportedOperationException("ClientLevel.getColor called with unregistered ColorResolver " + String.valueOf(resolver));
        }
        return cache;
    }
}

